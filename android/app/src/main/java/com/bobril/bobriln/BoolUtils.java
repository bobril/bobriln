package com.bobril.bobriln;

public final class BoolUtils {
    public static boolean parseBool(Object value) {
        if (value instanceof String) {
            String str = (String)value;
            return str.equals("1") || str.equals("true");
        }
        if (value instanceof Boolean) {
            return (boolean)value;
        }
        throw new RuntimeException("Cannot parse " + value.toString() + " to boolean");
    }
}
