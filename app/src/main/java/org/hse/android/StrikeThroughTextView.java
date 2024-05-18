package org.hse.android;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;

public class StrikeThroughTextView extends androidx.appcompat.widget.AppCompatTextView {

    public StrikeThroughTextView(Context context) {
        super(context);
    }

    public StrikeThroughTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StrikeThroughTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new StrikethroughSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        super.setText(spannableString, type);
    }
}
