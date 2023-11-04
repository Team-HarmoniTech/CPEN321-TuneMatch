package com.cpen321.tunematch;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class DownloadImage implements Runnable {
    private ImageView image;
    private String url;
    private int placeholder;

    public DownloadImage(ImageView image, String url, int placeholder) {
        this.image = image;
        this.url = url;
        this.placeholder = placeholder;
    }

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
                                .into(image);
                    }
                });
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}