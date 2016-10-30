"use strict";
var encoder = require('./encoder');
var decoder = require('./decoder');
var asap_1 = require('./asap');
if (typeof DEBUG === "undefined")
    DEBUG = true;
var nativeMethodName2Idx = Object.create(null);
var nativeCallbackDecoder = new decoder.Decoder();
var nativeMethodParamEncoder = new encoder.Encoder();
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
    });
}
var eventHandler = undefined;
function setEventHandler(handler) {
    eventHandler = handler;
}
exports.setEventHandler = setEventHandler;
function writeEventResult(_id, value) {
    __bobriln.c(value ? "\xd6" : "\xd5"); // true or false
}
var inAsyncEvent = false;
function __bobrilncb() {
    if (inAsyncEvent)
        return;
    var _loop_1 = function() {
        if (nativeCallbackDecoder.isEOF()) {
            nativeCallbackDecoder.initFromLatin1String(__bobriln.c(""));
            if (nativeCallbackDecoder.isEOF())
                return { value: void 0 };
        }
        var id = nativeCallbackDecoder.readAny();
        if (id === 1) {
            var cb = nativeCallResultCallbacks.shift();
            if (cb != null) {
                var resolver = nativeCallResultResolvers.shift();
                var rejecter = nativeCallResultRejecters.shift();
                try {
                    var res_1 = cb(nativeCallbackDecoder);
                    resolver(res_1);
                }
                catch (err) {
                    rejecter(err);
                }
            }
            while (!nativeCallbackDecoder.isAnyEnd())
                nativeCallbackDecoder.next();
            nativeCallbackDecoder.next();
            return "continue";
        }
        var name_1 = nativeCallbackDecoder.readAny();
        var param = nativeCallbackDecoder.readAny();
        var nodeId = nativeCallbackDecoder.readAny();
        var time = -1;
        if (!nativeCallbackDecoder.isAnyEnd()) {
            time = nativeCallbackDecoder.readAny();
        }
        while (!nativeCallbackDecoder.isAnyEnd())
            nativeCallbackDecoder.next();
        nativeCallbackDecoder.next();
        //console.log("Event " + name + " " + JSON.stringify(param) + " node:" + nodeId + " time:" + time);
        var res = eventHandler(name_1, param, nodeId, time);
        if (res === true || res === false) {
            if (id === 0)
                writeEventResult(id, res);
        }
        else {
            inAsyncEvent = true;
            res.then(function (val) {
                inAsyncEvent = false;
                if (id === 0)
                    writeEventResult(id, val);
                __bobrilncb();
            }, function (err) {
                console.error(err);
                inAsyncEvent = false;
                if (id === 0)
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
        __bobriln.c(nativeMethodParamEncoder.toLatin1String());
        nativeMethodParamEncoder.reset();
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
