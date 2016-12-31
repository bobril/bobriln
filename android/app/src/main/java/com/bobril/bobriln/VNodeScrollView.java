package com.bobril.bobriln;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.facebook.csslayout.CSSAlign;
import com.facebook.csslayout.CSSConstants;
import com.facebook.csslayout.CSSDirection;
import com.facebook.csslayout.CSSNode;
import com.facebook.csslayout.CSSOverflow;

import java.util.HashMap;
import java.util.Map;

public class VNodeScrollView extends VNodeView {
    CSSNode cssForChildren = new CSSNode();
    boolean horizontal = false;

    public VNodeScrollView() {
        css.setOverflow(CSSOverflow.SCROLL);
    }

    @Override
    View createView(Context ctx) {
        ViewGroup res;
        final VNodeScrollView that = this;
        if (horizontal)
            res = new HorizontalScrollView(ctx) {
                @Override
                protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                    that.onScroll(l, t);
                    super.onScrollChanged(l, t, oldl, oldt);
                }
            };
        else
            res = new ScrollView(ctx) {
                @Override
                protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                    that.onScroll(l, t);
                    super.onScrollChanged(l, t, oldl, oldt);
                }
            };
        res.addView(new NViewView(ctx, this, true));
        return res;
    }

    int lastLeft;
    int lastTop;

    private void onScroll(int left, int top) {
        if (lastLeft == left && lastTop == top) return;
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
    public CSSNode getCssForChildren() {
        return cssForChildren;
    }

    @Override
    int validateView(int indexInParent) {
        if (cssForChildren.getParent() == null) {
            css.addChildAt(cssForChildren, 0);
        }
        return super.validateView(indexInParent);
    }

    @Override
    public void flushLayout() {
        super.flushLayout();
        if (!cssForChildren.hasNewLayout()) return;
        ViewGroup v = getViewForChildren();
        v.forceLayout();
        cssForChildren.markLayoutSeen();
    }

    @Override
    public int pos2NodeId(float x, float y) {
        float lx = css.getLayoutX();
        float ly = css.getLayoutY();
        float w = css.getLayoutWidth();
        float h = css.getLayoutHeight();
        if (x < lx || y < ly || x > lx + w || y > ly + h) return 0;
        lx -= lastLeft;
        ly -= lastTop;
        if (children != null) {
            int c = children.size();
            for (int i = c - 1; i >= 0; i--) {
                VNode ch = children.get(i);
                int id = ch.pos2NodeId(x - lx, y - ly);
                if (id > 0) return id;
            }
        }
        return nodeId;
    }
}
