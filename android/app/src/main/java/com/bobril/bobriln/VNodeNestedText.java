package com.bobril.bobriln;

public class VNodeNestedText extends VNode implements SpanTextProvider, IHasTextStyle {
    TextStyle textStyle;

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
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
    public void BuildSpannableString(TextStyleAccumulator accu) {
        TextStyle backupStyle = accu.style;
        if (textStyle != null) {
            textStyle.ReadInherited(accu.style, textStyle.flags);
            accu.style = textStyle;
        }
        if (content != null) {
            accu.ApplyTextStyle(accu.style);
            accu.append(content);
        } else {
            if (children != null) {
                for (int i = 0; i < children.size(); i++) {
                    VNode node = children.get(i);
                    if (node instanceof SpanTextProvider) {
                        ((SpanTextProvider) node).BuildSpannableString(accu);
                    }
                }
            }
        }
        accu.style = backupStyle;
    }
}
