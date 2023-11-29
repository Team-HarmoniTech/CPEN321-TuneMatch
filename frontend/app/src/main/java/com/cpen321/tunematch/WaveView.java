package com.cpen321.tunematch;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class WaveView extends View {
    private static final int NUM_LINES = 5; // Adjust the number of lines as needed
    private final Paint paint;
    private final float amplitude;
    private final float frequency;
    private float phase;

    public WaveView(Context context, AttributeSet attr) {
        super(context, attr);

        amplitude = dpToPx(10); // Default amplitude in dp
        frequency = 1.25f; // Default frequency
        phase = 0;

        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.pointGreen));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animationValue = (float) animation.getAnimatedValue();
                phase = (float) Math.PI * 2 * animationValue;
                invalidate();
            }
        });

        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = dpToPx(30) - 14;
        float height = dpToPx(30);
        float centerY = height / 2f;

        float[] points = new float[NUM_LINES * 4];

        for (int i = 0; i < NUM_LINES * 4; i += 4) {
            float x = (i / 4f) * (width / (NUM_LINES - 1));
            float sine = (float) Math.sin((x / width) * frequency * 2 * Math.PI + phase);
            float yOffset = sine * amplitude;
            points[i] = x + 8;
            points[i + 1] = centerY + yOffset;
            points[i + 2] = x + 8;
            points[i + 3] = height - 5;
        }

        canvas.drawLines(points, paint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
