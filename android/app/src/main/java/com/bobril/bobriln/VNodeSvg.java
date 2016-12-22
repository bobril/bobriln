package com.bobril.bobriln;

import android.content.Context;
import android.view.View;

import java.util.Objects;

public class VNodeSvg extends VNodeViewGroupBased {
    SvgStyle svgStyle = new SvgStyle();
    float[] viewBox = null;

    @Override
    VNode createByTag(String tag) {
        if (tag.equals("Path") || tag.equals("path")) {
            return new VNodeSvgPath();
        }
        return super.createByTag(tag);
    }

    @Override
    View createView(Context ctx) {
        return new NViewSvg(ctx, this);
    }

    @Override
    public void setStyle(String styleName, Object styleValue) {
        if (svgStyle.setStyle(styleName,styleValue))
            return;
        super.setStyle(styleName, styleValue);
    }

    @Override
    public void setAttr(String attrName, Object attrValue) {
        if (Objects.equals(attrName, "viewBox")) {
            if (attrValue==null) {
                viewBox = null;
            } else if (attrValue instanceof String) {
                viewBox = new float[4];
                vdom.svgHelper.parseViewBox((String) attrValue,viewBox);
            }
            return;
        }
        super.setAttr(attrName, attrValue);
    }
}
