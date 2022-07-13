package com.example.roomies.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;

public class GlideChip extends Chip {

    public GlideChip(Context context) {
        super(context);
    }

    public GlideChip(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set an image from an URL for the {@link Chip} using {@link com.bumptech.glide.Glide}
     *
     * @param url         icon URL
     * @param errDrawable error icon if Glide return failed response
     */
    public GlideChip setIconUrl(String url, Drawable errDrawable) {
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        setChipIcon(errDrawable);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        setChipIcon(resource);
                        return false;
                    }
                }).preload();
        return this;
    }
}
