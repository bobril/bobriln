package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class NViewText extends TextView {
    final VNodeViewBased owner;

    public NViewText(Context ctx, VNodeViewBased owner) {
        super(ctx);
        this.owner = owner;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        ViewDecoration decoration = owner.getDecoration();
        if (decoration != null) decoration.onDraw(canvas);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        VNodeText vtext = (VNodeText) owner;
        SpanVNode[] spans = vtext.spanVNodes;
        if (spans == null) return;
        int count = spans.length;
        for (int i = 0; i < count; i++) {
            SpanVNode span = spans[i];
            //Log.d("Layout", String.format("Change: %d left: %d top: %d right: %d bottom: %d children: %d w: %d h: %d", b ? 1 : 0, left, top, right, bottom, count, span.w, span.h));
            span.node.view.layout(0, 0, span.w, span.h);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        VNodeText vtext = (VNodeText) owner;
        SpanVNode[] spans = vtext.spanVNodes;
        float px = event.getX();
        float py = event.getY();
        if (spans != null) {
            for (int i = 0; i < spans.length; i++) {
                SpanVNode span = spans[i];
                if (px >= span.x && py >= span.y && px < span.x + span.w && py < span.y + span.h) {
                    event.offsetLocation(-span.x, -span.y);
                    boolean res = span.node.view.dispatchTouchEvent(event);
                    event.offsetLocation(span.x, span.y);
                    if (res) return true;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
