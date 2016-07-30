package com.bobril.bobriln;

import android.content.Context;
import android.view.ViewGroup;

public class RootView extends ViewGroup {

    public RootView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        int count = getChildCount();
        //Log.d("Layout", String.format("Change: %d left: %d top: %d right: %d bottom: %d children: %d", b ? 1 : 0, left, top, right, bottom, count));
        for (int i = 0; i < count; i++) {
            getChildAt(i).layout(0, 0, right, bottom);
        }
    }
}
