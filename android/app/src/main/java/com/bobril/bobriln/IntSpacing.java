package com.bobril.bobriln;

import com.facebook.csslayout.Spacing;

public class IntSpacing {
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int vertical;
    private int horizontal;
    private int start;
    private int end;
    private int all;
    private int flags;

    int getRaw(int pos) {
        switch (pos) {
            case Spacing.LEFT:
                return left;
            case Spacing.TOP:
                return top;
            case Spacing.RIGHT:
                return right;
            case Spacing.BOTTOM:
                return bottom;
            case Spacing.VERTICAL:
                return vertical;
            case Spacing.HORIZONTAL:
                return horizontal;
            case Spacing.START:
                return start;
            case Spacing.END:
                return end;
            case Spacing.ALL:
                return all;
            default:
                throw new RuntimeException("invalid pos in getRaw");
        }
    }

    int get(int pos, boolean rtl, int dflt) {
        switch (pos) {
            case Spacing.LEFT: {
                if ((flags & (1 << Spacing.START)) != 0 && !rtl) return start;
                if ((flags & (1 << Spacing.END)) != 0 && rtl) return end;
                if ((flags & (1 << Spacing.LEFT)) != 0) return left;
                if ((flags & (1 << Spacing.HORIZONTAL)) != 0) return horizontal;
                if ((flags & (1 << Spacing.ALL)) != 0) return all;
                return dflt;
            }
            case Spacing.RIGHT: {
                if ((flags & (1 << Spacing.END)) != 0 && !rtl) return end;
                if ((flags & (1 << Spacing.START)) != 0 && rtl) return start;
                if ((flags & (1 << Spacing.RIGHT)) != 0) return right;
                if ((flags & (1 << Spacing.HORIZONTAL)) != 0) return horizontal;
                if ((flags & (1 << Spacing.ALL)) != 0) return all;
                return dflt;
            }
            case Spacing.TOP: {
                if ((flags & (1 << Spacing.TOP)) != 0) return top;
                if ((flags & (1 << Spacing.VERTICAL)) != 0) return vertical;
                if ((flags & (1 << Spacing.ALL)) != 0) return all;
                return dflt;
            }
            case Spacing.BOTTOM: {
                if ((flags & (1 << Spacing.BOTTOM)) != 0) return bottom;
                if ((flags & (1 << Spacing.VERTICAL)) != 0) return vertical;
                if ((flags & (1 << Spacing.ALL)) != 0) return all;
                return dflt;
            }
            default:
                throw new RuntimeException("invalid pos in get");
        }
    }

    void clear() {
        flags = 0;
    }

    boolean anyset() {
        return flags!=0;
    }

    void unset(int pos) {
        flags = flags & ~(1 << pos);
    }

    void set(int pos, int value) {
        flags = flags | (1 << pos);
        switch (pos) {
            case Spacing.LEFT:
                left = value;
                break;
            case Spacing.TOP:
                top = value;
                break;
            case Spacing.RIGHT:
                right = value;
                break;
            case Spacing.BOTTOM:
                bottom = value;
                break;
            case Spacing.VERTICAL:
                vertical = value;
                break;
            case Spacing.HORIZONTAL:
                horizontal = value;
                break;
            case Spacing.START:
                start = value;
                break;
            case Spacing.END:
                end = value;
                break;
            case Spacing.ALL:
                all = value;
                break;
            default:
                throw new RuntimeException("invalid pos in set");
        }
    }
}
