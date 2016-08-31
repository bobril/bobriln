package com.bobril.bobriln;

public class StringUtils {
    public static final long MAX_VALUE_DIVIDE_10 = Long.MAX_VALUE / 10;

    static double asDouble(long value, int exp, boolean negative, int decimalPlaces) {
        if (decimalPlaces > 0 && value < Long.MAX_VALUE / 2) {
            if (value < Long.MAX_VALUE / (1L << 32)) {
                exp -= 32;
                value <<= 32;
            }
            if (value < Long.MAX_VALUE / (1L << 16)) {
                exp -= 16;
                value <<= 16;
            }
            if (value < Long.MAX_VALUE / (1L << 8)) {
                exp -= 8;
                value <<= 8;
            }
            if (value < Long.MAX_VALUE / (1L << 4)) {
                exp -= 4;
                value <<= 4;
            }
            if (value < Long.MAX_VALUE / (1L << 2)) {
                exp -= 2;
                value <<= 2;
            }
            if (value < Long.MAX_VALUE / (1L << 1)) {
                exp -= 1;
                value <<= 1;
            }
        }
        for (; decimalPlaces > 0; decimalPlaces--) {
            exp--;
            long mod = value % 5;
            value /= 5;
            int modDiv = 1;
            if (value < Long.MAX_VALUE / (1L << 4)) {
                exp -= 4;
                value <<= 4;
                modDiv <<= 4;
            }
            if (value < Long.MAX_VALUE / (1L << 2)) {
                exp -= 2;
                value <<= 2;
                modDiv <<= 2;
            }
            if (value < Long.MAX_VALUE / (1L << 1)) {
                exp -= 1;
                value <<= 1;
                modDiv <<= 1;
            }
            value += modDiv * mod / 5;
        }
        final double d = Math.scalb((double) value, exp);
        return negative ? -d : d;
    }

    static public boolean parseString(String str, int[] posref, String what) {
        int pos = posref[0];
        int poswhat = 0;
        while (pos<str.length() && poswhat<what.length()) {
            if (str.charAt(pos)!=what.charAt(poswhat)) return false;
            pos++; poswhat++;
        }
        if (poswhat<what.length()) return false;
        posref[0] = pos;
        return true;
    }

    static public void skipWhiteSpace(String str, int[] posref) {
        int pos = posref[0];
        while (pos<str.length()) {
            char ch = str.charAt(pos);
            if (ch==' ' || ch=='\t' || ch=='\n' || ch=='\r' || ch=='\u00A0') {
                pos++;
            } else break;
        }
        posref[0] = pos;
    }

    static public boolean isEOS(String str, int[] posref) {
        return posref[0]>=str.length();
    }

    static public double parseDouble(String str, int[] posref) {
        int pos = posref[0];
        long value = 0;
        int exp = 0;
        boolean negative = false;
        int decimalPlaces = Integer.MIN_VALUE;
        while (pos<str.length()) {
            char ch = str.charAt(pos);
            pos++;
            if (ch >= '0' && ch <= '9') {
                while (value >= MAX_VALUE_DIVIDE_10) {
                    value >>>= 1;
                    exp++;
                }
                value = value * 10 + (ch - '0');
                decimalPlaces++;
            } else if (ch == '-') {
                negative = true;
            } else if (ch == '.') {
                decimalPlaces = 0;
            } else {
                pos--;
                break;
            }
        }
        posref[0] = pos;
        return asDouble(value, exp, negative, decimalPlaces);
    }
}
