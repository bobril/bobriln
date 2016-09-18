package com.bobril.bobriln;

import android.content.Context;
import android.view.View;

import com.facebook.csslayout.CSSLayoutContext;

import java.util.Map;

public class VNodeRoot extends VNodeViewGroupBased {
    Map<String,IVNodeFactory> vnodeFactories;

    public VNodeRoot(Map<String,IVNodeFactory> vnodeFactories) {
        this.vnodeFactories = vnodeFactories;
    }

    @Override
    VNode createByTag(String tag)
    {
        if (tag==null) return new VNodeVirtual();
        return vnodeFactories.get(tag).create();
    }

    @Override
    View createView(Context ctx) {
        return view;
    }

    @Override
    public boolean isDirty() {
        if (children==null) return false;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).isDirty()) return true;
        }
        return false;
    }

    @Override
    public void doLayout(CSSLayoutContext ctx) {
        if (children==null) return;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).doLayout(ctx);
        }
    }

    @Override
    public void flushLayout() {
        if (children==null) return;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).flushLayout();
        }
    }

    @Override
    public void setScreenSize(int width, int height) {
        if (children==null) return;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setScreenSize(width,height);
        }
    }
}
