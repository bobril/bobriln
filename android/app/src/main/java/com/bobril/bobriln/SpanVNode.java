package com.bobril.bobriln;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

public class SpanVNode extends ReplacementSpan {
    VNodeViewBased node;
    int offset;
    int x, y;
    int w, h;
    int verticalAlign; // 0-Bottom, 1-Center, 2-Top, 3-Baseline
    int verticalMove; // Only for Baseline

    public SpanVNode(VNodeViewBased node, int offset) {
        this.node = node;
        this.offset = offset;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        int height = Math.round(node.css.getLayoutHeight());
        int width = Math.round(node.css.getLayoutWidth());
        this.w = width;
        this.h = height;
        float density = node.vdom.density;
        Object valObj = node.getStyle().get("verticalAlign");
        verticalAlign = 3;
        if (valObj != null) {
            if (valObj instanceof String) {
                String valStr = (String) valObj;
                switch (valStr) {
                    case "bottom":
                        verticalAlign = 0;
                        break;
                    case "top":
                        verticalAlign = 2;
                        break;
                    case "center":
                        verticalAlign = 1;
                        break;
                    case "baseline":
                        verticalAlign = 3;
                        break;
                    default:
                        verticalMove = Math.round(Float.parseFloat(valStr) * density);
                }
            } else {
                verticalMove = Math.round(FloatUtils.unboxToFloat(valObj) * density);
            }
        }
        if (fm != null) {
            int oldHeight = fm.descent - fm.ascent;
            switch (verticalAlign) {
                case 0: // bottom
                {
                    if (height > oldHeight) {
                        fm.ascent -= height - oldHeight;
                    }
                    break;
                }
                case 1: // center
                {
                    if (height > oldHeight) {
                        int add = (height - oldHeight) / 2;
                        int add2 = height - oldHeight - add;
                        fm.ascent -= add2;
                        fm.descent += add;
                    }
                    break;
                }
                case 2: // top
                {
                    if (height > oldHeight) {
                        fm.descent += height - oldHeight;
                    }
                    break;
                }
                case 3: // baseline
                {
                    int top = 0;
                    int bottom = 0;
                    if (verticalMove >= 0) {
                        top = verticalMove + height;
                    } else {
                        bottom = -verticalMove;
                        top = height + verticalMove;
                        if (top < 0) top = 0;
                    }
                    if (top > -fm.ascent) fm.ascent = -top;
                    if (bottom > fm.descent) fm.descent = bottom;
                    break;
                }
            }
            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }
        return width;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
    }

    public void calcPos(int top, int bottom, int descent, float x) {
        int height = Math.round(node.css.getLayoutHeight());
        float transY = 0;
        switch (verticalAlign) {
            case 0: // bottom
                transY = bottom - height;
                break;
            case 1: // center
                transY = top + (bottom - top - height) / 2;
                break;
            case 2: // top
                transY = top;
                break;
            case 3: // baseline
                transY = bottom - descent - height - verticalMove;
                break;
        }
        this.x = Math.round(x);
        this.y = Math.round(transY);
    }
}
