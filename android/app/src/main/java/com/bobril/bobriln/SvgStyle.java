package com.bobril.bobriln;

import android.graphics.Color;
import android.graphics.Paint;

public class SvgStyle {
    static final int F_STROKE_COLOR = 1;
    static final int F_FILL_COLOR = 2;
    static final int F_STROKE_WIDTH = 4;

    int flags;
    int strokeColor;
    int fillColor;
    float strokeWidth;
    Paint strokePaint;
    Paint fillPaint;

    SvgStyle() {
        flags = 0;
        strokeWidth = 0;
        strokeColor = 0;
        fillColor = 0;
    }

    public boolean setStyle(String styleName, Object styleValue) {
        if (styleName.equals("stroke")) {
            if (styleValue == null) {
                flags &= ~F_STROKE_COLOR;
            } else {
                strokeColor = ColorUtils.toColor(styleValue);
                flags |= F_STROKE_COLOR;
            }
        } else if (styleName.equals("strokeWidth")) {
            if (styleValue == null) {
                flags &= ~F_STROKE_WIDTH;
            } else {
                strokeWidth = FloatUtils.parseFloat(styleValue);
                flags |= F_STROKE_WIDTH;
            }
        } else if (styleName.equals("fill")) {
            if (styleValue == null) {
                flags &= ~F_FILL_COLOR;
            } else {
                fillColor = ColorUtils.toColor(styleValue);
                flags |= F_FILL_COLOR;
            }
        } else {
            return false;
        }
        return true;
    }

    public void ReadInherited(SvgStyle merge) {
        int newFlags = ~flags;
        if ((newFlags & F_FILL_COLOR)!=0) {
            this.fillColor = merge.fillColor;
        }
        if ((newFlags & F_STROKE_COLOR)!=0) {
            this.strokeColor = merge.strokeColor;
        }
        if ((newFlags & F_STROKE_WIDTH)!=0) {
            this.strokeWidth = merge.strokeWidth;
        }
    }

    public void AddDefaults() {
        int newFlags = ~flags;
        if ((newFlags & F_FILL_COLOR)!=0) {
            this.fillColor = Color.TRANSPARENT;
        }
        if ((newFlags & F_STROKE_COLOR)!=0) {
            this.strokeColor = Color.TRANSPARENT;
        }
        if ((newFlags & F_STROKE_WIDTH)!=0) {
            this.strokeWidth = 1;
        }
    }

    public void update() {
        if (fillColor==Color.TRANSPARENT) {
            fillPaint = null;
        } else {
            if (fillPaint==null) {
                fillPaint = new Paint();
                fillPaint.setAntiAlias(true);
                fillPaint.setStyle(Paint.Style.FILL);
            }
            fillPaint.setColor(fillColor);
        }
        if (strokeColor==Color.TRANSPARENT || strokeWidth==0) {
            strokePaint = null;
        } else {
            if (strokePaint==null) {
                strokePaint = new Paint();
                strokePaint.setAntiAlias(true);
                strokePaint.setStyle(Paint.Style.STROKE);
            }
            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(strokeWidth);
        }
    }
}
