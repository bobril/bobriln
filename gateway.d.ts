import * as encoder from './encoder';
export declare function platformName(): string | undefined;
export declare let readyPromise: Promise<any>;
export declare function setEventHandler(handler: (name: string, param: Object, nodeId: number, time: number) => boolean | PromiseLike<boolean>): void;
export declare function prepareToCallNativeByName(name: string): encoder.Encoder;
export declare function callNativeIgnoreResult(): void;
