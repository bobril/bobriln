package com.bobril.bobriln;

import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.List;

public class VNodeImage extends VNodeViewGroupBased {
    @Override
    View createView(Context ctx) {
        View view = new NViewImage(ctx, this);
        return view;
    }

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
    }

    public List<Object> source;

    @Override
    public void setAttr(String attrName, Object attrValue) {
        super.setAttr(attrName, attrValue);
        if (attrName.equals("source")) {
            this.source = (List<Object>) attrValue;
            if (this.source!=null) {
                setStyle("width",this.source.get(0));
                setStyle("height",this.source.get(1));
            }
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
