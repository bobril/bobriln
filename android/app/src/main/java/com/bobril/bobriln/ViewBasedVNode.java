package com.bobril.bobriln;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.facebook.csslayout.CSSAlign;
import com.facebook.csslayout.CSSConstants;
import com.facebook.csslayout.CSSDirection;
import com.facebook.csslayout.CSSFlexDirection;
import com.facebook.csslayout.CSSJustify;
import com.facebook.csslayout.CSSLayoutContext;

public abstract class ViewBasedVNode extends VNode {
    View view;
    PublicCSSNode css;
    ViewBasedVNode() {
        css = new PublicCSSNode();
    }

    abstract View createView(Context ctx);

    @Override
    int validateView(int indexInParent) {
        needValidate = false;
        if (view==null) {
            view = createView(vdom.ctx);
        }
        ViewGroupBasedVNode parent = (ViewGroupBasedVNode)getParent();
        if (parent!=null) {
            ViewGroup g=(ViewGroup)parent.view;
            if (css.getParent()==null)
                parent.css.addChildAt(css,indexInParent);
            else
            {
                int ci = parent.css.indexOf(css);
                if (ci!=indexInParent) {
                    parent.css.removeChildAt(ci);
                    parent.css.addChildAt(css, indexInParent);
                }
            }
            if (view.getParent()==null) {
                g.addView(view,indexInParent);
            } else if (view.getParent()==g) {
                int ci = g.indexOfChild(view);
                if (ci!=indexInParent) {
                    g.removeViewAt(ci);
                    g.addView(view,indexInParent);
                }
            }
        }
        return indexInParent+1;
    }

    @Override
    public void unsetView() {
        view = null;
        super.unsetView();
    }

    @Override
    public void flushLayout() {
        if (!css.hasNewLayout()) return;
        view.setLayoutParams(new AbsoluteLayout.LayoutParams(
                (int)Math.round(css.getLayoutWidth()),
                (int)Math.round(css.getLayoutHeight()),
                (int)Math.round(css.getLayoutX()),
                (int)Math.round(css.getLayoutY())
        ));
        int dir = View.TEXT_DIRECTION_INHERIT;
        if (css.getLayoutDirection()== CSSDirection.LTR) dir = View.TEXT_DIRECTION_LTR;
        else if (css.getLayoutDirection()== CSSDirection.RTL) dir = View.TEXT_DIRECTION_RTL;
        view.setTextDirection(dir);
        css.markLayoutSeen();
    }

    @Override
    public void setStyle(String styleName, Object styleValue) {
        super.setStyle(styleName, styleValue);
        switch (styleName) {
            case "width":
                css.setStyleWidth(toCSSDimension(styleValue));
                break;
            case "height":
                css.setStyleHeight(toCSSDimension(styleValue));
                break;
            case "minWidth":
                css.setStyleMinWidth(toCSSDimension(styleValue));
                break;
            case "minHeight":
                css.setStyleMinHeight(toCSSDimension(styleValue));
                break;
            case "maxWidth":
                css.setStyleMaxWidth(toCSSDimension(styleValue));
                break;
            case "maxHeight":
                css.setStyleMaxHeight(toCSSDimension(styleValue));
                break;
            case "flex":
                css.setFlex(toFlex(styleValue));
                break;
            case "flexDirection":
                css.setFlexDirection(toFlexDirection(styleValue));
                break;
            case "alignItems":
                css.setAlignItems(toCSSAlign(styleValue, CSSAlign.STRETCH));
                break;
            case "alignSelf":
                css.setAlignSelf(toCSSAlign(styleValue, CSSAlign.FLEX_START));
                break;
            case "justifyContent":
                css.setJustifyContent(toCSSJustify(styleValue));
                break;
        }
    }

    private CSSJustify toCSSJustify(Object value) {
        if (value==null) return CSSJustify.FLEX_START;
        if (value=="flex-start") return CSSJustify.FLEX_START;
        if (value=="flex-end") return CSSJustify.FLEX_END;
        if (value=="center") return CSSJustify.CENTER;
        if (value=="space-around") return CSSJustify.SPACE_AROUND;
        if (value=="space-between") return CSSJustify.SPACE_BETWEEN;
        return CSSJustify.FLEX_START;
    }

    private CSSAlign toCSSAlign(Object value, CSSAlign dflt) {
        if (value==null) return dflt;
        if (value=="flex-start") return CSSAlign.FLEX_START;
        if (value=="flex-end") return CSSAlign.FLEX_END;
        if (value=="auto") return CSSAlign.AUTO;
        if (value=="stretch") return CSSAlign.STRETCH;
        if (value=="center") return CSSAlign.CENTER;
        return dflt;
    }

    private CSSFlexDirection toFlexDirection(Object value) {
        if (value==null) return CSSFlexDirection.COLUMN;
        if (value=="column") return CSSFlexDirection.COLUMN;
        if (value=="row") return CSSFlexDirection.ROW;
        if (value=="column-reverse") return CSSFlexDirection.COLUMN_REVERSE;
        if (value=="row-reverse") return CSSFlexDirection.ROW_REVERSE;
        return CSSFlexDirection.COLUMN;
    }

    private float toFlex(Object value) {
        if (value==null) return 0;
        if (value instanceof Double) return ((Double)value).floatValue();
        if (value instanceof Integer) return ((Integer)value).floatValue();
        if (value instanceof String) return Float.parseFloat((String)value);
        return 0;
    }

    private float toCSSDimension(Object value) {
        if (value==null) return CSSConstants.UNDEFINED;
        if (value instanceof Double) return ((Double)value).floatValue();
        if (value instanceof Integer) return ((Integer)value).floatValue();
        if (value instanceof String) return Float.parseFloat((String)value);
        return CSSConstants.UNDEFINED;
    }

    @Override
    public boolean isDirty() {
        return css.isDirty();
    }

    @Override
    public void doLayout(CSSLayoutContext ctx) {
        css.calculateLayout(ctx);
    }

    @Override
    public void setScreenSize(int width, int height) {
        css.setStyleWidth(width);
        css.setStyleHeight(height);
    }
}
