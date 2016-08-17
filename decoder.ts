import { EncodedType } from './decEncCommon';

export const enum PeekType {
    Number = 0,
    String = 1,
    Boolean = 2,
    Null = 3,
    ArrayStart = 4,
    ArrayEnd = 5,
    MapStart = 6,
    MapEnd = 7,
    EndOfBlock = 8,
    EOF = 9
}

const enum StateType {
    Array,
    MapKey,
    MapValue
}

export class Decoder {
    buf: Uint8Array;
    dv: DataView;
    ofs: number;
    len: number;
    stack: number[];
    peekType: PeekType;
    peekLiteral: number | string | boolean;

    constructor() {
        this.buf = null;
        this.ofs = 0;
        this.len = 0;
        this.stack = [];
        this.dv = null;
        this.peekType = PeekType.EOF;
        this.peekLiteral = undefined;
    }

    initFromLatin1String(value: string) {
        const len = value.length;
        if (this.len < len) {
            this.buf = new Uint8Array(len);
        }
        const buf = this.buf;
        for (let i = 0; i < len; i++) {
            buf[i] = value.charCodeAt(i);
        }
        this.ofs = 0;
        this.len = len;
        this.dv = null;
        this.stack.length = 0;
        this.peekType = PeekType.EOF;
        this.peekLiteral = undefined;
        this.next();
    }

    private setPeek(type: PeekType, lit: number | string | boolean, ofs: number) {
        this.peekType = type;
        this.peekLiteral = lit;
        this.ofs = ofs;
    }

    private throwEOF() {
        throw new Error("EOF too early");
    }

    private getDv(): DataView {
        if (this.dv !== null) return this.dv;
        this.dv = new DataView(this.buf.buffer);
        return this.dv;
    }

    private parseString(strLen: number, ofs: number) {
        let batchLen = Math.min(4096, strLen);
        let sb = new Uint16Array(batchLen);
        let res = "";
        let buf = this.buf;
        let len = this.len;
        while (strLen > 0) {
            if (strLen < batchLen) batchLen = strLen;
            let oofs = 0;
            while (oofs < batchLen) {
                if (ofs >= len) this.throwEOF();
                let b = buf[ofs++];
                if (b < 0x80) {
                    sb[oofs++] = b;
                } else if (b < 0xc0) {
                    if (ofs >= len) this.throwEOF();
                    sb[oofs++] = ((b - 0x80) << 8) + buf[ofs++];
                } else {
                    if (ofs + 1 >= len) this.throwEOF();
                    sb[oofs++] = (buf[ofs] << 8) + buf[ofs + 1];
                    ofs += 2;
                }
            }
            res += String.fromCharCode.apply(null, sb.subarray(0, batchLen));
            strLen -= batchLen;
        }
        this.setPeek(PeekType.String, res, ofs);
    }

    private readVarLenInt(lenBytesM1: number, ofs: number): number {
        if (lenBytesM1 === 0) {
            this.ofs = ofs + 1;
            return this.buf[ofs];
        }
        if (lenBytesM1 === 1) {
            this.ofs = ofs + 2;
            let buf = this.buf;
            return 256 + (buf[ofs] << 8) + buf[ofs + 1];
        }
        if (lenBytesM1 === 2) {
            this.ofs = ofs + 3;
            let buf = this.buf;
            return 0x10100 + (buf[ofs] << 16) + (buf[ofs + 1] << 8) + buf[ofs + 2];
        }
        this.ofs = ofs + 4;
        let buf = this.buf;
        return 0x1010100 + (buf[ofs] << 24) + (buf[ofs + 1] << 16) + (buf[ofs + 2] << 8) + buf[ofs + 3];
    }

    next() {
        switch (this.peekType) {
            case PeekType.ArrayStart: {
                if (this.stack[this.stack.length - 1]-- === 0) {
                    this.peekType = PeekType.ArrayEnd;
                    this.stack.length -= 2;
                    return;
                }
                break;
            }
            case PeekType.MapStart: {
                if (this.stack[this.stack.length - 1]-- === 0) {
                    this.peekType = PeekType.MapEnd;
                    this.stack.length -= 2;
                    return;
                }
                break;
            }
            default: {
                let s = this.stack;
                let sl = s.length;
                if (sl > 0) {
                    if (s[sl - 2] === StateType.Array) {
                        if (s[sl - 1]-- === 0) {
                            this.peekType = PeekType.ArrayEnd;
                            this.peekLiteral = undefined;
                            s.length -= 2;
                            return;
                        }
                    } else if (s[sl - 2] === StateType.MapKey) {
                        s[sl - 2] = StateType.MapValue;
                    } else {
                        s[sl - 2] = StateType.MapKey;
                        if (s[sl - 1]-- === 0) {
                            this.peekType = PeekType.MapEnd;
                            this.peekLiteral = undefined;
                            s.length -= 2;
                            return;
                        }
                    }
                }
                break;
            }
        }
        let buf = this.buf;
        let ofs = this.ofs;
        let len = this.len;
        if (ofs >= len) {
            this.setPeek(PeekType.EOF, undefined, ofs);
            return;
        }
        let b = buf[ofs++];
        if (b <= EncodedType.Number63) {
            this.setPeek(PeekType.Number, b, ofs);
            return;
        }
        if (b >= EncodedType.NumberM32) {
            this.setPeek(PeekType.Number, b - 256, ofs);
            return;
        }
        if (b <= EncodedType.String63) {
            let l = b - EncodedType.String0;
            if (l === 0) {
                this.setPeek(PeekType.String, "", ofs);
                return;
            }
            this.parseString(l, ofs);
            return;
        }
        if (b <= EncodedType.Map31) {
            this.stack.push(StateType.MapKey, b - EncodedType.Map0);
            this.setPeek(PeekType.MapStart, undefined, ofs);
            return;
        }
        if (b <= EncodedType.Array31) {
            this.stack.push(StateType.Array, b - EncodedType.Array0);
            this.setPeek(PeekType.ArrayStart, undefined, ofs);
            return;
        }
        switch (b) {
            case EncodedType.PositiveInt1:
            case EncodedType.PositiveInt1 + 1:
            case EncodedType.PositiveInt1 + 2:
            case EncodedType.PositiveInt4: {
                this.peekType = PeekType.Number;
                this.peekLiteral = 64 + this.readVarLenInt(b - EncodedType.PositiveInt1, ofs);
                break;
            }
            case EncodedType.NegativeInt1:
            case EncodedType.NegativeInt1 + 1:
            case EncodedType.NegativeInt1 + 2:
            case EncodedType.NegativeInt4: {
                this.peekType = PeekType.Number;
                this.peekLiteral = -33 - this.readVarLenInt(b - EncodedType.NegativeInt1, ofs);
                break;
            }
            case EncodedType.String1:
            case EncodedType.String1 + 1:
            case EncodedType.String1 + 2:
            case EncodedType.String4: {
                let l = 64 + this.readVarLenInt(b - EncodedType.String1, ofs);
                this.parseString(l, this.ofs);
                break;
            }
            case EncodedType.Map1:
            case EncodedType.Map1 + 1:
            case EncodedType.Map1 + 2:
            case EncodedType.Map4: {
                let l = 32 + this.readVarLenInt(b - EncodedType.Map1, ofs);
                this.stack.push(StateType.MapKey, l);
                this.peekType = PeekType.MapStart;
                this.peekLiteral = undefined;
                break;
            }
            case EncodedType.Array1:
            case EncodedType.Array1 + 1:
            case EncodedType.Array1 + 2:
            case EncodedType.Array4: {
                let l = 32 + this.readVarLenInt(b - EncodedType.Array1, ofs);
                this.stack.push(StateType.Array, l);
                this.peekType = PeekType.ArrayStart;
                this.peekLiteral = undefined;
                break;
            }
            case EncodedType.Double: {
                this.setPeek(PeekType.Number, this.getDv().getFloat64(ofs, true), ofs + 8);
                break;
            }
            case EncodedType.False: {
                this.setPeek(PeekType.Boolean, false, ofs);
                break;
            }
            case EncodedType.True: {
                this.setPeek(PeekType.Boolean, true, ofs);
                break;
            }
            case EncodedType.Null: {
                this.setPeek(PeekType.Null, null, ofs);
                break;
            }
            case EncodedType.EndOfBlock: {
                this.setPeek(PeekType.EndOfBlock, null, ofs);
                break;
            }
            default:
                throw new Error("Corrupted");
        }
    }

    isAnyEnd() {
        return (this.peekType === PeekType.EOF) || (this.peekType === PeekType.EndOfBlock);
    }

    isEOF() {
        return this.peekType === PeekType.EOF;
    }

    readAny(): any {
        switch (this.peekType) {
            case PeekType.EOF:
            case PeekType.EndOfBlock: {
                this.throwEOF();
            }
            case PeekType.Boolean:
            case PeekType.Null:
            case PeekType.Number:
            case PeekType.String: {
                let res = this.peekLiteral;
                this.next();
                return res;
            }
            case PeekType.ArrayEnd:
            case PeekType.MapEnd: {
                throw new Error("Reading something end");
            }
            case PeekType.ArrayStart: {
                let res: any[] = [];
                this.next();
                while (this.peekType !== PeekType.ArrayEnd) {
                    res.push(this.readAny());
                }
                this.next();
                return res;
            }
            case PeekType.MapStart: {
                let res = Object.create(null);
                this.next();
                while (this.peekType !== PeekType.MapEnd) {
                    if (this.peekType !== PeekType.String)
                        throw new Error("Object key is not string");
                    let key = <string>this.peekLiteral;
                    this.next();
                    res[key] = this.readAny();
                }
                this.next();
                return res;
            }
            default: throw new Error("Unexpected peekType " + this.peekType);
        }
    }
}
