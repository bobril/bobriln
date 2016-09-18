package com.bobril.bobriln;

public class VNodeVirtual extends VNode implements SpanTextProvider {

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

    @Override
    public void BuildSpannableString(TextStyleAccumulator accu) {
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                VNode node = children.get(i);
                if (node instanceof SpanTextProvider) {
                    ((SpanTextProvider) node).BuildSpannableString(accu);
                }
            }
        }
    }
}
