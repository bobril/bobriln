package com.bobril.bobriln;

import android.content.Context;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.csslayout.CSSLayoutContext;
import com.facebook.csslayout.CSSMeasureMode;
import com.facebook.csslayout.CSSNode;
import com.facebook.csslayout.MeasureOutput;

public class VNodeText extends VNodeViewBased implements CSSNode.MeasureFunction, IHasTextStyle, IVNodeTextLike {
    SpannableStringBuilder builder;
    public SpanVNode[] spanVNodes;
    TextView textView;

    VNodeText() {
        css.setMeasureFunction(this);
        css.setIsTextNode(true);
    }

    @Override
    VNode createByTag(String tag) {
        if (tag == null || tag.equals("Text") || tag.equals("text")) {
            return new VNodeNestedText();
        }
        return super.createByTag(tag);
    }

    @Override
    View createView(Context ctx) {
        builder = new SpannableStringBuilder();
        NViewSpansView res = new NViewSpansView(ctx, this);
        textView = new TextView(ctx);
        res.addView(textView);
        return res;
    }

    @Override
    int validateView(int indexInParent) {
        int res = super.validateView(indexInParent);
        if (children!=null) {
            for (int i=0;i<children.size();i++) {
                children.get(i).validateView(0);
            }
        }
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
        BuildSpannableString(this, accu);
        spanVNodes = accu.Flush();
        textView.setText(builder);
        ViewGroup vg = (ViewGroup)view;
        if (spanVNodes!=null) {
            for(int i=0;i<spanVNodes.length; i++) {
                View cv = spanVNodes[i].node.view;
                if (vg.getChildCount()-1>i && vg.getChildAt(i+1)==cv) continue;
                if (cv.getParent()!=null) {
                    vg.removeView(cv);
                }
                vg.addView(cv,i+1);
            }
            while (vg.getChildCount()-1>spanVNodes.length) {
                vg.removeViewAt(vg.getChildCount()-1);
            }
        } else {
            while (vg.getChildCount()>1) {
                vg.removeViewAt(vg.getChildCount()-1);
            }
        }
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
        view.measure(FloatUtils.toAndroid(width, widthMode), FloatUtils.toAndroid(height, heightMode));
        measureOutput.width = textView.getMeasuredWidth();
        measureOutput.height = textView.getMeasuredHeight();
        //Log.d("BobrilN",String.format("Measure: %s %s %s", this.content, String.valueOf(measureOutput.width), String.valueOf(measureOutput.height)));
    }

    public void BuildSpannableString(VNode that, TextStyleAccumulator accu) {
        if (that == this || that instanceof VNodeNestedText) {
            TextStyle backupStyle = accu.style;

            TextStyle textStyle = ((IHasTextStyle) that).getTextStyle();
            if (textStyle != null) {
                textStyle.ReadInherited(accu.style, textStyle.flags);
                accu.style = textStyle;
            }
            if (that.content != null) {
                accu.ApplyTextStyle(accu.style);
                accu.append(that.content);
            } else {
                if (children != null) {
                    for (int i = 0; i < children.size(); i++) {
                        VNode node = children.get(i);
                        BuildSpannableString(node, accu);
                    }
                }
            }
            accu.style = backupStyle;
        } else if (that instanceof VNodeViewBased) {
            accu.appendView((VNodeViewBased) that);
        }
    }

    @Override
    public boolean isDirty() {
        if (spanVNodes!=null) for (int i = 0; i < spanVNodes.length; i++) {
            if (spanVNodes[i].node.isDirty()) return true;
        }
        return super.isDirty();
    }

    @Override
    public void flushLayout() {
        if (spanVNodes!=null) for (int i = 0; i < spanVNodes.length; i++) {
            spanVNodes[i].node.flushLayout();
        }
        super.flushLayout();
    }

    @Override
    public void doLayout(CSSLayoutContext ctx) {
        super.doLayout(ctx);
        if (spanVNodes==null) return;
        for (int i = 0; i < spanVNodes.length; i++) {
            spanVNodes[i].node.doLayout(ctx);
        }
    }

    @Override
    public TextStyle getTextStyle() {
        return textStyle;
    }

    @Override
    public SpanVNode[] getSpanVNodes() {
        return spanVNodes;
    }

    @Override
    public Layout getLayout() {
        return textView.getLayout();
    }

    @Override
    public boolean isMeasureByFirst() {
        return true;
    }
}
