package com.bobril.bobriln;

public class VNodeVirtual extends VNode {

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
    }

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
    void setStringChild(String content) {
        VNode n = vdom.replace(this,"Text");
        n.tag = "";
        n.setStringChild(content);
    }
}
