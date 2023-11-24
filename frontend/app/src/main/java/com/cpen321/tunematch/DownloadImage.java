package com.cpen321.tunematch;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class DownloadImage implements Runnable {
    private final ImageView image;
    private final String url;
    private final int placeholder;

    // ChatGPT Usage: Partial
    public DownloadImage(ImageView image, String url, int placeholder) {
        this.image = image;
        this.url = url;
        this.placeholder = placeholder;
    }

    // ChatGPT Usage: Partial
    @Override
    public void run() {
        try {
            if (url != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.get()
                                .load(url)
                                .placeholder(placeholder) // Set the default image
                                .error(placeholder) // Use the default image in case of an error
                                .transform(new CircleTransform()) // Circle crop
                                .into(image);
                    }
                });
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int widthLight = source.getWidth();
            int heightLight = source.getHeight();

            Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(output);
            Paint paintColor = new Paint();
            paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

            RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

            canvas.drawRoundRect(rectF, widthLight / 2, heightLight / 2, paintColor);

            Paint paintImage = new Paint();
            paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            canvas.drawBitmap(source, 0, 0, paintImage);

            source.recycle();
            return output;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}