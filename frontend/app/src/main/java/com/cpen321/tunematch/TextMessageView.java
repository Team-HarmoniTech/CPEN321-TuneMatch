package com.cpen321.tunematch;

import android.content.Context;
import android.util.AttributeSet;

public class TextMessageView extends androidx.appcompat.widget.AppCompatTextView {

    private static final float MAX_WIDTH_PERCENTAGE = 0.75f; // 50% of screen width

    public TextMessageView(Context context) {
        super(context);
    }

    public TextMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * MAX_WIDTH_PERCENTAGE);
        int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
        super.onMeasure(newWidthMeasureSpec, heightMeasureSpec);
    }
}