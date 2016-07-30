package com.bobril.bobriln;

public abstract class ViewGroupBasedVNode extends ViewBasedVNode {

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
    public void flushLayout() {
        if (children!=null) {
            for (int i=0;i<children.size();i++) {
                children.get(i).flushLayout();
            }
        }
        super.flushLayout();
    }
}
