package com.bobril.bobriln;

public final class FloatUtils {
    public static float unboxToFloat(Object o) {
        if (o instanceof Double) {
            return ((Double) o).floatValue();
        }
        if (o instanceof Integer) {
            return ((Integer) o).floatValue();
        }
        if (o instanceof Float) {
            return (Float) o;
        }
        throw new RuntimeException("Cannot unbox " + o.toString() + " to float");
    }

    public static float parseFloat(Object value) {
        if (value instanceof String) {
            String str = (String)value;
            return Float.parseFloat(str);
        }
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        throw new RuntimeException("Cannot parse " + value.toString() + " to float");
    }
}

