package com.volokh.danylo.hashtaghelper;

import android.support.annotation.ColorInt;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Created by danylo.volokh on 12/22/2015.
 * Forked by pedromarthon on 25/07/2019
 * Forked to allow multiple tag types in the same text view and to add newer logic
 * This class is a combination of {@link android.text.style.ForegroundColorSpan}
 * and {@link ClickableSpan}.
 *
 * You can set a color of this span plus set a click listener
 */
public class ClickableForegroundColorSpan extends ClickableSpan {

    private OnTagClickListener mOnHashTagClickListener;

    public interface OnTagClickListener {
        void onHashTagClicked(String hashTag);
        void onAtTagClicked(String atTag);
    }

    private final int mColor;

    public ClickableForegroundColorSpan(@ColorInt int color, OnTagClickListener listener) {
        mColor = color;
        mOnHashTagClickListener = listener;

        if (mOnHashTagClickListener == null) {
            throw new RuntimeException("constructor, click listener not specified. Are you sure you need to use this class?");
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(mColor);
    }

    @Override
    public void onClick(View widget) {
        CharSequence text = ((TextView) widget).getText();

        Spanned s = (Spanned) text;
        int start = s.getSpanStart(this);
        int end = s.getSpanEnd(this);

        if(text.subSequence(start/*skip "#" sign*/, end).toString().startsWith("#")){
            mOnHashTagClickListener.onHashTagClicked(text.subSequence(start + 1/*skip "#" sign*/, end).toString());
        }else if(text.subSequence(start/*skip "#" sign*/, end).toString().startsWith("@")){
            mOnHashTagClickListener.onAtTagClicked(text.subSequence(start + 1/*skip "@" sign*/, end).toString());
        }
    }
}
