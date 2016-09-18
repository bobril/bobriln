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
}
