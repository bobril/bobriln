package com.bobril.bobriln;

import android.graphics.Canvas;
import android.graphics.Path;

import java.security.InvalidParameterException;

public class VNodeSvgPath extends VNode implements ISvgDrawable {
    Path path = new Path();
    SvgStyle svgStyle = new SvgStyle();

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
        if (svgStyle.setStyle(styleName, styleValue))
            return;
        super.setStyle(styleName, styleValue);
    }

    @Override
    public SvgStyle getSvgStyle() {
        return svgStyle;
    }

    @Override
    public void draw(Canvas canvas) {
        svgStyle.update();
        if (svgStyle.fillPaint != null) canvas.drawPath(path, svgStyle.fillPaint);
        if (svgStyle.strokePaint != null) canvas.drawPath(path, svgStyle.strokePaint);
    }
}
