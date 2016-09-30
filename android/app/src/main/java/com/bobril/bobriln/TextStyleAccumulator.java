package com.bobril.bobriln;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

public class TextStyleAccumulator {
    SpannableStringBuilder builder;
    TextStyle style = new TextStyle();
    int offset;
    float density;

    int offsetColor;
    int offsetBackground;
    int offsetBoldItalic;
    int offsetUnderline;
    int offsetFontSize;

    int lastColor;
    int lastBackground;
    int lastSimple;
    float lastFontSize;

    public void ResetBuilder(SpannableStringBuilder builder, float density) {
        this.builder = builder;
        this.density = density;
        offset = 0;
        offsetColor = -1;
        offsetBackground = -1;
        offsetBoldItalic = -1;
        offsetUnderline = -1;
        offsetFontSize = -1;
        lastBackground = Color.TRANSPARENT;
        lastColor = Color.TRANSPARENT;
        lastSimple = 0;
        lastFontSize = 0;
    }

    public void append(String text) {
        this.offset += text.length();
        this.builder.append(text);
    }

    void FlushColor() {
        if (offsetColor >= 0) {
            if (offsetColor < offset) {
                builder.setSpan(new ForegroundColorSpan(lastColor), offsetColor, offset, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetColor = -1;
        }
    }

    void FlushBackground() {
        if (offsetBackground >= 0) {
            if (offsetBackground < offset) {
                builder.setSpan(new BackgroundColorSpan(lastBackground), offsetBackground, offset, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetBackground = -1;
        }
    }

    void FlushUnderline() {
        if (offsetUnderline >= 0) {
            if (offsetUnderline < offset) {
                builder.setSpan(new UnderlineSpan(), offsetUnderline, offset, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetUnderline = -1;
        }
    }

    void FlushBoldItalic() {
        if (offsetBoldItalic >= 0) {
            if (offsetBoldItalic < offset && (lastSimple & TextStyle.F_BOLD) != 0 || (lastSimple & TextStyle.F_ITALIC) != 0) {
                int style = Typeface.NORMAL;
                if ((lastSimple & TextStyle.F_BOLD) != 0) style |= Typeface.BOLD;
                if ((lastSimple & TextStyle.F_ITALIC) != 0) style |= Typeface.ITALIC;
                builder.setSpan(new StyleSpan(style), offsetBoldItalic, offset, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetBoldItalic = -1;
        }
    }

    void FlushFontSize() {
        if (offsetFontSize >= 0) {
            if (offsetFontSize < offset) {
                builder.setSpan(new AbsoluteSizeSpan(Math.round(lastFontSize * density), false), offsetFontSize, offset, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            offsetFontSize = -1;
        }
    }

    public void Flush() {
        FlushColor();
        FlushBackground();
        FlushBoldItalic();
        FlushFontSize();
        FlushUnderline();
    }

    public void ApplyTextStyle(TextStyle style) {
        if (offsetColor < 0 || lastColor != style.color) {
            FlushColor();
            offsetColor = offset;
            lastColor = style.color;
        }
        if (lastBackground != style.background) {
            FlushBackground();
            if (style.background != Color.TRANSPARENT) {
                offsetBackground = offset;
                lastBackground = style.background;
            }
        }
        if ((lastSimple & TextStyle.F_BOLD) != (style.simple & TextStyle.F_BOLD)) {
            FlushBoldItalic();
            lastSimple &= ~TextStyle.F_BOLD;
            if ((style.simple & TextStyle.F_BOLD) != 0) {
                offsetBoldItalic = offset;
                lastSimple |= TextStyle.F_BOLD;
            }
        }
        if ((lastSimple & TextStyle.F_ITALIC) != (style.simple & TextStyle.F_ITALIC)) {
            FlushBoldItalic();
            lastSimple &= ~TextStyle.F_ITALIC;
            if ((style.simple & TextStyle.F_ITALIC) != 0) {
                offsetBoldItalic = offset;
                lastSimple |= TextStyle.F_ITALIC;
            }
        }
        if ((lastSimple & TextStyle.F_UNDERLINE) != (style.simple & TextStyle.F_UNDERLINE)) {
            FlushUnderline();
            lastSimple &= ~TextStyle.F_UNDERLINE;
            if ((style.simple & TextStyle.F_UNDERLINE) != 0) {
                offsetUnderline = offset;
                lastSimple |= TextStyle.F_UNDERLINE;
            }
        }
        if (lastFontSize != style.fontSize) {
            FlushFontSize();
            offsetFontSize = offset;
            lastFontSize = style.fontSize;
        }
    }

    public void appendView(VNodeViewBased node) {
        this.offset++;
        this.builder.append("B");
        this.builder.setSpan(new VNodeSpan(node),offset-1,offset,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private class VNodeSpan extends ReplacementSpan {
        VNodeViewBased node;

        public VNodeSpan(VNodeViewBased node) {
            this.node = node;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            int height = Math.round(node.css.getLayoutHeight());
            int width = Math.round(node.css.getLayoutWidth());
            if (fm != null) {
                fm.ascent = -height;
                fm.descent = 0;

                fm.top = fm.ascent;
                fm.bottom = 0;
            }
            return width;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            canvas.save();

            int transY = bottom - Math.round(node.css.getLayoutHeight());
            //if (mVerticalAlignment == ALIGN_BASELINE) {
            //    transY -= paint.getFontMetricsInt().descent;
            //}

            canvas.translate(x, transY);
            node.view.draw(canvas);
            canvas.restore();
        }
    }
}
