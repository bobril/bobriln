package com.bobril.bobriln;

import android.content.Context;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.csslayout.CSSLayoutContext;

import java.util.HashMap;
import java.util.Map;

public class VNodeTextInput extends VNodeViewBased implements IHasTextStyle, IVNodeTextLike, TextWatcher {
    SpannableStringBuilder builder;
    public SpanVNode[] spanVNodes;
    MyEditText textView;
    boolean ignoreChange;
    private boolean spanGarbage;

    @Override
    VNode createByTag(String tag) {
        if (tag == null || tag.equals("Text") || tag.equals("text")) {
            return new VNodeNestedText();
        }
        return super.createByTag(tag);
    }

    @Override
    View createView(Context ctx) {
        ignoreChange = true;
        builder = new SpannableStringBuilder();
        NViewSpansView res = new NViewSpansView(ctx, this);
        textView = new MyEditText(ctx, this);
        textView.addTextChangedListener(this);
        res.addView(textView);
        return res;
    }

    @Override
    public void setAttr(String attrName, Object attrValue) {
        super.setAttr(attrName, attrValue);
        if (attrName.equals("selectionStart")) {
            if (attrValue != null) {
                int start = IntUtils.parseInt(attrValue);
                textView.setSelection(start, textView.getSelectionEnd());
            }
        } else if (attrName.equals("selectionEnd")) {
            if (attrValue != null) {
                int end = IntUtils.parseInt(attrValue);
                textView.setSelection(textView.getSelectionStart(), end);
            }
        }
    }

    @Override
    int validateView(int indexInParent) {
        int res = super.validateView(indexInParent);
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
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
        ignoreChange = true;
        textView.setText(builder);
        ignoreChange = false;
        textView.setTextSize(accu.style.fontSize);
        ViewGroup vg = (ViewGroup) view;
        if (spanVNodes != null) {
            for (int i = 0; i < spanVNodes.length; i++) {
                View cv = spanVNodes[i].node.view;
                if (vg.getChildCount() - 1 > i && vg.getChildAt(i + 1) == cv) continue;
                if (cv.getParent() != null) {
                    vg.removeView(cv);
                }
                vg.addView(cv, i + 1);
            }
            while (vg.getChildCount() - 1 > spanVNodes.length) {
                vg.removeViewAt(vg.getChildCount() - 1);
            }
        } else {
            while (vg.getChildCount() > 1) {
                vg.removeViewAt(vg.getChildCount() - 1);
            }
        }
        return res;
    }

    @Override
    void setStringChild(String content) {
        super.setStringChild(content);
        invalidate();
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
        if (spanVNodes != null) for (int i = 0; i < spanVNodes.length; i++) {
            if (spanVNodes[i].node.isDirty()) return true;
        }
        return super.isDirty();
    }

    @Override
    public void flushLayout() {
        if (spanVNodes != null) for (int i = 0; i < spanVNodes.length; i++) {
            spanVNodes[i].node.flushLayout();
        }
        super.flushLayout();
    }

    @Override
    public void doLayout(CSSLayoutContext ctx) {
        super.doLayout(ctx);
        if (spanVNodes == null) return;
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
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (ignoreChange) return;
        // TODO shorten to true change - Android sends as changed whole word although user written just 1 character, but even worse sometimes were is no change!
        if (spanVNodes != null) {
            view.requestLayout();
            for (int i = 0; i < spanVNodes.length; i++) {
                SpanVNode spanVNode = spanVNodes[i];
                int o = spanVNode.offset;
                if (o >= start + before) {
                    spanVNode.offset += count - before;
                } else if (o >= start) {
                    if (s.charAt(o) != 16 || count == 0) {
                        view.invalidate();
                        spanVNode.offset = -1;
                        spanGarbage = true;
                        ((ViewGroup) view).removeView(spanVNode.node.view);
                        spanVNode.node = null;
                        if (spanVNodes.length == 1) {
                            spanVNodes = null;
                            break;
                        }
                        SpanVNode[] nSpanVNodes = new SpanVNode[spanVNodes.length - 1];
                        for (int j = 0; j < i; j++) nSpanVNodes[j] = spanVNodes[j];
                        for (int j = i; j < nSpanVNodes.length; j++)
                            nSpanVNodes[j] = spanVNodes[j + 1];
                        spanVNodes = nSpanVNodes;
                        i--;
                    }
                }
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("before", before);
        params.put("text", s.subSequence(start, start + count).toString());
        vdom.globalApp.emitJSEvent("onTextChanged", params, this.nodeId, -1);
        //Log.d("BobrilN", "AfterOnTextChanged "+s+" "+start+" "+before+" "+count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (spanGarbage) {
            spanGarbage = false;
            SpanVNode[] spans = s.getSpans(0, s.length(), SpanVNode.class);
            Boolean ch = false;
            for (int i = 0; i < spans.length; i++) {
                if (spans[i].offset < 0) {
                    s.removeSpan(spans[i]);
                    ch = true;
                }
            }
            if (ch) {
                int ss = textView.getSelectionStart();
                int se = textView.getSelectionEnd();
                textView.setText(s);
                textView.setSelection(ss, se);
            }
        }
    }
}
