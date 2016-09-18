package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Canvas;
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
}
