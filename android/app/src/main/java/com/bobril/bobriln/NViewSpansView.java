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
        if (decoration != null) decoration.onDraw(canvas);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int w, int h) {
        int count = getChildCount();
        if (count > 0) {
            getChildAt(0).layout(0, 0, w, h);
        }
        VNodeText vtext = (VNodeText) owner;
        SpanVNode[] spans = vtext.spanVNodes;
        if (spans == null) return;
        Layout layout = vtext.textView.getLayout();
        for (int i = 0; i < spans.length; i++) {
            SpanVNode spanVNode = spans[i];
            int line = layout.getLineForOffset(spanVNode.offset);
            spanVNode.calcPos(layout.getLineTop(line), layout.getLineBottom(line), layout.getLineDescent(line), layout.getPrimaryHorizontal(spanVNode.offset));
        }
        if (count - 1 != spans.length)
            throw new RuntimeException("NViewSpansView onLayout difference");
        for (int i = 0; i < count - 1; i++) {
            SpanVNode span = spans[i];
            View child = getChildAt(i + 1);
            child.layout(span.x, span.y, span.x + span.w, span.y + span.h);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
}
