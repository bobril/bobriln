package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;

public class NViewSpansView extends ViewGroup {
    VNodeViewBased owner;

    public NViewSpansView(Context context, VNodeViewBased owner) {
        super(context);
        this.owner = owner;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        ViewDecoration decoration = owner.getDecoration();
        if (decoration != null) decoration.onBeforeDraw(canvas);
        super.dispatchDraw(canvas);
        if (decoration != null) decoration.onAfterDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        View textView = getChildAt(0);
        IVNodeTextLike vtext = (IVNodeTextLike) owner;
        SpanVNode[] spans = vtext.getSpanVNodes();
        if (textView!=child) {
            canvas.save();
            canvas.clipRect(textView.getPaddingLeft(),textView.getPaddingTop(),textView.getRight()-textView.getPaddingRight(),textView.getBottom()-textView.getPaddingBottom());
        } else {
            if (spans != null) {
                Layout layout = vtext.getLayout();
                for (int i = 0; i < spans.length; i++) {
                    SpanVNode spanVNode = spans[i];
                    spanVNode.skipDraw = true;
                }
            }
        }
        boolean res=super.drawChild(canvas, child, drawingTime);
        if (textView!=child) {
            canvas.restore();
        } else {
            if (spans != null) {
                Layout layout = vtext.getLayout();
                for (int i = 0; i < spans.length; i++) {
                    SpanVNode spanVNode = spans[i];
                    spanVNode.skipDraw = false;
                }
            }
        }
        return res;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int w, int h) {
        int count = getChildCount();
        View textView = getChildAt(0);
        textView.layout(0, 0, w, h);
        IVNodeTextLike vtext = (IVNodeTextLike) owner;
        SpanVNode[] spans = vtext.getSpanVNodes();
        if (spans == null) return;
        Layout layout = vtext.getLayout();
        int topPadding = textView.getBaseline()-layout.getLineBaseline(0)-textView.getScrollY();
        int leftPadding = textView.getPaddingLeft()-textView.getScrollX();
        for (int i = 0; i < spans.length; i++) {
            SpanVNode spanVNode = spans[i];
            if (spanVNode.offset>=layout.getText().length()) {
                spanVNode.offset = -1;
            }
            if (spanVNode.offset<0) continue;
            int line = layout.getLineForOffset(spanVNode.offset);
            spanVNode.calcPos(layout.getLineTop(line)+topPadding, layout.getLineBottom(line)+topPadding, layout.getLineDescent(line), layout.getPrimaryHorizontal(spanVNode.offset)+leftPadding);
        }
        if (count - 1 != spans.length)
            throw new RuntimeException("NViewSpansView onLayout difference");
        for (int i = 0; i < count - 1; i++) {
            SpanVNode span = spans[i];
            View child = getChildAt(i + 1);
            if (span.offset<0) {
                child.layout(0,0,0,0);
            } else {
                child.layout(span.x, span.y, span.x + span.w, span.y + span.h);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (((IVNodeTextLike) owner).isMeasureByFirst()) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (i == 0) {
                    child.measure(widthMeasureSpec, heightMeasureSpec);
                    this.setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
                    continue;
                }
            }
        }
        else
        {
            getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
            LayoutParams lp = this.getLayoutParams();
            this.setMeasuredDimension(lp.width, lp.height);
        }
    }
}
