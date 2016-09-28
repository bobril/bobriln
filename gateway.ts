import * as encoder from './encoder';
import * as decoder from './decoder';
import { asap } from './asap';

declare var DEBUG: boolean;
if (typeof DEBUG === "undefined") DEBUG = true;

declare var __bobriln: {
    // call native method number, rest are parameters or when param=="" call JS event id, name, params, nodeid 
    c(param: string): string;
};

const nativeMethodName2Idx: { [name: string]: number } = Object.create(null);
const eventDecoder = new decoder.Decoder();
const nativeMethodParamEncoder = new encoder.Encoder();
const nativeMethodResultDecoder = new decoder.Decoder();
const nativeCallResultCallbacks: (((decoder: decoder.Decoder) => any) | undefined)[] = [];
const nativeCallResultResolvers: ((param: any) => void)[] = [];
const nativeCallResultRejecters: ((param: any) => void)[] = [];
let localPlatformName: string | undefined = undefined;
export function platformName() {
    return localPlatformName;
}
let readyResolver: () => void;
export let readyPromise: Promise<any> = new Promise((resolve, _reject) => {
    readyResolver = resolve;
});

function reset() {
    prepareToCallNativeByIndex(-1);
    callNativeAsync((decoder: decoder.Decoder) => {
        let fw = decoder.readAny();
        if (fw != "Bobril Native") throw new Error("Wrong framework " + fw);
        localPlatformName = decoder.readAny();
        let idx = 0;
        while (!decoder.isAnyEnd()) {
            nativeMethodName2Idx[decoder.readAny()] = idx++;
        }
        console.log("Starting " + fw + " " + localPlatformName + " with " + idx + " methods");
        readyResolver();
        __bobrilncb();
    });
}

let eventHandler: ((name: string, param: Object, nodeId: number, time: number) => boolean | PromiseLike<boolean>) | undefined = undefined;

export function setEventHandler(handler: (name: string, param: Object, nodeId: number, time: number) => boolean | PromiseLike<boolean>) {
    eventHandler = handler;
}

function writeEventResult(id: number, value: boolean) {
    nativeMethodParamEncoder.writeNumber(-2);
    nativeMethodParamEncoder.writeNumber(id);
    if (value)
        nativeMethodParamEncoder.writeTrue();
    else
        nativeMethodParamEncoder.writeFalse();
    nativeMethodParamEncoder.writeEndOfBlock();
    scheduleNativeCall();
}

let inAsyncEvent = false;

function __bobrilncb() {
    if (inAsyncEvent)
        return;
    while (true) {
        if (eventDecoder.isEOF()) {
            eventDecoder.initFromLatin1String(__bobriln.c(""));
            if (eventDecoder.isEOF())
                return;
        }
        let id = eventDecoder.readAny();
        let name = eventDecoder.readAny();
        let param = eventDecoder.readAny();
        let nodeId = eventDecoder.readAny();
        let time = -1;
        if (!eventDecoder.isAnyEnd()) {
            time = eventDecoder.readAny();
        }
        while (!eventDecoder.isAnyEnd()) eventDecoder.next();
        eventDecoder.next();
        console.log("Event " + name + " " + JSON.stringify(param) + " node:" + nodeId + " time:" + time);
        let res = eventHandler!(name, param, nodeId, time);
        if (res === true || res === false) {
            if (id >= 0) writeEventResult(id, <boolean>res);
        } else {
            inAsyncEvent = true;
            (<PromiseLike<boolean>>res).then((val) => {
                inAsyncEvent = false;
                if (id >= 0) writeEventResult(id, val);
                __bobrilncb();
            }, (err) => {
                console.error(err);
                inAsyncEvent = false;
                if (id >= 0) writeEventResult(id, false);
                __bobrilncb();
            });
            return;
        }
    }
}

(window as any)["__bobrilncb"] = __bobrilncb;

export function prepareToCallNativeByName(name: string): encoder.Encoder {
    const idx = nativeMethodName2Idx[name];
    if (idx === undefined) {
        throw new Error("There is no native method " + name);
    }
    return prepareToCallNativeByIndex(idx | 0);
}

function prepareToCallNativeByIndex(index: number): encoder.Encoder {
    nativeMethodParamEncoder.writeNumber(index);
    return nativeMethodParamEncoder;
}

let nativeCallScheduled = false;

function scheduleNativeCall() {
    if (nativeCallScheduled) return;
    nativeCallScheduled = true;
    asap(() => {
        nativeCallScheduled = false;
        nativeMethodResultDecoder.initFromLatin1String(__bobriln.c(nativeMethodParamEncoder.toLatin1String()));
        nativeMethodParamEncoder.reset();
        let cbs = nativeCallResultCallbacks.splice(0);
        let resolvers = nativeCallResultResolvers.splice(0);
        let rejecters = nativeCallResultRejecters.splice(0);
        let resi = 0;
        for (let i = 0; i < cbs.length; i++) {
            let cb = cbs[i];
            if (cb != null) {
                try {
                    let res = cb(nativeMethodResultDecoder);
                    resolvers[resi](res);
                }
                catch (err) {
                    rejecters[resi](err);
                }
                resi++;
            }
            while (!nativeMethodResultDecoder.isAnyEnd()) nativeMethodResultDecoder.next();
            nativeMethodResultDecoder.next();
        }
    });
}

export function callNativeIgnoreResult() {
    nativeMethodParamEncoder.writeEndOfBlock();
    nativeCallResultCallbacks.push(undefined);
    scheduleNativeCall();
}

function callNativeAsync<T>(resultParser: (decoder: decoder.Decoder) => T): PromiseLike<T> {
    nativeMethodParamEncoder.writeEndOfBlock();
    nativeCallResultCallbacks.push(resultParser);
    scheduleNativeCall();
    let result = new Promise<T>((resolve, reject) => {
        nativeCallResultResolvers.push(resolve);
        nativeCallResultRejecters.push(reject);
    });
    return result;
}

reset();
