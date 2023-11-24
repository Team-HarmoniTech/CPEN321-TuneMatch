package com.cpen321.tunematch;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

public class DownloadImage implements Runnable {
    private final ImageView image;
    private final String url;
    private final int placeholder;
    private List<Transformation> transformations = new ArrayList<>();

    // ChatGPT Usage: Partial
    public DownloadImage(ImageView image, String url, int placeholder) {
        this.image = image;
        this.url = url;
        this.placeholder = placeholder;
    }

    // ChatGPT Usage: Partial
    public DownloadImage(ImageView image, String url, int placeholder, List<Transformation> transformations) {
        this.image = image;
        this.url = url;
        this.placeholder = placeholder;
        this.transformations.addAll(transformations);
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
                                .placeholder(placeholder)
                                .error(placeholder)
                                .transform(transformations)
                                .into(image);
                    }
                });
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}