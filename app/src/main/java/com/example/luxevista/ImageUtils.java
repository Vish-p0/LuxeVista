package com.example.luxevista;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * Utility class for handling image loading with Glide and sensible fallbacks.
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    private static final RequestOptions DEFAULT = new RequestOptions()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_not_found)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop();

    public static void loadImageWithFallback(ImageView imageView, String imageUrl) {
        if (imageView == null) return;
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }
        String url = normalizeImgurUrl(imageUrl);
        Glide.with(imageView.getContext())
                .load(url)
                .apply(DEFAULT)
                .listener(new LogListener())
                .into(imageView);
    }

    public static void loadProfileImageWithFallback(ImageView imageView, String imageUrl) {
        if (imageView == null) return;
        RequestOptions opts = new RequestOptions()
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop();

        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(R.drawable.profile_placeholder);
            return;
        }
        Glide.with(imageView.getContext())
                .load(normalizeImgurUrl(imageUrl))
                .apply(opts)
                .listener(new LogListener())
                .into(imageView);
    }

    public static void loadItemImageWithFallback(ImageView imageView, String imageUrl, String itemType) {
        if (imageView == null) return;
        int placeholder = R.drawable.ic_image_placeholder;
        if ("room".equals(itemType)) placeholder = R.drawable.ic_rooms;
        else if ("service".equals(itemType)) placeholder = R.drawable.ic_services;

        RequestOptions opts = new RequestOptions()
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop();

        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(placeholder);
            return;
        }
        Glide.with(imageView.getContext())
                .load(normalizeImgurUrl(imageUrl))
                .apply(opts)
                .listener(new LogListener())
                .into(imageView);
    }

    private static class LogListener implements RequestListener<android.graphics.drawable.Drawable> {
        @Override
        public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
            Log.e(TAG, "Glide load failed for: " + model, e);
            return false;
        }

        @Override
        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    }

    // Convert common Imgur page links to direct asset links (i.imgur.com/.. .jpg)
    private static String normalizeImgurUrl(String url) {
        if (TextUtils.isEmpty(url)) return url;
        String lower = url.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
            return url;
        }
        try {
            Uri uri = Uri.parse(url);
            if ("imgur.com".equalsIgnoreCase(uri.getHost()) && uri.getPathSegments().size() >= 1) {
                String id = uri.getLastPathSegment();
                return "https://i.imgur.com/" + id + ".jpg";
            }
        } catch (Exception ignored) {}
        return url;
    }
}
