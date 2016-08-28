package com.bobril.bobriln;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Objects;

public class ViewVNode extends ViewGroupBasedVNode {
    @Override
    View createView(Context ctx) {
        View view = new ViewView(ctx, this);
        return view;
    }

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
    }

    @Override
    void setStringChild(String content) {
        if (content==null) {
            this.content=null;
            children=null;
            if (view!=null) ((ViewView)view).removeAllViews();
            css.dirty();
            return;
        }
        this.content = content;
        VNode n=createByTag("Text");
        n.vdom = this.vdom;
        n.tag = "Text";
        n.nodeId = -2;
        n.lparent = this;
        insertBefore(n, null);
        n.setStringChild(content);
    }
}
