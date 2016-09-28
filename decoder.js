"use strict";
var Decoder = (function () {
    function Decoder() {
        this.buf = undefined;
        this.ofs = 0;
        this.len = 0;
        this.stack = [];
        this.dv = undefined;
        this.peekType = 9 /* EOF */;
        this.peekLiteral = undefined;
    }
    Decoder.prototype.initFromLatin1String = function (value) {
        var len = value.length;
        if (this.len < len) {
            this.buf = new Uint8Array(len);
        }
        var buf = this.buf;
        for (var i = 0; i < len; i++) {
            buf[i] = value.charCodeAt(i);
        }
        this.ofs = 0;
        this.len = len;
        this.dv = undefined;
        this.stack.length = 0;
        this.peekType = 9 /* EOF */;
        this.peekLiteral = undefined;
        this.next();
    };
    Decoder.prototype.setPeek = function (type, lit, ofs) {
        this.peekType = type;
        this.peekLiteral = lit;
        this.ofs = ofs;
    };
    Decoder.prototype.throwEOF = function () {
        throw new Error("EOF too early");
    };
    Decoder.prototype.getDv = function () {
        if (this.dv !== undefined)
            return this.dv;
        this.dv = new DataView(this.buf.buffer);
        return this.dv;
    };
    Decoder.prototype.parseString = function (strLen, ofs) {
        var batchLen = Math.min(4096, strLen);
        var sb = new Uint16Array(batchLen);
        var res = "";
        var buf = this.buf;
        var len = this.len;
        while (strLen > 0) {
            if (strLen < batchLen)
                batchLen = strLen;
            var oofs = 0;
            while (oofs < batchLen) {
                if (ofs >= len)
                    this.throwEOF();
                var b = buf[ofs++];
                if (b < 0x80) {
                    sb[oofs++] = b;
                }
                else if (b < 0xc0) {
                    if (ofs >= len)
                        this.throwEOF();
                    sb[oofs++] = ((b - 0x80) << 8) + buf[ofs++];
                }
                else {
                    if (ofs + 1 >= len)
                        this.throwEOF();
                    sb[oofs++] = (buf[ofs] << 8) + buf[ofs + 1];
                    ofs += 2;
                }
            }
            res += String.fromCharCode.apply(null, sb.subarray(0, batchLen));
            strLen -= batchLen;
        }
        this.setPeek(1 /* String */, res, ofs);
    };
    Decoder.prototype.readVarLenInt = function (lenBytesM1, ofs) {
        if (lenBytesM1 === 0) {
            this.ofs = ofs + 1;
            return this.buf[ofs];
        }
        if (lenBytesM1 === 1) {
            this.ofs = ofs + 2;
            var buf_1 = this.buf;
            return 256 + (buf_1[ofs] << 8) + buf_1[ofs + 1];
        }
        if (lenBytesM1 === 2) {
            this.ofs = ofs + 3;
            var buf_2 = this.buf;
            return 0x10100 + (buf_2[ofs] << 16) + (buf_2[ofs + 1] << 8) + buf_2[ofs + 2];
        }
        this.ofs = ofs + 4;
        var buf = this.buf;
        return 0x1010100 + (buf[ofs] << 24) + (buf[ofs + 1] << 16) + (buf[ofs + 2] << 8) + buf[ofs + 3];
    };
    Decoder.prototype.next = function () {
        switch (this.peekType) {
            case 4 /* ArrayStart */: {
                if (this.stack[this.stack.length - 1]-- === 0) {
                    this.peekType = 5 /* ArrayEnd */;
                    this.stack.length -= 2;
                    return;
                }
                break;
            }
            case 6 /* MapStart */: {
                if (this.stack[this.stack.length - 1]-- === 0) {
                    this.peekType = 7 /* MapEnd */;
                    this.stack.length -= 2;
                    return;
                }
                break;
            }
            default: {
                var s = this.stack;
                var sl = s.length;
                if (sl > 0) {
                    if (s[sl - 2] === 0 /* Array */) {
                        if (s[sl - 1]-- === 0) {
                            this.peekType = 5 /* ArrayEnd */;
                            this.peekLiteral = undefined;
                            s.length -= 2;
                            return;
                        }
                    }
                    else if (s[sl - 2] === 1 /* MapKey */) {
                        s[sl - 2] = 2 /* MapValue */;
                    }
                    else {
                        s[sl - 2] = 1 /* MapKey */;
                        if (s[sl - 1]-- === 0) {
                            this.peekType = 7 /* MapEnd */;
                            this.peekLiteral = undefined;
                            s.length -= 2;
                            return;
                        }
                    }
                }
                break;
            }
        }
        var buf = this.buf;
        var ofs = this.ofs;
        var len = this.len;
        if (ofs >= len) {
            this.setPeek(9 /* EOF */, undefined, ofs);
            return;
        }
        var b = buf[ofs++];
        if (b <= 63 /* Number63 */) {
            this.setPeek(0 /* Number */, b, ofs);
            return;
        }
        if (b >= 224 /* NumberM32 */) {
            this.setPeek(0 /* Number */, b - 256, ofs);
            return;
        }
        if (b <= 127 /* String63 */) {
            var l = b - 64 /* String0 */;
            if (l === 0) {
                this.setPeek(1 /* String */, "", ofs);
                return;
            }
            this.parseString(l, ofs);
            return;
        }
        if (b <= 159 /* Map31 */) {
            this.stack.push(1 /* MapKey */, b - 128 /* Map0 */);
            this.setPeek(6 /* MapStart */, undefined, ofs);
            return;
        }
        if (b <= 191 /* Array31 */) {
            this.stack.push(0 /* Array */, b - 160 /* Array0 */);
            this.setPeek(4 /* ArrayStart */, undefined, ofs);
            return;
        }
        switch (b) {
            case 192 /* PositiveInt1 */:
            case 192 /* PositiveInt1 */ + 1:
            case 192 /* PositiveInt1 */ + 2:
            case 195 /* PositiveInt4 */: {
                this.peekType = 0 /* Number */;
                this.peekLiteral = 64 + this.readVarLenInt(b - 192 /* PositiveInt1 */, ofs);
                break;
            }
            case 196 /* NegativeInt1 */:
            case 196 /* NegativeInt1 */ + 1:
            case 196 /* NegativeInt1 */ + 2:
            case 199 /* NegativeInt4 */: {
                this.peekType = 0 /* Number */;
                this.peekLiteral = -33 - this.readVarLenInt(b - 196 /* NegativeInt1 */, ofs);
                break;
            }
            case 200 /* String1 */:
            case 200 /* String1 */ + 1:
            case 200 /* String1 */ + 2:
            case 203 /* String4 */: {
                var l = 64 + this.readVarLenInt(b - 200 /* String1 */, ofs);
                this.parseString(l, this.ofs);
                break;
            }
            case 204 /* Map1 */:
            case 204 /* Map1 */ + 1:
            case 204 /* Map1 */ + 2:
            case 207 /* Map4 */: {
                var l = 32 + this.readVarLenInt(b - 204 /* Map1 */, ofs);
                this.stack.push(1 /* MapKey */, l);
                this.peekType = 6 /* MapStart */;
                this.peekLiteral = undefined;
                break;
            }
            case 208 /* Array1 */:
            case 208 /* Array1 */ + 1:
            case 208 /* Array1 */ + 2:
            case 211 /* Array4 */: {
                var l = 32 + this.readVarLenInt(b - 208 /* Array1 */, ofs);
                this.stack.push(0 /* Array */, l);
                this.peekType = 4 /* ArrayStart */;
                this.peekLiteral = undefined;
                break;
            }
            case 212 /* Double */: {
                this.setPeek(0 /* Number */, this.getDv().getFloat64(ofs, true), ofs + 8);
                break;
            }
            case 213 /* False */: {
                this.setPeek(2 /* Boolean */, false, ofs);
                break;
            }
            case 214 /* True */: {
                this.setPeek(2 /* Boolean */, true, ofs);
                break;
            }
            case 215 /* Null */: {
                this.setPeek(3 /* Null */, undefined, ofs);
                break;
            }
            case 216 /* EndOfBlock */: {
                this.setPeek(8 /* EndOfBlock */, undefined, ofs);
                break;
            }
            default:
                throw new Error("Corrupted");
        }
    };
    Decoder.prototype.isAnyEnd = function () {
        return (this.peekType === 9 /* EOF */) || (this.peekType === 8 /* EndOfBlock */);
    };
    Decoder.prototype.isEOF = function () {
        return this.peekType === 9 /* EOF */;
    };
    Decoder.prototype.readAny = function () {
        switch (this.peekType) {
            case 9 /* EOF */:
            case 8 /* EndOfBlock */: {
                this.throwEOF();
                break;
            }
            case 2 /* Boolean */:
            case 3 /* Null */:
            case 0 /* Number */:
            case 1 /* String */: {
                var res = this.peekLiteral;
                this.next();
                return res;
            }
            case 5 /* ArrayEnd */:
            case 7 /* MapEnd */: {
                throw new Error("Reading something end");
            }
            case 4 /* ArrayStart */: {
                var res = [];
                this.next();
                while (this.peekType !== 5 /* ArrayEnd */) {
                    res.push(this.readAny());
                }
                this.next();
                return res;
            }
            case 6 /* MapStart */: {
                var res = Object.create(null);
                this.next();
                while (this.peekType !== 7 /* MapEnd */) {
                    if (this.peekType !== 1 /* String */)
                        throw new Error("Object key is not string");
                    var key = this.peekLiteral;
                    this.next();
                    res[key] = this.readAny();
                }
                this.next();
                return res;
            }
            default: throw new Error("Unexpected peekType " + this.peekType);
        }
    };
    return Decoder;
}());
exports.Decoder = Decoder;
