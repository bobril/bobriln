"use strict";
var encoder = require('./encoder');
var decoder = require('./decoder');
var asap_1 = require('./asap');
if (typeof DEBUG === "undefined")
    DEBUG = true;
var nativeMethodName2Idx = Object.create(null);
var eventDecoder = new decoder.Decoder();
var nativeMethodParamEncoder = new encoder.Encoder();
var nativeMethodResultDecoder = new decoder.Decoder();
var nativeCallResultCallbacks = [];
var nativeCallResultResolvers = [];
var nativeCallResultRejecters = [];
var localPlatformName = undefined;
function platformName() {
    return localPlatformName;
}
exports.platformName = platformName;
var readyResolver;
exports.readyPromise = new Promise(function (resolve, _reject) {
    readyResolver = resolve;
});
function reset() {
    prepareToCallNativeByIndex(-1);
    callNativeAsync(function (decoder) {
        var fw = decoder.readAny();
        if (fw != "Bobril Native")
            throw new Error("Wrong framework " + fw);
        localPlatformName = decoder.readAny();
        var idx = 0;
        while (!decoder.isAnyEnd()) {
            nativeMethodName2Idx[decoder.readAny()] = idx++;
        }
        console.log("Starting " + fw + " " + localPlatformName + " with " + idx + " methods");
        readyResolver();
        __bobrilncb();
    });
}
var eventHandler = undefined;
function setEventHandler(handler) {
    eventHandler = handler;
}
exports.setEventHandler = setEventHandler;
function writeEventResult(id, value) {
    nativeMethodParamEncoder.writeNumber(-2);
    nativeMethodParamEncoder.writeNumber(id);
    if (value)
        nativeMethodParamEncoder.writeTrue();
    else
        nativeMethodParamEncoder.writeFalse();
    nativeMethodParamEncoder.writeEndOfBlock();
    scheduleNativeCall();
}
var inAsyncEvent = false;
function __bobrilncb() {
    if (inAsyncEvent)
        return;
    var _loop_1 = function() {
        if (eventDecoder.isEOF()) {
            eventDecoder.initFromLatin1String(__bobriln.c(""));
            if (eventDecoder.isEOF())
                return { value: void 0 };
        }
        var id = eventDecoder.readAny();
        var name_1 = eventDecoder.readAny();
        var param = eventDecoder.readAny();
        var nodeId = eventDecoder.readAny();
        var time = -1;
        if (!eventDecoder.isAnyEnd()) {
            time = eventDecoder.readAny();
        }
        while (!eventDecoder.isAnyEnd())
            eventDecoder.next();
        eventDecoder.next();
        console.log("Event " + name_1 + " " + JSON.stringify(param) + " node:" + nodeId + " time:" + time);
        var res = eventHandler(name_1, param, nodeId, time);
        if (res === true || res === false) {
            if (id >= 0)
                writeEventResult(id, res);
        }
        else {
            inAsyncEvent = true;
            res.then(function (val) {
                inAsyncEvent = false;
                if (id >= 0)
                    writeEventResult(id, val);
                __bobrilncb();
            }, function (err) {
                console.error(err);
                inAsyncEvent = false;
                if (id >= 0)
                    writeEventResult(id, false);
                __bobrilncb();
            });
            return { value: void 0 };
        }
    };
    while (true) {
        var state_1 = _loop_1();
        if (typeof state_1 === "object") return state_1.value;
    }
}
window["__bobrilncb"] = __bobrilncb;
function prepareToCallNativeByName(name) {
    var idx = nativeMethodName2Idx[name];
    if (idx === undefined) {
        throw new Error("There is no native method " + name);
    }
    return prepareToCallNativeByIndex(idx | 0);
}
exports.prepareToCallNativeByName = prepareToCallNativeByName;
function prepareToCallNativeByIndex(index) {
    nativeMethodParamEncoder.writeNumber(index);
    return nativeMethodParamEncoder;
}
var nativeCallScheduled = false;
function scheduleNativeCall() {
    if (nativeCallScheduled)
        return;
    nativeCallScheduled = true;
    asap_1.asap(function () {
        nativeCallScheduled = false;
        nativeMethodResultDecoder.initFromLatin1String(__bobriln.c(nativeMethodParamEncoder.toLatin1String()));
        nativeMethodParamEncoder.reset();
        var cbs = nativeCallResultCallbacks.splice(0);
        var resolvers = nativeCallResultResolvers.splice(0);
        var rejecters = nativeCallResultRejecters.splice(0);
        var resi = 0;
        for (var i = 0; i < cbs.length; i++) {
            var cb = cbs[i];
            if (cb != null) {
                try {
                    var res = cb(nativeMethodResultDecoder);
                    resolvers[resi](res);
                }
                catch (err) {
                    rejecters[resi](err);
                }
                resi++;
            }
            while (!nativeMethodResultDecoder.isAnyEnd())
                nativeMethodResultDecoder.next();
            nativeMethodResultDecoder.next();
        }
    });
}
function callNativeIgnoreResult() {
    nativeMethodParamEncoder.writeEndOfBlock();
    nativeCallResultCallbacks.push(undefined);
    scheduleNativeCall();
}
exports.callNativeIgnoreResult = callNativeIgnoreResult;
function callNativeAsync(resultParser) {
    nativeMethodParamEncoder.writeEndOfBlock();
    nativeCallResultCallbacks.push(resultParser);
    scheduleNativeCall();
    var result = new Promise(function (resolve, reject) {
        nativeCallResultResolvers.push(resolve);
        nativeCallResultRejecters.push(reject);
    });
    return result;
}
reset();
