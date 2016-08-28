package com.bobril.bobriln;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.facebook.csslayout.CSSConstants;
import com.facebook.csslayout.CSSDirection;
import com.facebook.csslayout.Spacing;

public class ViewDecoration {
    ViewBasedVNode owner;
    int backgroundColor;
    Paint backgroundPaint;
    IntSpacing borderColor;
    Paint leftBorderPaint;
    Paint topBorderPaint;
    Paint rightBorderPaint;
    Paint bottomBorderPaint;


    public ViewDecoration(ViewBasedVNode owner) {
        this.owner = owner;
    }

    public void onDraw(Canvas canvas) {
        VDom vdom = owner.vdom;
        PublicCSSNode css = owner.css;
        backgroundPaint = vdom.color2Paint(backgroundColor);
        boolean rtl = css.getLayoutDirection() == CSSDirection.RTL;
        if (borderColor != null) {
            leftBorderPaint = vdom.color2Paint(borderColor.get(Spacing.LEFT, rtl, Color.TRANSPARENT));
            topBorderPaint = vdom.color2Paint(borderColor.get(Spacing.TOP, rtl, Color.TRANSPARENT));
            rightBorderPaint = vdom.color2Paint(borderColor.get(Spacing.RIGHT, rtl, Color.TRANSPARENT));
            bottomBorderPaint = vdom.color2Paint(borderColor.get(Spacing.BOTTOM, rtl, Color.TRANSPARENT));
        } else {
            leftBorderPaint = null;
            topBorderPaint = null;
            rightBorderPaint = null;
            bottomBorderPaint = null;
        }
        Spacing borderWidth = css.getBorder();
        float leftBorderWidth = borderWidth.get(rtl ? Spacing.END : Spacing.START);
        if (CSSConstants.isUndefined(leftBorderWidth))
            leftBorderWidth = borderWidth.get(Spacing.LEFT);
        float rightBorderWidth = borderWidth.get(rtl ? Spacing.START : Spacing.END);
        if (CSSConstants.isUndefined(rightBorderWidth))
            rightBorderWidth = borderWidth.get(Spacing.RIGHT);
        float topBorderWidth = borderWidth.get(Spacing.TOP);
        float bottomBorderWidth = borderWidth.get(Spacing.BOTTOM);
        if (leftBorderPaint == null) leftBorderWidth = 0;
        if (topBorderPaint == null) topBorderWidth = 0;
        if (rightBorderPaint == null) rightBorderWidth = 0;
        if (bottomBorderPaint == null) bottomBorderWidth = 0;
        Paint leftBorderP = (leftBorderWidth == 0) ? null : leftBorderPaint;
        Paint topBorderP = (topBorderWidth == 0) ? null : topBorderPaint;
        Paint rightBorderP = (rightBorderWidth == 0) ? null : rightBorderPaint;
        Paint bottomBorderP = (bottomBorderWidth == 0) ? null : bottomBorderPaint;
        float height = css.getLayoutHeight();
        float width = css.getLayoutWidth();
        if (backgroundPaint != null) {
            canvas.drawRect(leftBorderWidth * 0.5f, topBorderWidth * 0.5f, width - rightBorderWidth * 0.5f, height - bottomBorderWidth * 0.5f, backgroundPaint);
        }
        Path path = vdom.helperPath;
        if (leftBorderP != null && leftBorderP == topBorderP && topBorderP == rightBorderP && rightBorderP == bottomBorderP) {
            path.rewind();
            path.addRect(0, 0, width, height, Path.Direction.CW);
            path.addRect(leftBorderWidth, topBorderWidth, width - rightBorderWidth, height - bottomBorderWidth, Path.Direction.CCW);
            canvas.drawPath(path, leftBorderP);
            return;
        }
        if (leftBorderP != null && leftBorderP == topBorderP && topBorderP == rightBorderP) {
            path.rewind();
            path.moveTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.close();
            canvas.drawPath(path, leftBorderP);
            leftBorderP = null;
            topBorderP = null;
            rightBorderP = null;
        }
        if (topBorderP != null && topBorderP == rightBorderP && rightBorderP == bottomBorderP) {
            path.rewind();
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.close();
            canvas.drawPath(path, topBorderP);
            topBorderP = null;
            rightBorderP = null;
            bottomBorderP = null;
        }
        if (rightBorderP != null && rightBorderP == bottomBorderP && bottomBorderP == leftBorderP) {
            path.rewind();
            path.moveTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.close();
            canvas.drawPath(path, rightBorderP);
            rightBorderP = null;
            bottomBorderP = null;
            leftBorderP = null;
        }
        if (bottomBorderP != null && bottomBorderP == leftBorderP && leftBorderP == topBorderP) {
            path.rewind();
            path.moveTo(width, height);
            path.lineTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.close();
            canvas.drawPath(path, bottomBorderP);
            bottomBorderP = null;
            leftBorderP = null;
            topBorderP = null;
        }
        if (leftBorderP != null && leftBorderP == topBorderP) {
            path.rewind();
            path.moveTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.close();
            canvas.drawPath(path, leftBorderP);
            leftBorderP = null;
            topBorderP = null;
        }
        if (topBorderP != null && topBorderP == rightBorderP) {
            path.rewind();
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.close();
            canvas.drawPath(path, topBorderP);
            topBorderP = null;
            rightBorderP = null;
        }
        if (rightBorderP != null && rightBorderP == bottomBorderP) {
            path.rewind();
            path.moveTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.close();
            canvas.drawPath(path, rightBorderP);
            rightBorderP = null;
            bottomBorderP = null;
        }
        if (bottomBorderP != null && bottomBorderP == leftBorderP) {
            path.rewind();
            path.moveTo(width, height);
            path.lineTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.close();
            canvas.drawPath(path, bottomBorderP);
            bottomBorderP = null;
            leftBorderP = null;
        }
        if (leftBorderP != null && leftBorderP == topBorderP) {
            path.rewind();
            path.moveTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.close();
            canvas.drawPath(path, leftBorderP);
            leftBorderP = null;
            topBorderP = null;
        }
        if (topBorderP != null) {
            path.rewind();
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.close();
            canvas.drawPath(path, topBorderP);
        }
        if (rightBorderP != null) {
            path.rewind();
            path.moveTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, topBorderWidth);
            path.close();
            canvas.drawPath(path, rightBorderP);
        }
        if (bottomBorderP != null) {
            path.rewind();
            path.moveTo(width, height);
            path.lineTo(0, height);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.lineTo(width - rightBorderWidth, height - bottomBorderWidth);
            path.close();
            canvas.drawPath(path, bottomBorderP);
        }
        if (leftBorderP != null) {
            path.rewind();
            path.moveTo(0, height);
            path.lineTo(0, 0);
            path.lineTo(leftBorderWidth, topBorderWidth);
            path.lineTo(leftBorderWidth, height - bottomBorderWidth);
            path.close();
            canvas.drawPath(path, leftBorderP);
        }
    }

    public void setBackground(Object value) {
        int newColor = ColorUtils.toColor(value);
        if (backgroundColor != newColor) {
            backgroundColor = newColor;
            backgroundPaint = null;
            owner.invalidateView();
        }
    }

    public void setBackgroundColor(Object value) {
        setBackground(value);
    }

    public void setBorderColor(int what, Object value) {
        int newColor = ColorUtils.toColor(value);
        if (newColor == Color.TRANSPARENT) {
            if (borderColor == null) return;
            borderColor.unset(what);
        } else {
            if (borderColor == null) borderColor = new IntSpacing();
            borderColor.set(what, newColor);
        }
        leftBorderPaint = null;
        topBorderPaint = null;
        rightBorderPaint = null;
        bottomBorderPaint = null;
        owner.invalidateView();
    }
}
