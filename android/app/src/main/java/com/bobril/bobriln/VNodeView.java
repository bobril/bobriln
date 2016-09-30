package com.bobril.bobriln;

import android.content.Context;
import android.view.View;

public class VNodeView extends VNodeViewGroupBased {
    @Override
    View createView(Context ctx) {
        View view = new NViewView(ctx, this);
        return view;
    }

    @Override
    void setStringChild(String content) {
        if (content==null) {
            this.content=null;
            children=null;
            if (view!=null) ((NViewView)view).removeAllViews();
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

