import { EncodedType } from './decEncCommon';
export declare class Encoder {
    buf: Uint8Array;
    dv: DataView | undefined;
    ofs: number;
    len: number;
    constructor();
    private reserve(capacity);
    toLatin1String(): string;
    reset(): void;
    private getDv();
    writeInternalVarLenInt(n: number, encType: EncodedType): void;
    writeUInt(n: number): void;
    private writeNegativeInt(n);
    writeNumber(n: number): void;
    writeString(s: string): void;
    writeNull(): void;
    writeTrue(): void;
    writeFalse(): void;
    writeArray(arr: any[]): void;
    writeObject(obj: Object): void;
    writeAny(a: any): void;
    writeEndOfBlock(): void;
}
