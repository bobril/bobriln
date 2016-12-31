package com.bobril.bobriln;

public class StringUtils {
    static public boolean parseString(String str, int[] posref, String what) {
        int pos = posref[0];
        int poswhat = 0;
        while (pos < str.length() && poswhat < what.length()) {
            if (str.charAt(pos) != what.charAt(poswhat)) return false;
            pos++;
            poswhat++;
        }
        if (poswhat < what.length()) return false;
        posref[0] = pos;
        return true;
    }

    static public void skipWhiteSpace(String str, int[] posref) {
        int pos = posref[0];
        while (pos < str.length()) {
            char ch = str.charAt(pos);
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\u00A0') {
                pos++;
            } else break;
        }
        posref[0] = pos;
    }

    static public boolean isEOS(String str, int[] posref) {
        return posref[0] >= str.length();
    }

    static public double parseDouble(String str, int[] posref) {
        int pos = posref[0];
        double value = 0;
        double exp = 1;
        boolean wasDecimalPoint = false;
        final int strlen = str.length();
        if (pos < strlen) {
            char ch = str.charAt(pos);
            if (ch == '-') {
                exp = -exp;
                pos++;
            } else if (ch == '+') {
                pos++;
            }
        }
        if (pos < strlen) {
            char ch = str.charAt(pos);
            if ((ch < '0' || ch > '9') && ch!='.') return Double.NaN;
        } else return Double.NaN;
        while (pos < strlen) {
            char ch = str.charAt(pos);
            pos++;
            if (ch >= '0' && ch <= '9') {
                value = value * 10 + (ch - '0');
                if (wasDecimalPoint) exp *= 0.1;
            } else if (!wasDecimalPoint && ch == '.') {
                wasDecimalPoint = true;
            } else if (ch == 'e' || ch == 'E') {
                int exp2 = 0;
                boolean negativeExponent = false;
                if (pos < strlen) {
                    ch = str.charAt(pos);
                    if (ch == '-') {
                        negativeExponent = true;
                        pos++;
                    } else if (ch == '+') {
                        pos++;
                    }
                }
                while (pos < strlen) {
                    ch = str.charAt(pos);
                    if (ch >= '0' && ch <= '9') {
                        if (exp2 >= 100) {
                            if (negativeExponent) return 0;
                            if (exp > 0) return Double.POSITIVE_INFINITY;
                            return Double.NEGATIVE_INFINITY;
                        }
                        exp2 = exp2 * 10 + (ch - '0');
                        pos++;
                    } else {
                        break;
                    }
                }
                if (negativeExponent) {
                    while (exp2-- > 0) {
                        exp *= 0.1;
                    }
                } else {
                    while (exp2-- > 0) {
                        exp *= 10;
                    }
                }
                break;
            } else {
                pos--;
                break;
            }
        }
        posref[0] = pos;
        return value * exp;
    }
}
