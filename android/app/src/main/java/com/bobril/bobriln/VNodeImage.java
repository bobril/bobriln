package com.bobril.bobriln;

import android.content.Context;
import android.view.View;

import java.util.List;

public class VNodeImage extends VNodeViewGroupBased {
    @Override
    View createView(Context ctx) {
        return new NViewImage(ctx, this);
    }

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
    }

    public List<Object> source;

    @Override
    int validateView(int indexInParent) {
        int res = super.validateView(indexInParent);
        if (source!=null) {
            css.setStyleWidth(FloatUtils.unboxToFloat(source.get(0))*vdom.density);
            css.setStyleHeight(FloatUtils.unboxToFloat(source.get(1))*vdom.density);
        }
        return res;
    }

    @Override
    public void setAttr(String attrName, Object attrValue) {
        super.setAttr(attrName, attrValue);
        if (attrName.equals("source")) {
            this.source = (List<Object>) attrValue;
        }
    }

    @Override
    void setStringChild(String content) {
        if (content == null) {
            this.content = null;
            children = null;
            if (view != null) ((NViewView) view).removeAllViews();
            css.dirty();
            return;
        }
        this.content = content;
        VNode n = createByTag("Text");
        n.vdom = this.vdom;
        n.tag = "Text";
        n.nodeId = -2;
        n.lparent = this;
        insertBefore(n, null);
        n.setStringChild(content);
    }
}
