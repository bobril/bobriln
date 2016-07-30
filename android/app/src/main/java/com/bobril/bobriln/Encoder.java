package com.bobril.bobriln;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Encoder {
    private StringBuilder _sb;
    private ByteBuffer _bbuf;

    public Encoder() {
        _sb = new StringBuilder();
        _bbuf = ByteBuffer.allocate(8);
        _bbuf.order(ByteOrder.LITTLE_ENDIAN);
    }

    public String toLatin1String() {
        return _sb.toString();
    }

    public void reset() {
        _sb.setLength(0);
    }

    private void writeInternalVarLenInt(int n, EncodedType encType) {
        if (n < 256) {
            _sb.append((char) encType.val);
            _sb.append((char) n);
            return;
        }
        n -= 256;
        if (n < 65536) {
            _sb.append((char) (encType.val + 1));
            _sb.append((char) (n >>> 8));
            _sb.append((char) (n & 255));
            return;
        }
        n -= 65536;
        if (n < 0x1000000) {
            _sb.append((char) (encType.val + 2));
            _sb.append((char) (n >>> 16));
            _sb.append((char) ((n >>> 8) & 255));
            _sb.append((char) (n & 255));
            return;
        }
        n -= 0x1000000;
        _sb.append((char) (encType.val + 3));
        _sb.append((char) (n >>> 24));
        _sb.append((char) ((n >>> 16) & 255));
        _sb.append((char) ((n >>> 8) & 255));
        _sb.append((char) (n & 255));
    }

    private void writeUInt(int n) {
        if (n < 64) {
            _sb.append((char) n);
            return;
        }
        writeInternalVarLenInt(n - 64, EncodedType.PositiveInt1);
    }

    private void writeNegativeInt(int n) {
        if (n >= -32) {
            _sb.append((char) (256 + n));
            return;
        }
        writeInternalVarLenInt(-n - 33, EncodedType.NegativeInt1);
    }

    public void writeNumber(int n) {
        if (n >= 0) writeUInt(n);
        else writeNegativeInt(n);
    }

    public void writeNumber(double n) {
        int nn = (int) n;
        if (nn == n) {
            writeNumber(nn);
            return;
        }
        _sb.append((char) EncodedType.Double.val);
        _bbuf.putDouble(0, n);
        for (int i = 0; i < 8; i++)
            _sb.append((char) (255 & _bbuf.get(i)));
    }

    public void writeString(String s) {
        int len = s.length();
        if (len < 64) {
            _sb.append((char) (EncodedType.String0.val + len));
            if (len == 0) return;
        } else {
            this.writeInternalVarLenInt(len - 64, EncodedType.String1);
        }
        for (int i = 0; i < len; i++) {
            int n = s.charAt(i);
            if (n < 0x80) {
                _sb.append((char) n);
            } else if (n < 0x4000) {
                _sb.append((char) (0x80 + (n >>> 8)));
                _sb.append((char) (n & 255));
            } else {
                _sb.append((char) 0xC0);
                _sb.append((char) (n >>> 8));
                _sb.append((char) (n & 255));
            }
        }
    }

    public void writeNull() {
        _sb.append((char) EncodedType.Null.val);
    }

    public void writeTrue() {
        _sb.append((char) EncodedType.True.val);
    }

    public void writeFalse() {
        _sb.append((char) EncodedType.False.val);
    }

    public void writeArray(List<Object> arr) {
        int len = arr.size();
        if (len < 32) {
            _sb.append((char) (EncodedType.Array0.val + len));
            if (len == 0) return;
        } else {
            writeInternalVarLenInt(len - 32, EncodedType.Array1);
        }
        for (int i = 0; i < len; i++) {
            writeAny(arr.get(i));
        }
    }

    public void writeObject(Map<String, Object> obj) {
        int len = obj.size();
        if (len < 32) {
            _sb.append((char) (EncodedType.Map0.val + len));
            if (len == 0) return;
        } else {
            writeInternalVarLenInt(len - 32, EncodedType.Map1);
        }
        for (Map.Entry<String, Object> e : obj.entrySet()) {
            writeString(e.getKey());
            writeAny(e.getValue());
        }
    }

    public void writeAny(Object a) {
        if (a == null) {
            this.writeNull();
            return;
        }
        if (a instanceof String) {
            this.writeString((String) a);
            return;
        }
        if (a instanceof Integer) {
            this.writeNumber((Integer) a);
            return;
        }
        if (a instanceof Double) {
            this.writeNumber((Double) a);
            return;
        }
        if (a instanceof Float) {
            this.writeNumber((Float) a);
            return;
        }
        if (a instanceof Boolean) {
            if ((Boolean) a) writeTrue();
            else writeFalse();
            return;
        }
        if (a instanceof List) {
            this.writeArray((List<Object>)a);
            return;
        }
        if (a instanceof Object[]) {
            this.writeArray(Arrays.asList((Object[]) a));
            return;
        }
        if (a instanceof Map) {
            this.writeObject((Map<String, Object>) a);
            return;
        }
        throw new Error("Don't know how to write " + a.getClass().getSimpleName());
    }

    public void writeEndOfBlock() {
        _sb.append((char) EncodedType.EndOfBlock.val);
    }
}
