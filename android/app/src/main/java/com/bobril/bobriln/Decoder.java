package com.bobril.bobriln;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

enum EncodedType {
    Number0(0),
    Number63(63),
    String0(64),
    String63(127),
    Map0(128),
    Map31(159),
    Array0(160),
    Array31(191),
    PositiveInt1(192),
    PositiveInt4(195),
    NegativeInt1(196),
    NegativeInt4(199),
    String1(200),
    String4(203),
    Map1(204),
    Map4(207),
    Array1(208),
    Array4(211),
    Double(212),
    False(213),
    True(214),
    Null(215),
    EndOfBlock(216),
    NumberM32(224),
    NumberM1(255);

    public final int val;

    EncodedType(int param) {
        this.val = param;
    }
}

enum PeekType {
    Int,
    Double,
    String,
    Boolean,
    Null,
    ArrayStart,
    ArrayEnd,
    MapStart,
    MapEnd,
    EndOfBlock,
    EOF
}

enum StateType {
    Array,
    MapKey,
    MapValue
}

public class Decoder {
    byte[] _buf;
    ByteBuffer _bbuf;
    int[] _stack;
    int _stackPos;
    PeekType _peekType;
    String _peekString;
    StringBuilder _sb;
    int _peekInt;
    double _peekDouble;
    boolean _peekBoolean;

    public Decoder() {
        this._stack = new int[4];
        this._stackPos = 0;
        this._sb = new StringBuilder();
    }

    public void initFromString(String value) {
        int len = value.length();
        if (_buf == null || _buf.length < len) _buf = new byte[len];
        for (int i = 0; i < len; i++) {
            _buf[i] = (byte) value.charAt(i);
        }
        _bbuf = ByteBuffer.wrap(_buf, 0, len);
        _bbuf.order(ByteOrder.LITTLE_ENDIAN);
        this._stackPos = 0;
        this._peekType = PeekType.EOF;
        this._peekString = null;
        this.next();
    }

    private void setPeek(PeekType type, String lit, int ofs) {
        this._peekType = type;
        this._peekString = lit;
        this._bbuf.position(ofs);
    }

    private void setPeek(PeekType type, int lit, int ofs) {
        this._peekType = type;
        this._peekInt = lit;
        this._peekString = null;
        this._bbuf.position(ofs);
    }

    private void setPeek(PeekType type, double lit, int ofs) {
        this._peekType = type;
        this._peekDouble = lit;
        this._peekString = null;
        this._bbuf.position(ofs);
    }

    private void setPeek(PeekType type, boolean lit, int ofs) {
        this._peekType = type;
        this._peekBoolean = lit;
        this._peekString = null;
        this._bbuf.position(ofs);
    }

    private void throwEOF() throws Error {
        throw new Error("EOF too early");
    }

    private void parseString(int strLen, int ofs) {
        StringBuilder sb = this._sb;
        sb.setLength(0);
        ByteBuffer buf = this._bbuf;
        int len = buf.limit();
        while (strLen-- > 0) {
            if (ofs >= len) this.throwEOF();
            int b = 255 & buf.get(ofs++);
            if (b < 0x80) {
                sb.append((char) b);
            } else if (b < 0xc0) {
                if (ofs >= len) this.throwEOF();
                sb.append((char) (((b - 0x80) << 8) + (255 & buf.get(ofs++))));
            } else {
                if (ofs + 1 >= len) this.throwEOF();
                sb.append((char) (((255 & buf.get(ofs)) << 8) + (255 & buf.get(ofs + 1))));
                ofs += 2;
            }
        }
        this.setPeek(PeekType.String, sb.toString(), ofs);
    }

    private int readVarLenInt(int lenBytesM1, int ofs) {
        ByteBuffer buf = this._bbuf;
        if (lenBytesM1 == 0) {
            buf.position(ofs + 1);
            return 255 & buf.get(ofs);
        }
        if (lenBytesM1 == 1) {
            buf.position(ofs + 2);
            return 256 + ((255 & buf.get(ofs)) << 8) + (255 & buf.get(ofs + 1));
        }
        if (lenBytesM1 == 2) {
            buf.position(ofs + 3);
            return 0x10100 + ((255 & buf.get(ofs)) << 16) + ((255 & buf.get(ofs + 1)) << 8) + (255 & buf.get(ofs + 2));
        }
        buf.position(ofs + 4);
        return 0x1010100 + ((255 & buf.get(ofs)) << 24) + ((255 & buf.get(ofs + 1)) << 16) + ((255 & buf.get(ofs + 2)) << 8) + (255 & buf.get(ofs + 3));
    }

    public void next() {
        switch (this._peekType) {
            case ArrayStart: {
                if (this._stack[this._stackPos - 1]-- == 0) {
                    this._peekType = PeekType.ArrayEnd;
                    this._stackPos -= 2;
                    return;
                }
                break;
            }
            case MapStart: {
                if (this._stack[this._stackPos - 1]-- == 0) {
                    this._peekType = PeekType.MapEnd;
                    this._stackPos -= 2;
                    return;
                }
                break;
            }
            default: {
                int[] s = this._stack;
                int sl = this._stackPos;
                if (sl > 0) {
                    if (s[sl - 2] == StateType.Array.ordinal()) {
                        if (s[sl - 1]-- == 0) {
                            this._peekType = PeekType.ArrayEnd;
                            this._peekString = null;
                            this._stackPos -= 2;
                            return;
                        }
                    } else if (s[sl - 2] == StateType.MapKey.ordinal()) {
                        s[sl - 2] = StateType.MapValue.ordinal();
                    } else {
                        s[sl - 2] = StateType.MapKey.ordinal();
                        if (s[sl - 1]-- == 0) {
                            this._peekType = PeekType.MapEnd;
                            this._peekString = null;
                            this._stackPos -= 2;
                            return;
                        }
                    }
                }
                break;
            }
        }
        ByteBuffer buf = this._bbuf;
        int ofs = buf.position();
        int len = buf.limit();
        if (ofs >= len) {
            this.setPeek(PeekType.EOF, null, ofs);
            return;
        }
        int b = 255 & buf.get(ofs++);
        if (b <= EncodedType.Number63.val) {
            this.setPeek(PeekType.Int, b, ofs);
            return;
        }
        if (b >= EncodedType.NumberM32.val) {
            this.setPeek(PeekType.Int, b - 256, ofs);
            return;
        }
        if (b <= EncodedType.String63.val) {
            int l = b - EncodedType.String0.val;
            if (l == 0) {
                this.setPeek(PeekType.String, "", ofs);
                return;
            }
            this.parseString(l, ofs);
            return;
        }
        if (b <= EncodedType.Map31.val) {
            this.stackPush(StateType.MapKey, b - EncodedType.Map0.val);
            this.setPeek(PeekType.MapStart, null, ofs);
            return;
        }
        if (b <= EncodedType.Array31.val) {
            this.stackPush(StateType.Array, b - EncodedType.Array0.val);
            this.setPeek(PeekType.ArrayStart, null, ofs);
            return;
        }
        switch (b) {
            case 192 /* EncodedType.PositiveInt1 */:
            case 193 /* EncodedType.PositiveInt1 + 1 */:
            case 194 /* EncodedType.PositiveInt1 + 2 */:
            case 195 /* EncodedType.PositiveInt4 */: {
                this._peekType = PeekType.Int;
                this._peekInt = 64 + this.readVarLenInt(b - EncodedType.PositiveInt1.val, ofs);
                break;
            }
            case 196 /* EncodedType.NegativeInt1 */:
            case 197 /* EncodedType.NegativeInt1 + 1 */:
            case 198 /* EncodedType.NegativeInt1 + 2 */:
            case 199 /* EncodedType.NegativeInt4 */: {
                this._peekType = PeekType.Int;
                this._peekInt = -33 - this.readVarLenInt(b - EncodedType.NegativeInt1.val, ofs);
                break;
            }
            case 200 /* EncodedType.String1 */:
            case 201 /* EncodedType.String1 + 1 */:
            case 202 /* EncodedType.String1 + 2 */:
            case 203 /* EncodedType.String4 */: {
                int l = 64 + this.readVarLenInt(b - EncodedType.String1.val, ofs);
                this.parseString(l, this._bbuf.position());
                break;
            }
            case 204 /* EncodedType.Map1 */:
            case 205 /* EncodedType.Map1 + 1 */:
            case 206 /* EncodedType.Map1 + 2 */:
            case 207 /* EncodedType.Map4 */: {
                int l = 32 + this.readVarLenInt(b - EncodedType.Map1.val, ofs);
                this.stackPush(StateType.MapKey, l);
                this._peekType = PeekType.MapStart;
                break;
            }
            case 208 /* EncodedType.Array1 */:
            case 209 /* EncodedType.Array1 + 1 */:
            case 210 /* EncodedType.Array1 + 2 */:
            case 211 /* EncodedType.Array4 */: {
                int l = 32 + this.readVarLenInt(b - EncodedType.Array1.val, ofs);
                this.stackPush(StateType.Array, l);
                this._peekType = PeekType.ArrayStart;
                break;
            }
            case 212 /* EncodedType.Double */: {
                this.setPeek(PeekType.Double, buf.getDouble(ofs), ofs + 8);
                break;
            }
            case 213 /* EncodedType.False */: {
                this.setPeek(PeekType.Boolean, false, ofs);
                break;
            }
            case 214 /* EncodedType.True */: {
                this.setPeek(PeekType.Boolean, true, ofs);
                break;
            }
            case 215 /* EncodedType.Null */: {
                this.setPeek(PeekType.Null, null, ofs);
                break;
            }
            case 216 /* EncodedType.EndOfBlock */: {
                this.setPeek(PeekType.EndOfBlock, null, ofs);
                break;
            }
            default:
                throw new Error("Corrupted");
        }
    }

    private void stackPush(StateType type, int idx) {
        if (this._stackPos + 2 > this._stack.length) {
            this._stack = Arrays.copyOf(this._stack, this._stack.length * 2);
        }
        this._stack[this._stackPos++] = type.ordinal();
        this._stack[this._stackPos++] = idx;
    }

    public boolean isAnyEnd() {
        return (this._peekType == PeekType.EOF) || (this._peekType == PeekType.EndOfBlock);
    }

    public boolean isEOF() {
        return this._peekType == PeekType.EOF;
    }

    public int readInt() {
        if (_peekType != PeekType.Int)
            throw new Error("Cannot read int from " + _peekType.name());
        int res = _peekInt;
        next();
        return res;
    }

    public double readDouble() {
        if (_peekType != PeekType.Int && _peekType != PeekType.Double)
            throw new Error("Cannot read double from " + _peekType.name());
        double res = _peekType == PeekType.Double ? _peekDouble : _peekInt;
        next();
        return res;
    }

    public boolean readBoolean() {
        if (_peekType != PeekType.Boolean)
            throw new Error("Cannot read boolean from " + _peekType.name());
        boolean res = _peekBoolean;
        next();
        return res;
    }

    public String readString() {
        if (_peekType != PeekType.String)
            throw new Error("Cannot read string from " + _peekType.name());
        String res = _peekString;
        next();
        return res;
    }

    public Object readAny() {
        switch (_peekType) {
            case EOF:
            case EndOfBlock: {
                throwEOF();
            }
            case Boolean: {
                Object res = _peekBoolean ? Boolean.TRUE : Boolean.FALSE;
                next();
                return res;
            }
            case Null: {
                next();
                return null;
            }
            case Int: {
                Object res = _peekInt;
                next();
                return res;
            }
            case Double: {
                Object res = _peekDouble;
                next();
                return res;
            }
            case String: {
                Object res = _peekString;
                next();
                return res;
            }
            case ArrayEnd:
            case MapEnd: {
                throw new Error("Reading something end");
            }
            case ArrayStart: {
                List<Object> res = new ArrayList<Object>();
                next();
                while (_peekType != PeekType.ArrayEnd) {
                    res.add(readAny());
                }
                next();
                return res;
            }
            case MapStart: {
                HashMap<String, Object> res = new HashMap<>();
                next();
                while (_peekType != PeekType.MapEnd) {
                    if (_peekType != PeekType.String)
                        throw new Error("Object key is not string");
                    String key = _peekString;
                    _peekString = null;
                    next();
                    res.put(key, readAny());
                }
                next();
                return res;
            }
            default:
                throw new Error("Unexpected peekType " + _peekType.name());
        }
    }
}
