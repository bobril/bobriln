package com.bobril.bobriln;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.security.InvalidParameterException;

public class VNodeSvgPath extends VNode implements ISvgDrawable {
    Path path = new Path();
    Paint strokePaint = new Paint();
    Paint fillPaint = new Paint();

    public VNodeSvgPath() {
        super();
        strokePaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStrokeWidth(1);
        strokePaint.setAntiAlias(true);
        fillPaint.setAntiAlias(true);
    }

    @Override
    VNode createByTag(String tag) {
        throw new InvalidParameterException("Svg Path cannot have children");
    }

    @Override
    public void setAttr(String attrName, Object attrValue) {
        if (attrName.equals("d")) {
            if (!vdom.svgHelper.parsePath(attrValue.toString(), path)) {
                throw new InvalidParameterException("Cannot parse svg path: " + attrValue);
            }
            invalidate();
            return;
        }
        super.setAttr(attrName, attrValue);
    }

    @Override
    public void setStyle(String styleName, Object styleValue) {
        if (styleName.equals("stroke")) {
            int color = ColorUtils.toColor(styleValue);
            strokePaint.setColor(color);
        } else if (styleName.equals("strokeWidth")) {
            strokePaint.setStrokeWidth(FloatUtils.parseFloat(styleValue));
        } else if (styleName.equals("fill")) {
            int color = ColorUtils.toColor(styleValue);
            fillPaint.setColor(color);
        }
        super.setStyle(styleName, styleValue);
    }

    @Override
    public void draw(Canvas canvas) {
        if (fillPaint.getColor() != Color.TRANSPARENT) canvas.drawPath(path, fillPaint);
        if (strokePaint.getColor() != Color.TRANSPARENT) canvas.drawPath(path, strokePaint);
    }
}
