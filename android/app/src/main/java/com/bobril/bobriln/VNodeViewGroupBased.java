package com.bobril.bobriln;

import com.facebook.csslayout.CSSLayoutContext;

public abstract class VNodeViewGroupBased extends VNodeViewBased {

    @Override
    int validateView(int indexInParent) {
        int res = super.validateView(indexInParent);
        int idx = 0;
        if (children!=null) {
            for (int i=0;i<children.size();i++) {
                idx = children.get(i).validateView(idx);
            }
        }
        return res;
    }

    @Override
    public void doLayout(CSSLayoutContext ctx) {
        if (children!=null) {
            for (int i=0;i<children.size();i++) {
                children.get(i).doLayout(ctx);
            }
        }
        super.doLayout(ctx);
    }

    @Override
    public void flushLayout() {
        if (children!=null) {
            for (int i=0;i<children.size();i++) {
                children.get(i).flushLayout();
            }
        }
        super.flushLayout();
    }
}
