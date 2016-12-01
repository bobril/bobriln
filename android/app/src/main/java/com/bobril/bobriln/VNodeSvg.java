package com.bobril.bobriln;

import android.content.Context;
import android.view.View;

public class VNodeSvg extends VNodeViewGroupBased {

    @Override
    VNode createByTag(String tag) {
        if (tag.equals("Path") || tag.equals("path")) {
            return new VNodeSvgPath();
        }
        return super.createByTag(tag);
    }

    @Override
    View createView(Context ctx) {
        return new NViewSvg(ctx, this);
    }
}
