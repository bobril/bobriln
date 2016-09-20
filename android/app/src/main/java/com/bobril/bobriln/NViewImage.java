package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.List;

public class NViewImage extends NViewView {
    public NViewImage(Context ctx, VNodeImage imageVNode) {
        super(ctx, imageVNode);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        List<Object> source = ((VNodeImage) this.owner).source;
        if (source != null) {
            Bitmap b = ((VNodeImage) this.owner).vdom.globalApp.imageCache.get(source);
            float density = this.owner.vdom.density;
            if (b != null) {
                RectF r = this.owner.vdom.tempRectF;
                r.set(0, 0, density * FloatUtils.unboxToFloat(source.get(0)), density * FloatUtils.unboxToFloat(source.get(1)));
                canvas.drawBitmap(b, null, r, null);
            }
        }
    }
}
