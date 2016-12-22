package com.bobril.bobriln;

import android.graphics.Color;
import android.graphics.Paint;

public class SvgStyle {
    static final int F_STROKE_COLOR = 1;
    static final int F_FILL_COLOR = 2;
    static final int F_STROKE_WIDTH = 4;
    static final int F_STROKE_OPACITY = 8;
    static final int F_FILL_OPACITY = 16;

    int flags;
    int strokeColor;
    int fillColor;
    float fillOpacity;
    float strokeOpacity;
    float opacity;
    float finalOpacity;
    float strokeWidth;
    Paint strokePaint;
    Paint fillPaint;

    SvgStyle() {
        flags = 0;
        strokeWidth = 0;
        strokeColor = 0;
        fillColor = 0;
        opacity = 1;
        fillOpacity = 1;
        strokeOpacity = 1;
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
        } else if (styleName.equals("strokeOpacity")) {
            if (styleValue == null) {
                flags &= ~F_STROKE_OPACITY;
            } else {
                strokeOpacity = FloatUtils.parseFloat(styleValue);
                if (strokeOpacity < 0) strokeOpacity = 0;
                else if (strokeOpacity > 1) strokeOpacity = 1;
                flags |= F_STROKE_OPACITY;
            }
        } else if (styleName.equals("fillOpacity")) {
            if (styleValue == null) {
                flags &= ~F_FILL_OPACITY;
            } else {
                fillOpacity = FloatUtils.parseFloat(styleValue);
                if (fillOpacity < 0) fillOpacity = 0;
                else if (fillOpacity > 1) fillOpacity = 1;
                flags |= F_FILL_OPACITY;
            }
        } else if (styleName.equals("opacity")) {
            if (styleValue == null) {
                opacity = 1;
            } else {
                opacity = FloatUtils.parseFloat(styleValue);
                if (opacity < 0) opacity = 0;
                else if (opacity > 1) opacity = 1;
            }
        } else {
            return false;
        }
        return true;
    }

    public void ReadInherited(SvgStyle merge) {
        int newFlags = ~flags;
        if ((newFlags & F_FILL_COLOR) != 0) {
            this.fillColor = merge.fillColor;
        }
        if ((newFlags & F_STROKE_COLOR) != 0) {
            this.strokeColor = merge.strokeColor;
        }
        if ((newFlags & F_FILL_OPACITY) != 0) {
            this.fillOpacity = merge.fillOpacity;
        }
        if ((newFlags & F_STROKE_OPACITY) != 0) {
            this.strokeOpacity = merge.strokeOpacity;
        }
        if ((newFlags & F_STROKE_WIDTH) != 0) {
            this.strokeWidth = merge.strokeWidth;
        }
        finalOpacity = opacity * merge.finalOpacity;
    }

    public void AddDefaults() {
        int newFlags = ~flags;
        if ((newFlags & F_FILL_COLOR) != 0) {
            this.fillColor = Color.TRANSPARENT;
        }
        if ((newFlags & F_STROKE_COLOR) != 0) {
            this.strokeColor = Color.TRANSPARENT;
        }
        if ((newFlags & F_FILL_OPACITY) != 0) {
            this.fillOpacity = 1;
        }
        if ((newFlags & F_STROKE_OPACITY) != 0) {
            this.strokeOpacity = 1;
        }
        if ((newFlags & F_STROKE_WIDTH) != 0) {
            this.strokeWidth = 1;
        }
        finalOpacity = opacity;
    }

    public void update() {
        float fillO = finalOpacity * fillOpacity;
        if (fillColor == Color.TRANSPARENT || fillO == 0) {
            fillPaint = null;
        } else {
            if (fillPaint == null) {
                fillPaint = new Paint();
                fillPaint.setAntiAlias(true);
                fillPaint.setStyle(Paint.Style.FILL);
            }
            fillPaint.setColor(fillColor);
            if (fillO < 1) {
                fillPaint.setAlpha((int) (fillPaint.getAlpha() * fillO + 0.5));
            }
        }
        float strokeO = finalOpacity * strokeOpacity;
        if (strokeColor == Color.TRANSPARENT || strokeWidth == 0 || strokeO == 0) {
            strokePaint = null;
        } else {
            if (strokePaint == null) {
                strokePaint = new Paint();
                strokePaint.setAntiAlias(true);
                strokePaint.setStyle(Paint.Style.STROKE);
            }
            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(strokeWidth);
            if (strokeO < 1) {
                strokePaint.setAlpha((int) (strokePaint.getAlpha() * strokeO + 0.5));
            }
        }
    }
}
