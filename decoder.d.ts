export declare const enum PeekType {
    Number = 0,
    String = 1,
    Boolean = 2,
    Null = 3,
    ArrayStart = 4,
    ArrayEnd = 5,
    MapStart = 6,
    MapEnd = 7,
    EndOfBlock = 8,
    EOF = 9,
}
export declare class Decoder {
    buf: Uint8Array | undefined;
    dv: DataView | undefined;
    ofs: number;
    len: number;
    stack: number[];
    peekType: PeekType;
    peekLiteral: number | string | boolean | undefined;
    constructor();
    initFromLatin1String(value: string): void;
    private setPeek(type, lit, ofs);
    private throwEOF();
    private getDv();
    private parseString(strLen, ofs);
    private readVarLenInt(lenBytesM1, ofs);
    next(): void;
    isAnyEnd(): boolean;
    isEOF(): boolean;
    readAny(): any;
}
