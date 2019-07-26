package com.volokh.danylo.hashtaghelper;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a helper class that should be used with {@link android.widget.EditText} or {@link android.widget.TextView}
 * In order to have hash-tagged words highlighted. It also provides a click listeners for every hashtag
 *
 * Example :
 * #ThisIsHashTagWord
 * #ThisIsFirst#ThisIsSecondHashTag
 * #hashtagendsifitfindsnotletterornotdigitsignlike_thisIsNotHighlithedArea
 *
 */
public final class AnyTagHelper implements ClickableForegroundColorSpan.OnTagClickListener {

    /**
     * If this is not null then  all of the symbols in the List will be considered as valid symbols of hashtag
     * For example :
     * mAdditionalHashTagChars = {'$','_','-'}
     * it means that hashtag: "#this_is_hashtag-with$dollar-sign" will be highlighted.
     *
     * Note: if mAdditionalHashTagChars would be "null" only "#this" would be highlighted
     *
     */
    private final List<Character> mAdditionalTagChars;
    private TextView mTextView;
    private int mHashTagWordColor;
    private int mAtTagWordColor;

    private OnTagClickListener mOnTagClickListener;

    public static final class Creator{

        private Creator(){}

        public static AnyTagHelper create(int hashTagColor, int atTagColor){
            return new AnyTagHelper(hashTagColor, atTagColor, null);
        }

        public static AnyTagHelper create(int hashTagColor, int atTagColor, char... additionalTagChars){
            return new AnyTagHelper(hashTagColor, atTagColor, additionalTagChars);
        }

    }

    public interface OnTagClickListener{
        void onHashTagClicked(String hashTag);
        void onAtTagClicked(String atTag);
    }

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            if (text.length() > 0) {
                eraseAndColorizeAllText(text);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private AnyTagHelper(int hashTagColor, int atTagColor, char... additionalHashTagCharacters) {
        mHashTagWordColor = hashTagColor;
        mAtTagWordColor = atTagColor;
        mAdditionalTagChars = new ArrayList<>();

        if(additionalHashTagCharacters != null){
            for(char additionalChar : additionalHashTagCharacters){
                mAdditionalTagChars.add(additionalChar);
            }
        }
    }

    public void handle(TextView textView){
        if(mTextView == null){
            mTextView = textView;
            mTextView.addTextChangedListener(mTextWatcher);

            // in order to use spannable we have to set buffer type
            mTextView.setText(mTextView.getText(), TextView.BufferType.SPANNABLE);

            if(mOnTagClickListener != null){
                // we need to set this in order to get onClick event
                mTextView.setMovementMethod(LinkMovementMethod.getInstance());

                // after onClick clicked text become highlighted
                mTextView.setHighlightColor(Color.TRANSPARENT);
            } else {
                // hash tags are not clickable, no need to change these parameters
            }

            setColorsToAllTags(mTextView.getText());
        } else {
            throw new RuntimeException("TextView is not null. You need to create a unique HashTagHelper for every TextView");
        }

    }

    private void eraseAndColorizeAllText(CharSequence text) {

        Spannable spannable = ((Spannable) mTextView.getText());

        CharacterStyle[] spans = spannable.getSpans(0, text.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            spannable.removeSpan(span);
        }

        setColorsToAllTags(text);
    }

    private void setColorsToAllTags(CharSequence text) {

        int startIndexOfNextSign;

        int index = 0;
        while (index < text.length()-  1){
            char sign = text.charAt(index);
            int nextNotLetterDigitCharIndex = index + 1; // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
            if(sign == '#'){
                startIndexOfNextSign = index;

                nextNotLetterDigitCharIndex = findNextValidTagChar(text, startIndexOfNextSign);

                setColorForHashTagToTheEnd(startIndexOfNextSign, nextNotLetterDigitCharIndex);
            }else if(sign == '@'){
                startIndexOfNextSign = index;

                nextNotLetterDigitCharIndex = findNextValidTagChar(text, startIndexOfNextSign);

                setColorForAtTagToTheEnd(startIndexOfNextSign, nextNotLetterDigitCharIndex);
            }

            index = nextNotLetterDigitCharIndex;
        }
    }

    private int findNextValidTagChar(CharSequence text, int start) {

        int nonLetterDigitCharIndex = -1; // skip first sign '#"
        for (int index = start + 1; index < text.length(); index++) {

            char sign = text.charAt(index);

            boolean isValidSign = Character.isLetterOrDigit(sign) || mAdditionalTagChars.contains(sign);
            if (!isValidSign) {
                nonLetterDigitCharIndex = index;
                break;
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length();
        }

        return nonLetterDigitCharIndex;
    }

    private void setColorForHashTagToTheEnd(int startIndex, int nextNotLetterDigitCharIndex) {
        Spannable s = (Spannable) mTextView.getText();

        CharacterStyle span;

        if(mOnTagClickListener != null){
            span = new ClickableForegroundColorSpan(mHashTagWordColor, this);
        } else {
            // no need for clickable span because it is messing with selection when click
            span = new ForegroundColorSpan(mHashTagWordColor);
        }

        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    private void setColorForAtTagToTheEnd(int startIndex, int nextNotLetterDigitCharIndex) {
        Spannable s = (Spannable) mTextView.getText();

        CharacterStyle span;

        if(mOnTagClickListener != null){
            span = new ClickableForegroundColorSpan(mAtTagWordColor, this);
        } else {
            // no need for clickable span because it is messing with selection when click
            span = new ForegroundColorSpan(mAtTagWordColor);
        }

        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public List<String> getAllHashTags(boolean withHashes) {

        String text = mTextView.getText().toString();
        Spannable spannable = (Spannable) mTextView.getText();

        // use set to exclude duplicates
        Set<String> hashTags = new LinkedHashSet<>();

        for (CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            if(text.substring(spannable.getSpanStart(span), spannable.getSpanEnd(span)).startsWith("#")) {
                hashTags.add(
                        text.substring(!withHashes ? spannable.getSpanStart(span) + 1/*skip "#" sign*/
                                        : spannable.getSpanStart(span),
                                spannable.getSpanEnd(span)));
            }
        }

        return new ArrayList<>(hashTags);
    }

    public List<String> getAllHashTags() {
        return getAllHashTags(false);
    }

    public List<String> getAllAtTags(boolean withAts) {

        String text = mTextView.getText().toString();
        Spannable spannable = (Spannable) mTextView.getText();

        // use set to exclude duplicates
        Set<String> atTags = new LinkedHashSet<>();

        for (CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            if(text.substring(spannable.getSpanStart(span), spannable.getSpanEnd(span)).startsWith("@")){
                atTags.add(
                        text.substring(!withAts ? spannable.getSpanStart(span) + 1/*skip "@" sign*/
                                        : spannable.getSpanStart(span),
                                spannable.getSpanEnd(span)));
            }
        }

        return new ArrayList<>(atTags);
    }

    public List<String> getAllAtTags() {
        return getAllAtTags(false);
    }

    public void setOnTagClickListener(OnTagClickListener listener){
        this.mOnTagClickListener = listener;
    }
    @Override
    public void onHashTagClicked(String hashTag) {
        mOnTagClickListener.onHashTagClicked(hashTag);
    }

    @Override
    public void onAtTagClicked(String atTag) {
        mOnTagClickListener.onAtTagClicked(atTag);

    }
}
