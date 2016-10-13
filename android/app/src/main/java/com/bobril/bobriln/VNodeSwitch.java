package com.bobril.bobriln;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.facebook.csslayout.CSSMeasureMode;
import com.facebook.csslayout.CSSNode;
import com.facebook.csslayout.CSSNodeAPI;
import com.facebook.csslayout.MeasureOutput;

import java.util.HashMap;

public class VNodeSwitch extends VNodeView implements CompoundButton.OnCheckedChangeListener, CSSNode.MeasureFunction {
    public VNodeSwitch() {
        css.setMeasureFunction(this);
        css.setIsTextNode(true);
    }

    @Override
    View createView(Context ctx) {
        Switch view = new Switch(ctx);
        view.setOnCheckedChangeListener(this);
        Object value = getAttrs().get("value");
        if (value!=null) {
            view.setChecked(BoolUtils.parseBool(value));
        }
        return view;
    }

    @Override
    public void setAttr(String attrName, Object attrValue) {
        super.setAttr(attrName, attrValue);
        if (attrName.equals("value")) {
            if (attrValue==null) return;
            if (view==null) return;
            ((Switch)view).setChecked(BoolUtils.parseBool(attrValue));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("value", isChecked);
        vdom.globalApp.emitJSEvent("onChange", params, nodeId, -1);
    }

    public void measure(CSSNodeAPI node, float width, CSSMeasureMode widthMode, float height, CSSMeasureMode heightMode, MeasureOutput measureOutput) {
        view.measure(toAndroid(width, widthMode), toAndroid(height, heightMode));
        measureOutput.width = view.getMeasuredWidth();
        measureOutput.height = view.getMeasuredHeight();
        //Log.d("BobrilN",String.format("Measure: %s %s %s", this.content, String.valueOf(measureOutput.width), String.valueOf(measureOutput.height)));
    }

    private static int toAndroid(float size, CSSMeasureMode mode) {
        if (mode == CSSMeasureMode.AT_MOST)
            return View.MeasureSpec.makeMeasureSpec((int) Math.floor(size), View.MeasureSpec.AT_MOST);
        if (mode == CSSMeasureMode.EXACTLY)
            return View.MeasureSpec.makeMeasureSpec((int) Math.round(size), View.MeasureSpec.EXACTLY);
        return View.MeasureSpec.makeMeasureSpec((int) Math.round(size), View.MeasureSpec.UNSPECIFIED);
    }
}
