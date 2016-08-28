package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

public class DecoratedTextView extends TextView {
    final ViewBasedVNode owner;

    public DecoratedTextView(Context ctx, ViewBasedVNode owner) {
        super(ctx);
        this.owner = owner;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        ViewDecoration decoration = owner.getDecoration();
        if (decoration != null) decoration.onDraw(canvas);
        super.dispatchDraw(canvas);
    }
}
