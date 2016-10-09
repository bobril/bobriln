package com.bobril.bobriln;

import android.view.View;

import com.facebook.csslayout.CSSMeasureMode;

public final class FloatUtils {
    public static float unboxToFloat(Object o) {
        if (o instanceof Double) {
            return ((Double) o).floatValue();
        }
        if (o instanceof Integer) {
            return ((Integer) o).floatValue();
        }
        if (o instanceof Float) {
            return (Float) o;
        }
        throw new RuntimeException("Cannot unbox " + o.toString() + " to float");
    }

    public static float parseFloat(Object value) {
        if (value instanceof String) {
            String str = (String)value;
            return Float.parseFloat(str);
        }
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        throw new RuntimeException("Cannot parse " + value.toString() + " to float");
    }

    static int toAndroid(float size, CSSMeasureMode mode) {
        if (mode == CSSMeasureMode.AT_MOST)
            return View.MeasureSpec.makeMeasureSpec((int) Math.floor(size), View.MeasureSpec.AT_MOST);
        if (mode == CSSMeasureMode.EXACTLY)
            return View.MeasureSpec.makeMeasureSpec((int) Math.round(size), View.MeasureSpec.EXACTLY);
        return View.MeasureSpec.makeMeasureSpec((int) Math.round(size), View.MeasureSpec.UNSPECIFIED);
    }
}

