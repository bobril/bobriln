package com.bobril.bobriln;

import android.graphics.Color;

public class TextStyle {
    static final int F_ITALIC = 1;
    static final int F_BOLD = 2;
    static final int F_UNDERLINE = 4;

    static final int F_SIMPLE_MASK = 255;

    static final int F_COLOR = 256;
    static final int F_BACKGROUND = 512;
    static final int F_FONT_SIZE = 1024;

    public int flags;
    public int simple;
    public int color;
    public int background;
    public float fontSize;

    static TextStyle setStyle(TextStyle current, String styleName, Object styleValue) {
        switch (styleName) {
            case "color": {
                if (current==null) {
                    if (styleValue!=null) current = new TextStyle(); else return null;
                }
                if (styleValue==null) {
                    current.flags &= ~F_COLOR;
                } else {
                    current.flags |= F_COLOR;
                    current.color = ColorUtils.toColor(styleValue);
                }
                return current;
            }
            case "backgroundColor": {
                if (current==null) {
                    if (styleValue!=null) current = new TextStyle(); else return null;
                }
                if (styleValue==null) {
                    current.flags &= ~F_BACKGROUND;
                } else {
                    current.flags |= F_BACKGROUND;
                    current.background = ColorUtils.toColor(styleValue);
                }
                return current;
            }
            case "textDecoration": {
                if (current==null) {
                    if (styleValue!=null) current = new TextStyle(); else return null;
                }
                if (styleValue==null || !styleValue.equals("underline")) {
                    current.flags &= ~F_UNDERLINE;
                } else {
                    current.flags |= F_UNDERLINE;
                    current.simple |= F_UNDERLINE;
                }
                return current;
            }
            case "fontStyle": {
                if (current==null) {
                    if (styleValue!=null) current = new TextStyle(); else return null;
                }
                if (styleValue==null) {
                    current.flags &= ~F_ITALIC;
                } else {
                    current.flags |= F_ITALIC;
                    if (styleValue.equals("italic"))
                        current.simple |= F_ITALIC;
                    else
                        current.simple &= ~F_ITALIC;
                }
                return current;
            }
            case "fontWeight": {
                if (current==null) {
                    if (styleValue!=null) current = new TextStyle(); else return null;
                }
                if (styleValue==null) {
                    current.flags &= ~F_BOLD;
                } else {
                    current.flags |= F_BOLD;
                    if (styleValue.equals("bold"))
                        current.simple |= F_BOLD;
                    else
                        current.simple &= ~F_BOLD;
                }
                return current;
            }
            case "fontSize": {
                if (current==null) {
                    if (styleValue!=null) current = new TextStyle(); else return null;
                }
                if (styleValue==null) {
                    current.flags &= ~F_FONT_SIZE;
                } else {
                    current.flags |= F_FONT_SIZE;
                    current.fontSize = FloatUtils.parseFloat(styleValue);
                }
                return current;
            }
        }
        return current;
    }

    public int ReadInherited(TextStyle merge, int fixedFlags) {
        int mergedFlags = fixedFlags | merge.flags;
        int newFlags = mergedFlags ^ fixedFlags;
        if ((newFlags & F_BACKGROUND)!=0) {
            this.background = merge.background;
        }
        if ((newFlags & F_COLOR)!=0) {
            this.color = merge.color;
        }
        if ((newFlags & F_FONT_SIZE)!=0) {
            this.fontSize = merge.fontSize;
        }
        if ((newFlags & F_SIMPLE_MASK)!=0) {
            this.simple &= ~(newFlags & F_SIMPLE_MASK);
            this.simple |= merge.simple & newFlags & F_SIMPLE_MASK;
        }
        return mergedFlags;
    }

    public void AddDefaults(int fixedFlags) {
        int newFlags = ~fixedFlags;
        if ((newFlags & F_BACKGROUND)!=0) {
            this.background = Color.TRANSPARENT;
        }
        if ((newFlags & F_COLOR)!=0) {
            this.color = Color.WHITE;
        }
        if ((newFlags & F_FONT_SIZE)!=0) {
            this.fontSize = 12;
        }
        if ((newFlags & F_SIMPLE_MASK)!=0) {
            this.simple &= ~(newFlags & F_SIMPLE_MASK);
        }
        flags = -1;
    }
}
