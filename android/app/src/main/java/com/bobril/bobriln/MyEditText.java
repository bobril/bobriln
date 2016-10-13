package com.bobril.bobriln;

import android.content.Context;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

public class MyEditText extends EditText {
    VNodeTextInput owner;

    public MyEditText(Context context, VNodeTextInput owner) {
        super(context);
        this.owner = owner;
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        getParent().requestLayout();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        //Log.d("BobrilN", "SelectionChanged "+selStart+" "+selEnd);
        super.onSelectionChanged(selStart, selEnd);
        if (owner==null || owner.ignoreChange) return;
        Map<String, Object> param = new HashMap<>();
        param.put("start", selStart);
        param.put("end", selEnd);
        owner.vdom.globalApp.emitJSEvent("onSelectionChanged", param, owner.nodeId, -1);
    }
}
