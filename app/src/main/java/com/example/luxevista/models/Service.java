package com.example.luxevista.models;

import com.google.firebase.firestore.Exclude;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Service {
    private String serviceId;
    private String name;
    private String category;
    private double price;
    private String currency;
    private String description;
    private int durationMinutes;
    private List<String> imageUrls;

    // Default constructor required for Firestore
    public Service() {}

    // Constructor
    public Service(String serviceId, String name, String category, double price, String currency,
                   String description, int durationMinutes, List<String> imageUrls) {
        this.serviceId = serviceId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.currency = currency;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.imageUrls = imageUrls;
    }

    // Getters
    public String getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    // Setters
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Helper methods
    @Exclude
    public String getFormattedPrice() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(price);
    }

    @Exclude
    public String getFormattedDuration() {
        if (durationMinutes < 60) {
            return durationMinutes + "min";
        } else {
            int hours = durationMinutes / 60;
            int remainingMinutes = durationMinutes % 60;
            if (remainingMinutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + remainingMinutes + "m";
            }
        }
    }

    @Exclude
    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    @Exclude
    public boolean matchesSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        return (name != null && name.toLowerCase().contains(lowerQuery)) ||
               (category != null && category.toLowerCase().contains(lowerQuery)) ||
               (description != null && description.toLowerCase().contains(lowerQuery));
    }

    @Exclude
    public boolean matchesPriceRange(double minPrice, double maxPrice) {
        return price >= minPrice && price <= maxPrice;
    }

    @Exclude
    public boolean matchesCategory(List<String> selectedCategories) {
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return true;
        }
        
        return selectedCategories.contains(category);
    }
}
