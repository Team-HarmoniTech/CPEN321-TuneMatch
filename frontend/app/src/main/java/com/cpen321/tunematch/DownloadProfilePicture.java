package com.cpen321.tunematch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

import java.util.Collections;

public class DownloadProfilePicture extends DownloadImage {
    // ChatGPT Usage: No
    public DownloadProfilePicture(ImageView image, String url) {
        super(image, url, R.drawable.default_profile_image, Collections.singletonList(new CircleTransform()));
    }

    public static class CircleTransform implements Transformation {
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
