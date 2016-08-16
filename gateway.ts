import * as encoder from './encoder';
import * as decoder from './decoder';
import { asap } from './asap';

declare var DEBUG: boolean;
if (typeof DEBUG === "undefined") DEBUG = true;

declare var __bobriln: {
    // call native method number, rest are parameters
    c(param: string): string,
    // call JS event id, name, params, nodeid
    p(): string
};

const nativeMethodName2Idx: { [name: string]: number } = Object.create(null);
const eventDecoder = new decoder.Decoder();
const eventEncoder = new encoder.Encoder();
const nativeMethodParamEncoder = new encoder.Encoder();
const nativeMethodResultDecoder = new decoder.Decoder();
const nativeCallResultCallbacks: ((decoder: decoder.Decoder) => any)[] = [];
const nativeCallResultResolvers: ((any) => void)[] = [];
const nativeCallResultRejecters: ((any) => void)[] = [];
export let platformName: string = null;
let readyResolver: ()=>void;
export let readyPromise: Promise<any> = new Promise((resolve,reject)=>{
    readyResolver = resolve;
});

function reset() {
    let params = prepareToCallNativeByIndex(-1);
    callNativeAsync((decoder: decoder.Decoder) => {
        let fw = decoder.readAny();
        if (fw != "Bobril Native") throw new Error("Wrong framework " + fw);
        platformName = decoder.readAny();
        let idx = 0;
        while (!decoder.isAnyEnd()) {
            nativeMethodName2Idx[decoder.readAny()] = idx++;
        }
        readyResolver();
        __bobrilncb();
    });
}

let eventHandler: (name:string, param: Object, nodeId: number) => boolean | PromiseLike<boolean> = null;

export function setEventHandler(handler: (name:string, param: Object, nodeId: number) => boolean | PromiseLike<boolean>) {
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
            eventDecoder.initFromLatin1String(__bobriln.p());
            if (eventDecoder.isEOF())
                return;
        }
        let id = eventDecoder.readAny();
        let name = eventDecoder.readAny();
        let param = eventDecoder.readAny();
        let nodeId = eventDecoder.readAny();
        while (!eventDecoder.isAnyEnd()) eventDecoder.next();
        eventDecoder.next();
        let res = eventHandler(name,param,nodeId);
        if (res===true || res === false) {
            if (id>=0) writeEventResult(id,<boolean>res);
        } else {
            inAsyncEvent = true;
            (<PromiseLike<boolean>>res).then((val)=>{
                inAsyncEvent = false;
                if (id>=0) writeEventResult(id,val);
                __bobrilncb();
            }, (err)=>{
                console.error(err);
                inAsyncEvent = false;
                if (id>=0) writeEventResult(id,false);
                __bobrilncb();
            });
            return;
        }
    }
}

window["__bobrilncb"] = __bobrilncb;

function parseSkipper(decoder: decoder.Decoder): any {
    while (!decoder.isAnyEnd()) decoder.next();
    decoder.next();
    return undefined;
}

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
    nativeCallResultCallbacks.push(null);
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
