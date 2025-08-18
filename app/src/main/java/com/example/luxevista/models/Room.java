package com.example.luxevista.models;

import com.example.luxevista.R;
import com.google.firebase.firestore.Exclude;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Room {
    private String roomId;
    private String name;
    private String type;
    private double pricePerNight;
    private String currency;
    private String description;
    private Map<String, Boolean> amenities;
    private List<String> imageUrls;
    private int maxGuests;
    private boolean visible;

    // Availability
    private int totalRooms;
    private int availableRooms;
    private int defaultDailyRooms;
    private java.util.Map<String, Long> availability; // date (yyyy-MM-dd) -> booked count

    // Default constructor required for Firestore
    public Room() {}

    // Constructor
    public Room(String roomId, String name, String type, double pricePerNight, String currency,
                String description, Map<String, Boolean> amenities, List<String> imageUrls,
                int maxGuests, boolean visible) {
        this.roomId = roomId;
        this.name = name;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.currency = currency;
        this.description = description;
        this.amenities = amenities;
        this.imageUrls = imageUrls;
        this.maxGuests = maxGuests;
        this.visible = visible;
    }

    // Getters
    public String getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Boolean> getAmenities() {
        return amenities;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getTotalRooms() { return totalRooms; }
    public int getAvailableRooms() { return availableRooms; }
    public int getDefaultDailyRooms() { return defaultDailyRooms; }
    public java.util.Map<String, Long> getAvailability() { return availability; }

    // Setters
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmenities(Map<String, Boolean> amenities) {
        this.amenities = amenities;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    public void setDefaultDailyRooms(int defaultDailyRooms) { this.defaultDailyRooms = defaultDailyRooms; }
    public void setAvailability(java.util.Map<String, Long> availability) { this.availability = availability; }

    // Helper methods
    @Exclude
    public String getFormattedPrice() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(pricePerNight) + "/night";
    }

    @Exclude
    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    @Exclude
    public boolean hasAmenity(String amenityKey) {
        return amenities != null && amenities.containsKey(amenityKey) && Boolean.TRUE.equals(amenities.get(amenityKey));
    }

    @Exclude
    public int getAmenityCount() {
        if (amenities == null) return 0;
        int count = 0;
        for (Boolean value : amenities.values()) {
            if (Boolean.TRUE.equals(value)) {
                count++;
            }
        }
        return count;
    }

    @Exclude
    public int getRemainingForDate(String dateKey) {
        int dailyCapacity = defaultDailyRooms;
        if (dailyCapacity <= 0) return 0;
        long booked = 0L;
        if (availability != null && availability.containsKey(dateKey) && availability.get(dateKey) != null) {
            booked = availability.get(dateKey);
        }
        long remaining = (long) dailyCapacity - booked;
        return (int) Math.max(0L, remaining);
    }

    @Exclude
    public boolean matchesSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        return (name != null && name.toLowerCase().contains(lowerQuery)) ||
               (type != null && type.toLowerCase().contains(lowerQuery)) ||
               (description != null && description.toLowerCase().contains(lowerQuery));
    }

    @Exclude
    public boolean matchesPriceRange(double minPrice, double maxPrice) {
        return pricePerNight >= minPrice && pricePerNight <= maxPrice;
    }

    @Exclude
    public boolean hasAnyAmenity(List<String> requiredAmenities) {
        if (requiredAmenities == null || requiredAmenities.isEmpty()) {
            return true;
        }
        
        for (String amenity : requiredAmenities) {
            if (hasAmenity(amenity)) {
                return true;
            }
        }
        return false;
    }

    @Exclude
    public int getAmenityIconResId(String amenityKey) {
        switch (amenityKey) {
            case "airConditioning": return R.drawable.amenities_air_conditioning;
            case "balcony": return R.drawable.amenities_balcony;
            case "coffeeMaker": return R.drawable.amenities_coffee_maker;
            case "jacuzzi": return R.drawable.amenities_jacuzzi;
            case "kingBed": return R.drawable.amenities_king_bed;
            case "miniBar": return R.drawable.amenities_mini_bar;
            case "nonSmoking": return R.drawable.amenities_non_smoking;
            case "oceanView": return R.drawable.amenities_ocean_view;
            case "roomService": return R.drawable.amenities_room_service;
            case "safe": return R.drawable.amenities_safe;
            case "television": return R.drawable.amenities_television;
            case "wheelchairAccessible": return R.drawable.amenities_wheelchair_accessible;
            case "wifi": return R.drawable.amenities_wifi;
            default: return 0; // No icon found
        }
    }
}
