package com.example.luxevista;

import android.widget.ImageView;

/**
 * Utility class for handling image loading with fallback support
 * across all fragments in the app
 */
public class ImageUtils {
    
    /**
     * Loads an image with fallback to placeholder
     * 
     * In a production app, you would use Glide or Picasso:
     * 
     * Glide.with(context)
     *     .load(imageUrl)
     *     .placeholder(R.drawable.ic_image_placeholder)
     *     .error(R.drawable.ic_image_not_found)
     *     .into(imageView);
     * 
     * @param imageView The ImageView to load the image into
     * @param imageUrl The URL of the image to load (can be null or empty)
     */
    public static void loadImageWithFallback(ImageView imageView, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // No URL provided, use placeholder
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        } else {
            // In a real app, you would use an image loading library here
            // For now, we'll use the placeholder as well since we don't have actual URLs
            // TODO: Implement actual image loading with Glide/Picasso
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        }
    }
    
    /**
     * Loads a profile image with fallback to profile placeholder
     * 
     * @param imageView The ImageView to load the image into
     * @param imageUrl The URL of the profile image to load (can be null or empty)
     */
    public static void loadProfileImageWithFallback(ImageView imageView, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // No URL provided, use profile placeholder
            imageView.setImageResource(R.drawable.profile_placeholder);
        } else {
            // In a real app, you would use an image loading library here
            // For now, we'll use the placeholder as well
            // TODO: Implement actual image loading with Glide/Picasso
            imageView.setImageResource(R.drawable.profile_placeholder);
        }
    }
    
    /**
     * Loads a room/service image with fallback for bookings
     * 
     * @param imageView The ImageView to load the image into
     * @param imageUrl The URL of the image to load (can be null or empty)
     * @param itemType The type of item ("room" or "service") for type-specific fallbacks
     */
    public static void loadItemImageWithFallback(ImageView imageView, String imageUrl, String itemType) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // No URL provided, use type-specific placeholder
            if ("room".equals(itemType)) {
                imageView.setImageResource(R.drawable.ic_rooms);
            } else if ("service".equals(itemType)) {
                imageView.setImageResource(R.drawable.ic_services);
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            // In a real app, you would use an image loading library here
            // For now, we'll use type-specific placeholders
            if ("room".equals(itemType)) {
                imageView.setImageResource(R.drawable.ic_rooms);
            } else if ("service".equals(itemType)) {
                imageView.setImageResource(R.drawable.ic_services);
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        }
    }
}
