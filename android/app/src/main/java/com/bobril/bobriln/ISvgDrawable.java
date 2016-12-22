package com.bobril.bobriln;

import android.graphics.Canvas;

public interface ISvgDrawable {
    SvgStyle getSvgStyle();
    void draw(Canvas canvas);
}
