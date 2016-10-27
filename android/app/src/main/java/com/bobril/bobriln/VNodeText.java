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
import com.facebook.csslayout.CSSNodeAPI;
import com.facebook.csslayout.MeasureOutput;

import static android.text.Layout.DIR_RIGHT_TO_LEFT;

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
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                VNode child = children.get(i);
                if (child.needValidate())
                    child.validateView(0);
            }
        }
        builder.clear();
        builder.clearSpans();
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
        css.dirty();
    }

    @Override
    public void measure(CSSNodeAPI node, float width, CSSMeasureMode widthMode, float height, CSSMeasureMode heightMode, MeasureOutput measureOutput) {
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
                if (that.children != null) {
                    for (int i = 0; i < that.children.size(); i++) {
                        VNode node = that.children.get(i);
                        BuildSpannableString(node, accu);
                    }
                }
            }
            accu.style = backupStyle;
        } else if (that instanceof VNodeViewBased) {
            accu.appendView((VNodeViewBased) that);
        }
    }

    public int Offset2NodeId(VNode that, int[] offset) {
        if (that == this || that instanceof VNodeNestedText) {
            if (that.content != null) {
                offset[0] -= that.content.length();
                if (offset[0] <= 0) return that.nodeId;
            } else {
                if (that.children != null) {
                    for (int i = 0; i < that.children.size(); i++) {
                        VNode node = that.children.get(i);
                        int res = Offset2NodeId(node, offset);
                        if (res > 0) return res;
                        if (offset[0] < 0) return 0;
                    }
                }
            }
        } else if (that instanceof VNodeViewBased) {
            offset[0]--;
            if (offset[0] < 0) return that.nodeId;
        }
        return 0;
    }

    @Override
    public int pos2NodeId(float x, float y) {
        float lx = css.getLayoutX();
        float ly = css.getLayoutY();
        float w = css.getLayoutWidth();
        float h = css.getLayoutHeight();
        x -= lx;
        y -= ly;
        if (x < 0 || y < 0 || x > w || y > h) return 0;
        if (spanVNodes != null) {
            int c = spanVNodes.length;
            for (int i = 0; i < c; i++) {
                SpanVNode ch = spanVNodes[i];
                int id = ch.node.pos2NodeId(x - ch.x, y - ch.y);
                if (id > 0) return id;
            }
        }
        if (children != null) {
            Layout layout = getLayout();
            if (layout == null) return nodeId;
            int ry = Math.round(y);
            int line = layout.getLineForVertical(ry);
            if (layout.getLineBottom(line)<ry) return nodeId;
            int[] offset = vdom.tempIntArray;
            offset[0] = layout.getOffsetForHorizontal(line, x);
            float ph1 = layout.getPrimaryHorizontal(offset[0]);
            float ph2;
            int dir = layout.getParagraphDirection(line);
            if (layout.getLineEnd(line)>offset[0]+1) {
                ph2 = layout.getPrimaryHorizontal(offset[0] + 1);
            } else {
                if (dir==DIR_RIGHT_TO_LEFT)
                    ph2 = layout.getLineLeft(line);
                else
                    ph2 = layout.getLineRight(line);
            }
            if (ph2 < ph1) {
                if (ph2 <= x && x <= ph1) {
                    offset[0]++;
                    return Offset2NodeId(this, offset);
                }
            } else {
                if (ph1 <= x && x <= ph2) {
                    offset[0]++;
                    return Offset2NodeId(this, offset);
                }
            }
            if (layout.getLineStart(line)<offset[0]-1) {
                ph2 = layout.getPrimaryHorizontal(offset[0]-1);
            } else {
                if (dir==DIR_RIGHT_TO_LEFT)
                    ph2 = layout.getLineRight(line);
                else
                    ph2 = layout.getLineLeft(line);
            }
            if (ph2 < ph1) {
                if (ph2 <= x && x <= ph1) {
                    return Offset2NodeId(this, offset);
                }
            } else {
                if (ph1 <= x && x <= ph2) {
                    return Offset2NodeId(this, offset);
                }
            }
        }
        return nodeId;
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
        return true;
    }
}
