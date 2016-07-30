package com.bobril.bobriln;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.csslayout.CSSMeasureMode;
import com.facebook.csslayout.CSSNode;
import com.facebook.csslayout.MeasureOutput;

import java.util.Objects;

public class TextVNode extends ViewBasedVNode implements CSSNode.MeasureFunction {
    TextVNode() {
        css.setMeasureFunction(this);
        css.setIsTextNode(true);
    }

    @Override
    VNode createByTag(String tag) {
        return lparent.createByTag(tag);
    }

    @Override
    View createView(Context ctx) {
        TextView res = new TextView(ctx);
        updateBackground(res, getStyle().get("background"));
        res.setText(this.content);
        res.setTextColor(Color.WHITE);
        return res;
    }

    private void updateBackground(View view, Object background) {
        view.setBackgroundColor(ColorUtils.toColor(background));
    }

    @Override
    public void setStyle(String styleName, Object styleValue) {
        if (view!=null) {
            if (Objects.equals(styleName, "background")) {
                updateBackground(view, styleValue);
            }
        }
        super.setStyle(styleName, styleValue);
    }

    @Override
    void setStringChild(String content) {
        super.setStringChild(content);
        css.dirty();
        if (view!=null) {
            ((TextView)view).setText(content);
        }
    }

    @Override
    public void measure(CSSNode node, float width, CSSMeasureMode widthMode, float height, CSSMeasureMode heightMode, MeasureOutput measureOutput) {
        view.measure(toAndroid(width,widthMode),toAndroid(height,heightMode));
        measureOutput.width=view.getMeasuredWidth();
        measureOutput.height=view.getMeasuredHeight();
        Log.d("measure",String.format("%s %s %s", this.content, String.valueOf(width), String.valueOf(height)));
    }

    private static int toAndroid(float size, CSSMeasureMode mode) {
        if (mode==CSSMeasureMode.AT_MOST) return View.MeasureSpec.makeMeasureSpec((int)Math.floor(size), View.MeasureSpec.AT_MOST);
        if (mode==CSSMeasureMode.EXACTLY) return View.MeasureSpec.makeMeasureSpec((int)Math.round(size), View.MeasureSpec.EXACTLY);
        return View.MeasureSpec.makeMeasureSpec((int)Math.round(size),View.MeasureSpec.UNSPECIFIED);
    }
}
