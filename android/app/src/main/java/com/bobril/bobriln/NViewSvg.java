package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.util.List;

public class NViewSvg extends View {
    private final VNodeSvg owner;

    public NViewSvg(Context ctx, VNodeSvg nodeSvg) {
        super(ctx);
        this.owner = nodeSvg;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        ViewDecoration decoration = owner.getDecoration();
        if (decoration != null) decoration.onBeforeDraw(canvas);
        canvas.save();
        canvas.scale(owner.vdom.density, owner.vdom.density);
        List<VNode> children = owner.children;
        for (int i=0;i<children.size();i++) {
            VNode vNode = children.get(i);
            if (vNode instanceof ISvgDrawable) {
                ISvgDrawable drawable = (ISvgDrawable)vNode;
                drawable.draw(canvas);
            }
        }
        canvas.restore();
        if (decoration != null) decoration.onAfterDraw(canvas);
    }

}
