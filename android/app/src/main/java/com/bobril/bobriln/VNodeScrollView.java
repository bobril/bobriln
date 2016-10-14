package com.bobril.bobriln;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.facebook.csslayout.CSSConstants;

public class VNodeScrollView extends VNodeView {
    boolean horizontal = false;

    @Override
    View createView(Context ctx) {
        ViewGroup res;
        if (horizontal)
            res = new HorizontalScrollView(ctx);
        else
            res = new ScrollView(ctx);
        res.addView(new NViewView(ctx, this));
        return res;
    }

    @Override
    public void setAttr(String attrName, Object attrValue) {
        super.setAttr(attrName, attrValue);
        if (attrName.equals("horizontal")) {
            boolean newHorizontal = BoolUtils.parseBool(attrValue);
            if (horizontal != newHorizontal && view != null) {
                getViewForChildren().removeAllViews();
                ((ViewGroup)view.getParent()).removeView(view);
                invalidate();
            }
            horizontal = newHorizontal;
        }
    }

    @Override
    public ViewGroup getViewForChildren() {
        return (ViewGroup)((ViewGroup)view).getChildAt(0);
    }

    @Override
    public void setScreenSize(int width, int height) {
        if (horizontal) {
            css.setStyleWidth(CSSConstants.UNDEFINED);
            css.setStyleHeight(height);
        } else {
            css.setStyleWidth(width);
            css.setStyleHeight(CSSConstants.UNDEFINED);
        }
    }
}
