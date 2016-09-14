package com.bobril.bobriln;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public class TextStyleAccumulator {
    SpannableStringBuilder builder;
    TextStyle style = new TextStyle();
    int offset;

    int offsetColor;
    int offsetBackground;
    int offsetItalic;
    int offsetBold;
    int offsetUnderline;

    int lastColor;
    int lastBackground;
    int lastSimple;

    public void ResetBuilder(SpannableStringBuilder builder) {
        this.builder = builder;
        offset = 0;
        offsetColor = -1;
        offsetBackground = -1;
        offsetItalic = -1;
        offsetBold = -1;
        offsetUnderline = -1;
        lastBackground = Color.TRANSPARENT;
        lastColor = Color.TRANSPARENT;
        lastSimple = 0;
    }

    public void append(String text) {
        this.offset+=text.length();
        this.builder.append(text);
    }

    void FlushColor() {
        if (offsetColor>=0) {
            if (offsetColor<offset) {
                builder.setSpan(new ForegroundColorSpan(lastColor),offsetColor,offset, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetColor = -1;
        }
    }

    void FlushBackground() {
        if (offsetBackground>=0) {
            if (offsetBackground<offset) {
                builder.setSpan(new BackgroundColorSpan(lastBackground),offsetBackground,offset, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetBackground = -1;
        }
    }

    void FlushBold() {
        if (offsetBold>=0) {
            if (offsetBold<offset && (lastSimple & TextStyle.F_BOLD)!=0) {
                builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),offsetBold,offset,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetBold = -1;
        }
    }

    public void Flush() {
        FlushColor();
        FlushBackground();
        FlushBold();
    }

    public void ApplyTextStyle(TextStyle style) {
        if (offsetColor<0 || lastColor != style.color) {
            FlushColor();
            offsetColor = offset;
            lastColor = style.color;
        }
        if (lastBackground!=style.background) {
            FlushBackground();
            if (style.background!=Color.TRANSPARENT) {
                offsetBackground = offset;
                lastBackground = style.background;
            }
        }
        if ((lastSimple&TextStyle.F_BOLD)!=(style.simple&TextStyle.F_BOLD)) {
            FlushBold();
            lastSimple &= ~TextStyle.F_BOLD;
            if ((style.simple&TextStyle.F_BOLD)!=0) {
                offsetBold = offset;
                lastSimple |= TextStyle.F_BOLD;
            }
        }
    }
}
