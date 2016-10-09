package com.bobril.bobriln;

public final class IntUtils {
    public static int parseInt(Object value) {
        if (value instanceof String) {
            String str = (String)value;
            return Integer.parseInt(str);
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        if (value instanceof Float) {
            return ((Float) value).intValue();
        }
        throw new RuntimeException("Cannot parse " + value.toString() + " to int");
    }
}
