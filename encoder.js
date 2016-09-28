"use strict";
var Encoder = (function () {
    function Encoder() {
        this.ofs = 0;
        this.len = 16;
        this.buf = new Uint8Array(this.len);
        this.dv = undefined;
    }
    Encoder.prototype.reserve = function (capacity) {
        if (this.len >= this.ofs + capacity)
            return;
        this.len = Math.max(this.ofs + capacity, this.len * 2);
        var oldBuf = this.buf;
        this.buf = new Uint8Array(this.len);
        this.buf.set(oldBuf);
        this.dv = undefined;
    };
    Encoder.prototype.toLatin1String = function () {
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
    };
    Encoder.prototype.reset = function () {
        this.ofs = 0;
    };
    Encoder.prototype.getDv = function () {
        if (this.dv !== undefined)
            return this.dv;
        this.dv = new DataView(this.buf.buffer);
        return this.dv;
    };
    Encoder.prototype.writeInternalVarLenInt = function (n, encType) {
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
    };
    Encoder.prototype.writeUInt = function (n) {
        n = n | 0;
        if (n < 64) {
            this.reserve(1);
            this.buf[this.ofs++] = n;
            return;
        }
        this.writeInternalVarLenInt(n - 64, 192 /* PositiveInt1 */);
    };
    Encoder.prototype.writeNegativeInt = function (n) {
        n = n | 0;
        if (n >= -32) {
            this.reserve(1);
            this.buf[this.ofs++] = 256 + n;
            return;
        }
        this.writeInternalVarLenInt(-n - 33, 196 /* NegativeInt1 */);
    };
    Encoder.prototype.writeNumber = function (n) {
        if (n === (n | 0)) {
            if (n >= 0)
                this.writeUInt(n);
            else
                this.writeNegativeInt(n);
            return;
        }
        this.reserve(9);
        this.buf[this.ofs++] = 212 /* Double */;
        this.getDv().setFloat64(this.ofs, n, true);
        this.ofs += 8;
    };
    Encoder.prototype.writeString = function (s) {
        var len = s.length;
        var res = 0;
        for (var i = 0; i < len; i++) {
            var n = s.charCodeAt(i);
            if (n < 0x80) {
                res += 1;
            }
            else if (n < 0x4000) {
                res += 2;
            }
            else {
                res += 3;
            }
        }
        if (len < 64) {
            this.reserve(1 + res);
            this.buf[this.ofs++] = 64 /* String0 */ + len;
            if (len == 0)
                return;
        }
        else {
            this.reserve(5 + res);
            this.writeInternalVarLenInt(len - 64, 200 /* String1 */);
        }
        var b = this.buf;
        var o = this.ofs;
        for (var i = 0; i < len; i++) {
            var n = s.charCodeAt(i);
            if (n < 0x80) {
                b[o++] = n;
            }
            else if (n < 0x4000) {
                b[o++] = 0x80 + (n >> 8);
                b[o++] = n & 255;
            }
            else {
                b[o++] = 0xC0;
                b[o++] = n >> 8;
                b[o++] = n & 255;
            }
        }
        this.ofs = o;
    };
    Encoder.prototype.writeNull = function () {
        this.reserve(1);
        this.buf[this.ofs++] = 215 /* Null */;
    };
    Encoder.prototype.writeTrue = function () {
        this.reserve(1);
        this.buf[this.ofs++] = 214 /* True */;
    };
    Encoder.prototype.writeFalse = function () {
        this.reserve(1);
        this.buf[this.ofs++] = 213 /* False */;
    };
    Encoder.prototype.writeArray = function (arr) {
        var len = arr.length;
        if (len < 32) {
            this.reserve(1 + len);
            this.buf[this.ofs++] = 160 /* Array0 */ + len;
            if (len == 0)
                return;
        }
        else {
            this.writeInternalVarLenInt(len - 32, 208 /* Array1 */);
        }
        for (var i = 0; i < len; i++) {
            this.writeAny(arr[i]);
        }
    };
    Encoder.prototype.writeObject = function (obj) {
        var keys = Object.keys(obj);
        var len = keys.length;
        if (len < 32) {
            this.reserve(1 + len);
            this.buf[this.ofs++] = 128 /* Map0 */ + len;
            if (len == 0)
                return;
        }
        else {
            this.writeInternalVarLenInt(len - 32, 204 /* Map1 */);
        }
        for (var i = 0; i < len; i++) {
            var key = keys[i];
            this.writeString(key);
            this.writeAny(obj[key]);
        }
    };
    Encoder.prototype.writeAny = function (a) {
        if (a == null) {
            this.writeNull();
            return;
        }
        var to = typeof a;
        if (to === "string") {
            this.writeString(a);
        }
        else if (to === "number") {
            this.writeNumber(a);
        }
        else if (to === "boolean") {
            if (a === true)
                this.writeTrue();
            else
                this.writeFalse();
        }
        else if (Array.isArray(a)) {
            this.writeArray(a);
        }
        else {
            this.writeObject(a);
        }
    };
    Encoder.prototype.writeEndOfBlock = function () {
        this.reserve(1);
        this.buf[this.ofs++] = 216 /* EndOfBlock */;
    };
    return Encoder;
}());
exports.Encoder = Encoder;
