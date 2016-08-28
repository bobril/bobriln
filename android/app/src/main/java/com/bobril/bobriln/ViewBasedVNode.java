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
import com.facebook.csslayout.CSSPositionType;
import com.facebook.csslayout.CSSWrap;
import com.facebook.csslayout.Spacing;

public abstract class ViewBasedVNode extends VNode {
    View view;
    PublicCSSNode css;
    ViewDecoration decoration;
    ViewBasedVNode() {
        css = new PublicCSSNode();
    }

    abstract View createView(Context ctx);

    public int pos2NodeId(float x, float y) {
        float lx = css.getLayoutX();
        float ly = css.getLayoutY();
        float w = css.getLayoutWidth();
        float h = css.getLayoutHeight();
        if (x<lx || y<ly || x>lx+w || y>ly+h) return 0;
        if (children!=null) {
            int c = children.size();
            for(int i=0;i<c;i++) {
                VNode ch = children.get(i);
                int id = ch.pos2NodeId(x-lx,y-ly);
                if (id>0) return id;
            }
        }
        return nodeId;
    }

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

    public void invalidateView() {
        if (view!=null) {
            view.invalidate();
        }
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
        view.requestLayout();
        css.markLayoutSeen();
    }

    @Override
    public void setStyle(String styleName, Object styleValue) {
        super.setStyle(styleName, styleValue);
        switch (styleName) {
            case "background":
                lazyDecoration().setBackground(styleValue);
                break;
            case "backgroundColor":
                lazyDecoration().setBackgroundColor(styleValue);
                break;
            case "borderColor":
                lazyDecoration().setBorderColor(Spacing.ALL, styleValue);
                break;
            case "borderTopColor":
                lazyDecoration().setBorderColor(Spacing.TOP, styleValue);
                break;
            case "borderBottomColor":
                lazyDecoration().setBorderColor(Spacing.BOTTOM, styleValue);
                break;
            case "borderLeftColor":
                lazyDecoration().setBorderColor(Spacing.LEFT, styleValue);
                break;
            case "borderRightColor":
                lazyDecoration().setBorderColor(Spacing.RIGHT, styleValue);
                break;
            case "borderHorizontalColor":
                lazyDecoration().setBorderColor(Spacing.HORIZONTAL, styleValue);
                break;
            case "borderVerticalColor":
                lazyDecoration().setBorderColor(Spacing.VERTICAL, styleValue);
                break;
            case "borderStartColor":
                lazyDecoration().setBorderColor(Spacing.START, styleValue);
                break;
            case "borderEndColor":
                lazyDecoration().setBorderColor(Spacing.END, styleValue);
                break;
            case "position":
                css.setPositionType(toCSSPositionType(styleValue));
                break;
            case "width":
                css.setStyleWidth(toCSSDimension(styleValue));
                break;
            case "height":
                css.setStyleHeight(toCSSDimension(styleValue));
                break;
            case "top":
                css.setPositionTop(toCSSDimension(styleValue));
                break;
            case "left":
                css.setPositionLeft(toCSSDimension(styleValue));
                break;
            case "right":
                css.setPositionRight(toCSSDimension(styleValue));
                break;
            case "bottom":
                css.setPositionBottom(toCSSDimension(styleValue));
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
            case "flexWrap":
                css.setWrap(toCSSWrap(styleValue));
                break;
            case "padding":
                css.setPadding(Spacing.ALL, toCSSDimension(styleValue));
                break;
            case "paddingLeft":
                css.setPadding(Spacing.LEFT, toCSSDimension(styleValue));
                break;
            case "paddingTop":
                css.setPadding(Spacing.TOP, toCSSDimension(styleValue));
                break;
            case "paddingRight":
                css.setPadding(Spacing.RIGHT, toCSSDimension(styleValue));
                break;
            case "paddingBottom":
                css.setPadding(Spacing.BOTTOM, toCSSDimension(styleValue));
                break;
            case "paddingHorizontal":
                css.setPadding(Spacing.HORIZONTAL, toCSSDimension(styleValue));
                break;
            case "paddingVertical":
                css.setPadding(Spacing.VERTICAL, toCSSDimension(styleValue));
                break;
            case "paddingStart":
                css.setPadding(Spacing.START, toCSSDimension(styleValue));
                break;
            case "paddingEnd":
                css.setPadding(Spacing.END, toCSSDimension(styleValue));
                break;
            case "margin":
                css.setMargin(Spacing.ALL, toCSSDimension(styleValue));
                break;
            case "marginLeft":
                css.setMargin(Spacing.LEFT, toCSSDimension(styleValue));
                break;
            case "marginTop":
                css.setMargin(Spacing.TOP, toCSSDimension(styleValue));
                break;
            case "marginRight":
                css.setMargin(Spacing.RIGHT, toCSSDimension(styleValue));
                break;
            case "marginBottom":
                css.setMargin(Spacing.BOTTOM, toCSSDimension(styleValue));
                break;
            case "marginHorizontal":
                css.setMargin(Spacing.HORIZONTAL, toCSSDimension(styleValue));
                break;
            case "marginVertical":
                css.setMargin(Spacing.VERTICAL, toCSSDimension(styleValue));
                break;
            case "marginStart":
                css.setMargin(Spacing.START, toCSSDimension(styleValue));
                break;
            case "marginEnd":
                css.setMargin(Spacing.END, toCSSDimension(styleValue));
                break;
            case "borderWidth":
                css.setBorder(Spacing.ALL, toCSSDimension(styleValue));
                break;
            case "borderLeftWidth":
                css.setBorder(Spacing.LEFT, toCSSDimension(styleValue));
                break;
            case "borderTopWidth":
                css.setBorder(Spacing.TOP, toCSSDimension(styleValue));
                break;
            case "borderRightWidth":
                css.setBorder(Spacing.RIGHT, toCSSDimension(styleValue));
                break;
            case "borderBottomWidth":
                css.setBorder(Spacing.BOTTOM, toCSSDimension(styleValue));
                break;
            case "borderHorizontalWidth":
                css.setBorder(Spacing.HORIZONTAL, toCSSDimension(styleValue));
                break;
            case "borderVerticalWidth":
                css.setBorder(Spacing.VERTICAL, toCSSDimension(styleValue));
                break;
            case "borderStartWidth":
                css.setBorder(Spacing.START, toCSSDimension(styleValue));
                break;
            case "borderEndWidth":
                css.setBorder(Spacing.END, toCSSDimension(styleValue));
                break;
        }
    }

    private CSSPositionType toCSSPositionType(Object value) {
        if (value==null) return CSSPositionType.RELATIVE;
        if (value.equals("absolute")) return CSSPositionType.ABSOLUTE;
        if (value.equals("relative")) return CSSPositionType.RELATIVE;
        return CSSPositionType.RELATIVE;
    }

    private CSSWrap toCSSWrap(Object value) {
        if (value==null) return CSSWrap.NOWRAP;
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue()?CSSWrap.WRAP:CSSWrap.NOWRAP;
        }
        if (value.equals("wrap")) return CSSWrap.WRAP;
        if (value.equals("nowrap")) return CSSWrap.NOWRAP;
        return CSSWrap.NOWRAP;
    }

    private CSSJustify toCSSJustify(Object value) {
        if (value==null) return CSSJustify.FLEX_START;
        if (value.equals("flex-start")) return CSSJustify.FLEX_START;
        if (value.equals("flex-end")) return CSSJustify.FLEX_END;
        if (value.equals("center")) return CSSJustify.CENTER;
        if (value.equals("space-around")) return CSSJustify.SPACE_AROUND;
        if (value.equals("space-between")) return CSSJustify.SPACE_BETWEEN;
        return CSSJustify.FLEX_START;
    }

    private CSSAlign toCSSAlign(Object value, CSSAlign dflt) {
        if (value==null) return dflt;
        if (value.equals("flex-start")) return CSSAlign.FLEX_START;
        if (value.equals("flex-end")) return CSSAlign.FLEX_END;
        if (value.equals("auto")) return CSSAlign.AUTO;
        if (value.equals("stretch")) return CSSAlign.STRETCH;
        if (value.equals("center")) return CSSAlign.CENTER;
        return dflt;
    }

    private CSSFlexDirection toFlexDirection(Object value) {
        if (value==null) return CSSFlexDirection.COLUMN;
        if (value.equals("column")) return CSSFlexDirection.COLUMN;
        if (value.equals("row")) return CSSFlexDirection.ROW;
        if (value.equals("column-reverse")) return CSSFlexDirection.COLUMN_REVERSE;
        if (value.equals("row-reverse")) return CSSFlexDirection.ROW_REVERSE;
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

    public ViewDecoration getDecoration() {
        return decoration;
    }

    public ViewDecoration lazyDecoration() {
        if (decoration==null) {
            decoration = new ViewDecoration(this);
        }
        return decoration;
    }
}
