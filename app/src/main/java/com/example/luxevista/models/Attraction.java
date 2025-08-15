package com.example.luxevista.models;

import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.Locale;

public class Attraction {
    private String name;
    private String description;
    private double distanceKM;
    private List<String> imageUrls;  // Changed to match your database field name
    private boolean visible;

    // Default constructor required for Firestore
    public Attraction() {}

    // Constructor
    public Attraction(String name, String description, double distanceKM, 
                     List<String> imageUrls, boolean visible) {
        this.name = name;
        this.description = description;
        this.distanceKM = distanceKM;
        this.imageUrls = imageUrls;
        this.visible = visible;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getDistanceKM() {
        return distanceKM;
    }

    public List<String> getImageUrls() {  // This matches your database field name
        return imageUrls;
    }

    public boolean isVisible() {
        return visible;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDistanceKM(double distanceKM) {
        this.distanceKM = distanceKM;
    }

    public void setImageUrls(List<String> imageUrls) {  // This matches your database field name
        this.imageUrls = imageUrls;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    // Helper methods
    @Exclude
    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    @Exclude
    public String getFormattedDistance() {
        if (distanceKM < 1.0) {
            int meters = (int) (distanceKM * 1000);
            return meters + "m away";
        } else {
            return String.format(Locale.getDefault(), "%.1f km away", distanceKM);
        }
    }

    @Exclude
    public String getShortDescription() {
        if (description == null || description.length() <= 80) {
            return description;
        }
        return description.substring(0, 77) + "...";
    }
}
