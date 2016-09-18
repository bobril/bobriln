package com.bobril.bobriln;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.facebook.csslayout.CSSConstants;
import com.facebook.csslayout.CSSDirection;
import com.facebook.csslayout.Spacing;

public class ViewDecoration {
    VNodeViewBased owner;
    int backgroundColor;
    Paint backgroundPaint;
    IntSpacing borderColor;
    Paint leftBorderPaint;
    Paint topBorderPaint;
    Paint rightBorderPaint;
    Paint bottomBorderPaint;
    // positive in absolute pixels, negative in percentages
    float userBorderRadiusTopLeftX;
    float userBorderRadiusTopLeftY;
    float userBorderRadiusBottomLeftX;
    float userBorderRadiusBottomLeftY;
    float userBorderRadiusTopRightX;
    float userBorderRadiusTopRightY;
    float userBorderRadiusBottomRightX;
    float userBorderRadiusBottomRightY;

    public ViewDecoration(VNodeViewBased owner) {
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
        Paint leftBorderP = (leftBorderWidth == 0) ? null : leftBorderPaint;
        Paint topBorderP = (topBorderWidth == 0) ? null : topBorderPaint;
        Paint rightBorderP = (rightBorderWidth == 0) ? null : rightBorderPaint;
        Paint bottomBorderP = (bottomBorderWidth == 0) ? null : bottomBorderPaint;
        float height = css.getLayoutHeight();
        float width = css.getLayoutWidth();
        float borderRadiusTopLeftX = expandPercentages(userBorderRadiusTopLeftX, width);
        float borderRadiusTopLeftY = expandPercentages(userBorderRadiusTopLeftY, height);
        float borderRadiusBottomLeftX = expandPercentages(userBorderRadiusBottomLeftX, width);
        float borderRadiusBottomLeftY = expandPercentages(userBorderRadiusBottomLeftY, height);
        float borderRadiusTopRightX = expandPercentages(userBorderRadiusTopRightX, width);
        float borderRadiusTopRightY = expandPercentages(userBorderRadiusTopRightY, height);
        float borderRadiusBottomRightX = expandPercentages(userBorderRadiusBottomRightX, width);
        float borderRadiusBottomRightY = expandPercentages(userBorderRadiusBottomRightY, height);
        float[] tempFloatArray = vdom.tempFloatArray;
        tempFloatArray[0] = borderRadiusTopLeftX;
        tempFloatArray[1] = borderRadiusTopRightX;
        fixRadius(tempFloatArray, width);
        borderRadiusTopLeftX = tempFloatArray[0];
        borderRadiusTopRightX = tempFloatArray[1];
        tempFloatArray[0] = borderRadiusBottomLeftX;
        tempFloatArray[1] = borderRadiusBottomRightX;
        fixRadius(tempFloatArray, width);
        borderRadiusBottomLeftX = tempFloatArray[0];
        borderRadiusBottomRightX = tempFloatArray[1];
        tempFloatArray[0] = borderRadiusTopLeftY;
        tempFloatArray[1] = borderRadiusBottomLeftY;
        fixRadius(tempFloatArray, height);
        borderRadiusTopLeftY = tempFloatArray[0];
        borderRadiusBottomLeftY = tempFloatArray[1];
        tempFloatArray[0] = borderRadiusTopRightY;
        tempFloatArray[1] = borderRadiusBottomRightY;
        fixRadius(tempFloatArray, height);
        borderRadiusTopRightY = tempFloatArray[0];
        borderRadiusBottomRightY = tempFloatArray[1];
        Path path = vdom.helperPath;
        RectF tempRect = vdom.tempRectF;
        if (backgroundPaint != null) {
            if (borderRadiusTopLeftX + borderRadiusTopLeftY + borderRadiusTopRightX + borderRadiusTopRightY + borderRadiusBottomRightX + borderRadiusBottomRightY + borderRadiusBottomLeftX + borderRadiusBottomLeftY == 0)
                canvas.drawRect(leftBorderWidth * 0.5f, topBorderWidth * 0.5f, width - rightBorderWidth * 0.5f, height - bottomBorderWidth * 0.5f, backgroundPaint);
            else {
                path.rewind();
                addCorner(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect);
                addCorner(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect);
                addCorner(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect);
                addCorner(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect);
                path.close();
                canvas.drawPath(path, backgroundPaint);
            }
        }
        if (leftBorderP != null && leftBorderP == topBorderP && topBorderP == rightBorderP && rightBorderP == bottomBorderP) {
            path.rewind();
            addCornerOutside90(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect, true);
            addCornerOutside90(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect, false);
            addCornerOutside90(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect, false);
            addCornerOutside90(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect, false);
            path.close();
            addCornerInside90(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 270, path, tempRect, true);
            addCornerInside90(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 180, path, tempRect, false);
            addCornerInside90(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 90, path, tempRect, false);
            addCornerInside90(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 0, path, tempRect, false);
            path.close();
            canvas.drawPath(path, leftBorderP);
            return;
        }
        if (leftBorderP != null && leftBorderP == topBorderP && topBorderP == rightBorderP) {
            path.rewind();
            addCornerOutside45(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 135, path, tempRect, true);
            addCornerOutside90(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect, false);
            addCornerOutside90(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect, false);
            addCornerOutside45(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect, false);
            addCornerInside45(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 45, path, tempRect, false);
            addCornerInside90(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 0, path, tempRect, false);
            addCornerInside90(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 270, path, tempRect, false);
            addCornerInside45(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 180, path, tempRect, false);
            path.close();
            canvas.drawPath(path, leftBorderP);
            leftBorderP = null;
            topBorderP = null;
            rightBorderP = null;
        }
        if (topBorderP != null && topBorderP == rightBorderP && rightBorderP == bottomBorderP) {
            path.rewind();
            addCornerOutside45(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 225, path, tempRect, true);
            addCornerOutside90(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect, false);
            addCornerOutside90(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect, false);
            addCornerOutside45(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect, false);
            addCornerInside45(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 135, path, tempRect, false);
            addCornerInside90(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 90, path, tempRect, false);
            addCornerInside90(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 0, path, tempRect, false);
            addCornerInside45(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 270, path, tempRect, false);
            path.close();
            canvas.drawPath(path, topBorderP);
            topBorderP = null;
            rightBorderP = null;
            bottomBorderP = null;
        }
        if (rightBorderP != null && rightBorderP == bottomBorderP && bottomBorderP == leftBorderP) {
            path.rewind();
            addCornerOutside45(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, -45, path, tempRect, true);
            addCornerOutside90(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect, false);
            addCornerOutside90(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect, false);
            addCornerOutside45(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect, false);
            addCornerInside45(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 225, path, tempRect, false);
            addCornerInside90(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 180, path, tempRect, false);
            addCornerInside90(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 90, path, tempRect, false);
            addCornerInside45(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 0, path, tempRect, false);
            path.close();
            canvas.drawPath(path, rightBorderP);
            rightBorderP = null;
            bottomBorderP = null;
            leftBorderP = null;
        }
        if (bottomBorderP != null && bottomBorderP == leftBorderP && leftBorderP == topBorderP) {
            path.rewind();
            addCornerOutside45(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 45, path, tempRect, true);
            addCornerOutside90(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect, false);
            addCornerOutside90(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect, false);
            addCornerOutside45(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect, false);
            addCornerInside45(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, -45, path, tempRect, false);
            addCornerInside90(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 270, path, tempRect, false);
            addCornerInside90(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 180, path, tempRect, false);
            addCornerInside45(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 90, path, tempRect, false);
            path.close();
            canvas.drawPath(path, bottomBorderP);
            bottomBorderP = null;
            leftBorderP = null;
            topBorderP = null;
        }
        if (leftBorderP != null && leftBorderP == topBorderP) {
            path.rewind();
            addCornerOutside45(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 135, path, tempRect, true);
            addCornerOutside90(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect, false);
            addCornerOutside45(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect, false);
            addCornerInside45(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, -45, path, tempRect, false);
            addCornerInside90(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 270, path, tempRect, false);
            addCornerInside45(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 180, path, tempRect, false);
            path.close();
            canvas.drawPath(path, leftBorderP);
            leftBorderP = null;
            topBorderP = null;
        }
        if (topBorderP != null && topBorderP == rightBorderP) {
            path.rewind();
            addCornerOutside45(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 225, path, tempRect, true);
            addCornerOutside90(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect, false);
            addCornerOutside45(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect, false);
            addCornerInside45(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 45, path, tempRect, false);
            addCornerInside90(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 0, path, tempRect, false);
            addCornerInside45(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 270, path, tempRect, false);
            path.close();
            canvas.drawPath(path, topBorderP);
            topBorderP = null;
            rightBorderP = null;
        }
        if (rightBorderP != null && rightBorderP == bottomBorderP) {
            path.rewind();
            addCornerOutside45(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, -45, path, tempRect, true);
            addCornerOutside90(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect, false);
            addCornerOutside45(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect, false);
            addCornerInside45(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 135, path, tempRect, false);
            addCornerInside90(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 90, path, tempRect, false);
            addCornerInside45(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 0, path, tempRect, false);
            path.close();
            canvas.drawPath(path, rightBorderP);
            rightBorderP = null;
            bottomBorderP = null;
        }
        if (bottomBorderP != null && bottomBorderP == leftBorderP) {
            path.rewind();
            addCornerOutside45(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 45, path, tempRect, true);
            addCornerOutside90(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect, false);
            addCornerOutside45(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect, false);
            addCornerInside45(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 225, path, tempRect, false);
            addCornerInside90(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 180, path, tempRect, false);
            addCornerInside45(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 90, path, tempRect, false);
            path.close();
            canvas.drawPath(path, bottomBorderP);
            bottomBorderP = null;
            leftBorderP = null;
        }
        if (topBorderP != null) {
            path.rewind();
            addCornerOutside45(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 225, path, tempRect, true);
            addCornerOutside45(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 270, path, tempRect, false);
            addCornerInside45(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, -45, path, tempRect, false);
            addCornerInside45(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 270, path, tempRect, false);
            path.close();
            canvas.drawPath(path, topBorderP);
        }
        if (rightBorderP != null) {
            path.rewind();
            addCornerOutside45(width, 0, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, -45, path, tempRect, true);
            addCornerOutside45(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 0, path, tempRect, false);
            addCornerInside45(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 45, path, tempRect, false);
            addCornerInside45(width, 0, rightBorderWidth, topBorderWidth, borderRadiusTopRightX, borderRadiusTopRightY, -1, 1, 0, path, tempRect, false);
            path.close();
            canvas.drawPath(path, rightBorderP);
        }
        if (bottomBorderP != null) {
            path.rewind();
            addCornerOutside45(width, height, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 45, path, tempRect, true);
            addCornerOutside45(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 90, path, tempRect, false);
            addCornerInside45(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 135, path, tempRect, false);
            addCornerInside45(width, height, rightBorderWidth, bottomBorderWidth, borderRadiusBottomRightX, borderRadiusBottomRightY, -1, -1, 90, path, tempRect, false);
            path.close();
            canvas.drawPath(path, bottomBorderP);
        }
        if (leftBorderP != null) {
            path.rewind();
            addCornerOutside45(0, height, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 135, path, tempRect, true);
            addCornerOutside45(0, 0, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 180, path, tempRect, false);
            addCornerInside45(0, 0, leftBorderWidth, topBorderWidth, borderRadiusTopLeftX, borderRadiusTopLeftY, 1, 1, 225, path, tempRect, false);
            addCornerInside45(0, height, leftBorderWidth, bottomBorderWidth, borderRadiusBottomLeftX, borderRadiusBottomLeftY, 1, -1, 180, path, tempRect, false);
            path.close();
            canvas.drawPath(path, leftBorderP);
        }
    }

    private void addCornerInside90(float x, float y, float borderWidthX, float borderWidthY, float radiusX, float radiusY, float whichX, float whichY, int angle, Path path, RectF tempRect, boolean forceMove) {
        if (radiusX + radiusY == 0) {
            if (forceMove)
                path.moveTo(x + whichX * borderWidthX, y + whichY * borderWidthY);
            else
                path.lineTo(x + whichX * borderWidthX, y + whichY * borderWidthY);
        } else {
            float cx = x + whichX * radiusX;
            float cy = y + whichY * radiusY;
            float rx = radiusX - borderWidthX;
            float ry = radiusY - borderWidthY;
            tempRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
            path.arcTo(tempRect, angle, -90, forceMove);
        }
    }

    private void addCornerInside45(float x, float y, float borderWidthX, float borderWidthY, float radiusX, float radiusY, float whichX, float whichY, int angle, Path path, RectF tempRect, boolean forceMove) {
        if (radiusX + radiusY == 0) {
            if (forceMove)
                path.moveTo(x + whichX * borderWidthX, y + whichY * borderWidthY);
            else
                path.lineTo(x + whichX * borderWidthX, y + whichY * borderWidthY);
        } else {
            float cx = x + whichX * radiusX;
            float cy = y + whichY * radiusY;
            float rx = radiusX - borderWidthX;
            float ry = radiusY - borderWidthY;
            tempRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
            path.arcTo(tempRect, angle, -45, forceMove);
        }
    }

    private void addCornerOutside90(float x, float y, float radiusX, float radiusY, float whichX, float whichY, int angle, Path path, RectF tempRect, boolean forceMove) {
        if (radiusX + radiusY == 0) {
            if (forceMove)
                path.moveTo(x, y);
            else
                path.lineTo(x, y);
        } else {
            float cx = x + whichX * radiusX;
            float cy = y + whichY * radiusY;
            float rx = radiusX;
            float ry = radiusY;
            tempRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
            path.arcTo(tempRect, angle, 90, forceMove);
        }
    }

    private void addCornerOutside45(float x, float y, float radiusX, float radiusY, float whichX, float whichY, int angle, Path path, RectF tempRect, boolean forceMove) {
        if (radiusX + radiusY == 0) {
            if (forceMove)
                path.moveTo(x, y);
            else
                path.lineTo(x, y);
        } else {
            float cx = x + whichX * radiusX;
            float cy = y + whichY * radiusY;
            float rx = radiusX;
            float ry = radiusY;
            tempRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
            path.arcTo(tempRect, angle, 45, forceMove);
        }
    }

    private void addCorner(float x, float y, float borderWidthX, float borderWidthY, float radiusX, float radiusY, float whichX, float whichY, int angle, Path path, RectF tempRect) {
        if (radiusX + radiusY == 0) {
            if (path.isEmpty())
                path.moveTo(x + whichX * borderWidthX * 0.5f, y + whichY * borderWidthY * 0.5f);
            else
                path.lineTo(x + whichX * borderWidthX * 0.5f, y + whichY * borderWidthY * 0.5f);
        } else {
            float cx = x + whichX * radiusX;
            float cy = y + whichY * radiusY;
            float rx = radiusX - borderWidthX * 0.5f;
            float ry = radiusY - borderWidthY * 0.5f;
            tempRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
            path.arcTo(tempRect, angle, 90, false);
        }
    }

    private void fixRadius(float[] floats, float max) {
        float sum = floats[0] + floats[1];
        if (sum > max) {
            float koef = max / sum;
            floats[0] *= koef;
            floats[1] *= koef;
        }
    }

    private float expandPercentages(float value, float full) {
        return value < 0 ? -value * full : value;
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

    public void parseRadiusValue(Object value, float[] result) {
        if (value == null) {
            result[0] = 0;
            result[1] = 0;
            return;
        }
        if (value instanceof Double) {
            result[0] = Math.max(0, ((Double) value).floatValue());
            result[0] *= owner.vdom.density;
            result[1] = result[0];
            return;
        }
        if (value instanceof Float) {
            result[0] = Math.max(0, (Float) value);
            result[0] *= owner.vdom.density;
            result[1] = result[0];
            return;
        }
        if (value instanceof Integer) {
            result[0] = Math.max(0, ((Integer) value).floatValue());
            result[0] *= owner.vdom.density;
            result[1] = result[0];
            return;
        }
        if (value instanceof String) {
            String str = (String) value;
            int[] pos = owner.vdom.tempIntArray;
            pos[0] = 0;
            float f = ((float) StringUtils.parseDouble(str, pos));
            if (pos[0] == 0) {
                // error should start with number
                result[0] = 0;
                result[1] = 0;
                return;
            }
            if (f < 0) f = 0;
            if (StringUtils.parseString(str, pos, "%")) {
                f = -f;
            } else {
                StringUtils.parseString(str, pos, "px");
                f *= owner.vdom.density;
            }
            result[0] = f;
            StringUtils.skipWhiteSpace(str, pos);
            if (!StringUtils.isEOS(str, pos)) {
                int posbackup = pos[0];
                f = ((float) StringUtils.parseDouble(str, pos));
                if (pos[0] == posbackup) {
                    // error should continue with number
                    result[0] = 0;
                    result[1] = 0;
                    return;
                }
                if (f < 0) f = 0;
                if (StringUtils.parseString(str, pos, "%")) {
                    f = -f;
                } else {
                    StringUtils.parseString(str, pos, "px");
                    f *= owner.vdom.density;
                }
            }
            result[1] = f;
            return;
        }
        result[0] = 0;
        result[1] = 0;
        return;
    }

    public void parseRadiusValueSlash(Object value, float[] result) {
        if (value == null) {
            result[0] = 0;
            result[1] = 0;
            return;
        }
        if (value instanceof Double) {
            result[0] = Math.max(0, ((Double) value).floatValue());
            result[0] *= owner.vdom.density;
            result[1] = result[0];
            return;
        }
        if (value instanceof Float) {
            result[0] = Math.max(0, (Float) value);
            result[0] *= owner.vdom.density;
            result[1] = result[0];
            return;
        }
        if (value instanceof Integer) {
            result[0] = Math.max(0, ((Integer) value).floatValue());
            result[0] *= owner.vdom.density;
            result[1] = result[0];
            return;
        }
        if (value instanceof String) {
            String str = (String) value;
            int[] pos = owner.vdom.tempIntArray;
            pos[0] = 0;
            float f = ((float) StringUtils.parseDouble(str, pos));
            if (pos[0] == 0) {
                // error should start with number
                result[0] = 0;
                result[1] = 0;
                return;
            }
            if (f < 0) f = 0;
            if (StringUtils.parseString(str, pos, "%")) {
                f = -f;
            } else {
                StringUtils.parseString(str, pos, "px");
                f *= owner.vdom.density;
            }
            result[0] = f;
            StringUtils.skipWhiteSpace(str, pos);
            if (!StringUtils.isEOS(str, pos)) {
                if (!StringUtils.parseString(str, pos, "/")) {
                    throw new RuntimeException("BorderRadius x y values must be delimited by slash");
                }
                StringUtils.skipWhiteSpace(str, pos);
                int posbackup = pos[0];
                f = ((float) StringUtils.parseDouble(str, pos));
                if (pos[0] == posbackup) {
                    // error should continue with number
                    result[0] = 0;
                    result[1] = 0;
                    return;
                }
                if (f < 0) f = 0;
                if (StringUtils.parseString(str, pos, "%")) {
                    f = -f;
                } else {
                    StringUtils.parseString(str, pos, "px");
                    f *= owner.vdom.density;
                }
            }
            result[1] = f;
            return;
        }
        result[0] = 0;
        result[1] = 0;
        return;
    }

    public void setBorderRadius(Object value) {
        float[] v = owner.vdom.tempFloatArray;
        parseRadiusValueSlash(value, v);
        float x = v[0];
        float y = v[1];
        userBorderRadiusTopLeftX = x;
        userBorderRadiusTopLeftY = y;
        userBorderRadiusBottomLeftX = x;
        userBorderRadiusBottomLeftY = y;
        userBorderRadiusTopRightX = x;
        userBorderRadiusTopRightY = y;
        userBorderRadiusBottomRightX = x;
        userBorderRadiusBottomRightY = y;
    }

    public void setBorderTopLeftRadius(Object value) {
        float[] v = owner.vdom.tempFloatArray;
        parseRadiusValue(value, v);
        float x = v[0];
        float y = v[1];
        userBorderRadiusTopLeftX = x;
        userBorderRadiusTopLeftY = y;
    }

    public void setBorderTopRightRadius(Object value) {
        float[] v = owner.vdom.tempFloatArray;
        parseRadiusValue(value, v);
        float x = v[0];
        float y = v[1];
        userBorderRadiusTopRightX = x;
        userBorderRadiusTopRightY = y;
    }

    public void setBorderBottomLeftRadius(Object value) {
        float[] v = owner.vdom.tempFloatArray;
        parseRadiusValue(value, v);
        float x = v[0];
        float y = v[1];
        userBorderRadiusBottomLeftX = x;
        userBorderRadiusBottomLeftY = y;
    }

    public void setBorderBottomRightRadius(Object value) {
        float[] v = owner.vdom.tempFloatArray;
        parseRadiusValue(value, v);
        float x = v[0];
        float y = v[1];
        userBorderRadiusBottomRightX = x;
        userBorderRadiusBottomRightY = y;
    }
}
