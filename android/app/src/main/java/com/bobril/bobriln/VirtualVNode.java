package com.bobril.bobriln;

public class VirtualVNode extends VNode {

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
    }

    @Override
    void setStringChild(String content) {
        VNode n = vdom.replace(this,"Text");
        n.tag = "";
        n.setStringChild(content);
    }
}
