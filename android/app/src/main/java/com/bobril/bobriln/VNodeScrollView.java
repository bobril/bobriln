package com.bobril.bobriln;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.facebook.csslayout.CSSConstants;

import java.util.HashMap;
import java.util.Map;

public class VNodeScrollView extends VNodeView {
    boolean horizontal = false;

    @Override
    View createView(Context ctx) {
        ViewGroup res;
        final VNodeScrollView that = this;
        if (horizontal)
            res = new HorizontalScrollView(ctx) {
                @Override
                protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                    that.onScroll(l,t);
                    super.onScrollChanged(l, t, oldl, oldt);
                }
            };
        else
            res = new ScrollView(ctx) {
                @Override
                protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                    that.onScroll(l,t);
                    super.onScrollChanged(l, t, oldl, oldt);
                }
            };
        res.addView(new NViewView(ctx, this));
        return res;
    }

    int lastLeft;
    int lastTop;

    private void onScroll(int left, int top) {
        if (lastLeft==left && lastTop == top) return;
        lastLeft = left;
        lastTop = top;
        Map<String, Object> params = new HashMap<>();
        params.put("left", left / vdom.density);
        params.put("top", top / vdom.density);
        vdom.globalApp.emitJSEvent("onScroll", params, nodeId, -1);
    }

    @Override
    public void setAttr(String attrName, Object attrValue) {
        super.setAttr(attrName, attrValue);
        if (attrName.equals("horizontal")) {
            boolean newHorizontal = BoolUtils.parseBool(attrValue);
            if (horizontal != newHorizontal && view != null) {
                getViewForChildren().removeAllViews();
                ((ViewGroup) view.getParent()).removeView(view);
                invalidate();
            }
            horizontal = newHorizontal;
        }
    }

    @Override
    public ViewGroup getViewForChildren() {
        return (ViewGroup) ((ViewGroup) view).getChildAt(0);
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
