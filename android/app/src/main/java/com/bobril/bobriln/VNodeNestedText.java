package com.bobril.bobriln;

public class VNodeNestedText extends VNode implements IHasTextStyle {
    TextStyle textStyle;

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
    }

    @Override
    public VNode getParent() {
        return lparent.getParent();
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
    public void setStyle(String styleName, Object styleValue) {
        super.setStyle(styleName, styleValue);
        textStyle = TextStyle.setStyle(textStyle, styleName, styleValue);
    }

    @Override
    public TextStyle getTextStyle() {
        return textStyle;
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
    public void flushLayout() {
        if (children!=null) {
            for (int i=0;i<children.size();i++) {
                children.get(i).flushLayout();
            }
        }
        super.flushLayout();
    }
}
