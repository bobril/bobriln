package com.bobril.bobriln;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

public class ViewView extends ViewGroup {

    public ViewView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int w, int h) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lpo = child.getLayoutParams();
            if (lpo instanceof AbsoluteLayout.LayoutParams){
                AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams)lpo;
                child.layout(lp.x,lp.y,lp.x+lp.width,lp.y+lp.height);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lpo = child.getLayoutParams();
            if (lpo instanceof AbsoluteLayout.LayoutParams){
                AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams)lpo;
                child.measure(MeasureSpec.makeMeasureSpec(lp.width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(lp.height,MeasureSpec.EXACTLY));
            }
        }
        LayoutParams lp= this.getLayoutParams();
        this.setMeasuredDimension(lp.width,lp.height);
    }
}
