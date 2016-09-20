package com.bobril.bobriln;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.facebook.csslayout.CSSMeasureMode;
import com.facebook.csslayout.CSSNode;
import com.facebook.csslayout.MeasureOutput;

public class VNodeText extends VNodeViewBased implements CSSNode.MeasureFunction, SpanTextProvider {
    SpannableStringBuilder builder;

    VNodeText() {
        css.setMeasureFunction(this);
        css.setIsTextNode(true);
    }

    @Override
    VNode createByTag(String tag) {
        if (tag!=null && tag.equals("Text")) {
            return new VNodeNestedText();
        }
        return lparent.createByTag(tag);
    }

    @Override
    View createView(Context ctx) {
        builder = new SpannableStringBuilder();
        NViewText res = new NViewText(ctx, this);
        return res;
    }

    @Override
    int validateView(int indexInParent) {
        int res = super.validateView(indexInParent);
        builder.clear();
        final TextStyleAccumulator accu = vdom.textStyleAccu;
        accu.ResetBuilder(builder, vdom.density);
        int flags = 0;
        VNode n = this.lparent;
        while (n != null) {
            if (n instanceof IHasTextStyle) {
                TextStyle s = ((IHasTextStyle) n).getTextStyle();
                if (s != null)
                    flags = accu.style.ReadInherited(s, flags);
            }
            n = n.lparent;
        }
        accu.style.AddDefaults(flags);
        BuildSpannableString(accu);
        accu.Flush();
        ((NViewText) view).setText(builder);
        return res;
    }

    @Override
    void setStringChild(String content) {
        super.setStringChild(content);
        invalidate();
        css.dirty();
    }

    @Override
    public void measure(CSSNode node, float width, CSSMeasureMode widthMode, float height, CSSMeasureMode heightMode, MeasureOutput measureOutput) {
        view.measure(toAndroid(width, widthMode), toAndroid(height, heightMode));
        measureOutput.width = view.getMeasuredWidth();
        measureOutput.height = view.getMeasuredHeight();
        //Log.d("BobrilN",String.format("Measure: %s %s %s", this.content, String.valueOf(measureOutput.width), String.valueOf(measureOutput.height)));
    }

    private static int toAndroid(float size, CSSMeasureMode mode) {
        if (mode == CSSMeasureMode.AT_MOST)
            return View.MeasureSpec.makeMeasureSpec((int) Math.floor(size), View.MeasureSpec.AT_MOST);
        if (mode == CSSMeasureMode.EXACTLY)
            return View.MeasureSpec.makeMeasureSpec((int) Math.round(size), View.MeasureSpec.EXACTLY);
        return View.MeasureSpec.makeMeasureSpec((int) Math.round(size), View.MeasureSpec.UNSPECIFIED);
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
