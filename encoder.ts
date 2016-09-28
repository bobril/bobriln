import { EncodedType } from './decEncCommon';

export class Encoder {
    buf: Uint8Array;
    dv: DataView | undefined;
    ofs: number;
    len: number;

    constructor() {
        this.ofs = 0;
        this.len = 16;
        this.buf = new Uint8Array(this.len);
        this.dv = undefined;
    }

    private reserve(capacity: number) {
        if (this.len >= this.ofs + capacity) return;
        this.len = Math.max(this.ofs + capacity, this.len * 2);
        let oldBuf = this.buf;
        this.buf = new Uint8Array(this.len);
        this.buf.set(oldBuf);
        this.dv = undefined;
    }

    toLatin1String(): string {
        var bufView = new Uint8Array(this.buf.buffer, 0, this.ofs);
        var length = this.ofs;
        var result = '';
        var addition = 65535;

        for (var i = 0; i < length; i += addition) {

            if (i + addition > length) {
                addition = length - i;
            }
            result += String.fromCharCode.apply(null, bufView.subarray(i, i + addition));
        }
        return result;
    }

    reset() {
        this.ofs = 0;
    }

    private getDv(): DataView {
        if (this.dv !== undefined) return this.dv;
        this.dv = new DataView(this.buf.buffer);
        return this.dv;
    }

    writeInternalVarLenInt(n: number, encType: EncodedType) {
        n = n | 0;
        if (n < 256) {
            this.reserve(2);
            this.buf[this.ofs++] = encType;
            this.buf[this.ofs++] = n;
            return;
        }
        n -= 256;
        if (n < 65536) {
            this.reserve(3);
            this.buf[this.ofs++] = encType + 1;
            this.buf[this.ofs++] = n >> 8;
            this.buf[this.ofs++] = n & 255;
            return;
        }
        n -= 65536;
        if (n < 0x1000000) {
            this.reserve(4);
            this.buf[this.ofs++] = encType + 2;
            this.buf[this.ofs++] = n >> 16;
            this.buf[this.ofs++] = (n >> 8) & 255;
            this.buf[this.ofs++] = n & 255;
            return;
        }
        n -= 0x1000000;
        this.reserve(5);
        this.buf[this.ofs++] = encType + 3;
        this.getDv().setUint32(this.ofs, n);
    }

    writeUInt(n: number) {
        n = n | 0;
        if (n < 64) {
            this.reserve(1);
            this.buf[this.ofs++] = n;
            return;
        }
        this.writeInternalVarLenInt(n - 64, EncodedType.PositiveInt1);
    }

    private writeNegativeInt(n: number) {
        n = n | 0;
        if (n >= -32) {
            this.reserve(1);
            this.buf[this.ofs++] = 256 + n;
            return;
        }
        this.writeInternalVarLenInt(-n - 33, EncodedType.NegativeInt1);
    }

    writeNumber(n: number) {
        if (n === (n | 0)) {
            if (n >= 0) this.writeUInt(n); else this.writeNegativeInt(n);
            return;
        }
        this.reserve(9);
        this.buf[this.ofs++] = EncodedType.Double;
        this.getDv().setFloat64(this.ofs, n, true);
        this.ofs += 8;
    }

    writeString(s: string) {
        let len = s.length;
        let res = 0;
        for (let i = 0; i < len; i++) {
            let n = s.charCodeAt(i);
            if (n < 0x80) {
                res += 1;
            } else if (n < 0x4000) {
                res += 2;
            } else {
                res += 3;
            }
        }
        if (len < 64) {
            this.reserve(1 + res);
            this.buf[this.ofs++] = EncodedType.String0 + len;
            if (len == 0) return;
        } else {
            this.reserve(5 + res);
            this.writeInternalVarLenInt(len - 64, EncodedType.String1);
        }
        let b = this.buf;
        let o = this.ofs;
        for (let i = 0; i < len; i++) {
            let n = s.charCodeAt(i);
            if (n < 0x80) {
                b[o++] = n;
            } else if (n < 0x4000) {
                b[o++] = 0x80 + (n >> 8);
                b[o++] = n & 255;
            } else {
                b[o++] = 0xC0;
                b[o++] = n >> 8;
                b[o++] = n & 255;
            }
        }
        this.ofs = o;
    }

    writeNull() {
        this.reserve(1);
        this.buf[this.ofs++] = EncodedType.Null;
    }

    writeTrue() {
        this.reserve(1);
        this.buf[this.ofs++] = EncodedType.True;
    }

    writeFalse() {
        this.reserve(1);
        this.buf[this.ofs++] = EncodedType.False;
    }

    writeArray(arr: any[]) {
        let len = arr.length;
        if (len < 32) {
            this.reserve(1 + len);
            this.buf[this.ofs++] = EncodedType.Array0 + len;
            if (len == 0) return;
        } else {
            this.writeInternalVarLenInt(len - 32, EncodedType.Array1);
        }
        for (let i = 0; i < len; i++) {
            this.writeAny(arr[i]);
        }
    }

    writeObject(obj: Object) {
        let keys = Object.keys(obj);
        let len = keys.length;
        if (len < 32) {
            this.reserve(1 + len);
            this.buf[this.ofs++] = EncodedType.Map0 + len;
            if (len == 0) return;
        } else {
            this.writeInternalVarLenInt(len - 32, EncodedType.Map1);
        }
        for (let i = 0; i < len; i++) {
            let key = keys[i];
            this.writeString(key);
            this.writeAny((obj as any)[key]);
        }
    }

    writeAny(a: any) {
        if (a == null) {
            this.writeNull();
            return;
        }
        let to = typeof a;
        if (to === "string") {
            this.writeString(a);
        } else if (to === "number") {
            this.writeNumber(a);
        } else if (to === "boolean") {
            if (a === true) this.writeTrue(); else this.writeFalse();
        } else if (Array.isArray(a)) {
            this.writeArray(a);
        } else {
            this.writeObject(a);
        }
    }

    writeEndOfBlock() {
        this.reserve(1);
        this.buf[this.ofs++] = EncodedType.EndOfBlock;
    }
}
