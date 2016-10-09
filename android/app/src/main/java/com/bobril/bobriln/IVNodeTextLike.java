package com.bobril.bobriln;

import android.text.Layout;

public interface IVNodeTextLike {
    SpanVNode[] getSpanVNodes();
    Layout getLayout();
    boolean isMeasureByFirst();
}
