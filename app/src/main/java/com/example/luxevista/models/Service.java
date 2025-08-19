package com.example.luxevista.models;

import com.google.firebase.firestore.Exclude;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Service {
    private String serviceId;
    private String name;
    private String category;
    private double price;
    private String currency;
    private String description;
    private int durationMinutes;
    private List<String> imageUrls;

    // New availability structure
    private Map<String, String> availableHours; // "start" -> "09:00", "end" -> "21:00"
    private Map<String, Integer> timeSlots; // "09:00" -> 2, "10:00" -> 2, etc.
    private Map<String, Map<String, Integer>> availability; // date (yyyy-MM-dd) -> time -> booked count

    // Legacy fields for backward compatibility
    private int defaultDailySlots;
    private Map<String, Long> legacyAvailability; // date (yyyy-MM-dd) -> booked count

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

    // New availability getters
    public Map<String, String> getAvailableHours() { return availableHours; }
    public Map<String, Integer> getTimeSlots() { return timeSlots; }
    public Map<String, Map<String, Integer>> getAvailability() { return availability; }

    // Legacy getters for backward compatibility
    public int getDefaultDailySlots() { return defaultDailySlots; }
    public Map<String, Long> getLegacyAvailability() { return legacyAvailability; }

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

    // New availability setters
    public void setAvailableHours(Map<String, String> availableHours) { this.availableHours = availableHours; }
    public void setTimeSlots(Map<String, Integer> timeSlots) { this.timeSlots = timeSlots; }
    public void setAvailability(Map<String, Map<String, Integer>> availability) { this.availability = availability; }

    // Legacy setters for backward compatibility
    public void setDefaultDailySlots(int defaultDailySlots) { this.defaultDailySlots = defaultDailySlots; }
    public void setLegacyAvailability(Map<String, Long> legacyAvailability) { this.legacyAvailability = legacyAvailability; }

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

    // New availability methods
    @Exclude
    public String getStartTime() {
        if (availableHours != null && availableHours.containsKey("start")) {
            return availableHours.get("start");
        }
        return "09:00"; // Default start time
    }

    @Exclude
    public String getEndTime() {
        if (availableHours != null && availableHours.containsKey("end")) {
            return availableHours.get("end");
        }
        return "21:00"; // Default end time
    }

    @Exclude
    public List<String> getAvailableTimeSlots() {
        List<String> slots = new java.util.ArrayList<>();
        if (timeSlots != null) {
            slots.addAll(timeSlots.keySet());
        }
        return slots;
    }

    @Exclude
    public int getAvailableSlotsForDateAndTime(String dateKey, String timeKey) {
        if (availability != null && availability.containsKey(dateKey)) {
            Map<String, Integer> dateAvailability = availability.get(dateKey);
            if (dateAvailability != null && dateAvailability.containsKey(timeKey)) {
                return dateAvailability.get(timeKey);
            }
        }
        
        // Fallback to legacy availability or default
        if (timeSlots != null && timeSlots.containsKey(timeKey)) {
            return timeSlots.get(timeKey);
        }
        
        return 0;
    }

    @Exclude
    public boolean hasAvailableSlotsForDate(String dateKey) {
        if (availability != null && availability.containsKey(dateKey)) {
            Map<String, Integer> dateAvailability = availability.get(dateKey);
            if (dateAvailability != null) {
                for (Integer slots : dateAvailability.values()) {
                    if (slots > 0) return true;
                }
            }
        }
        
        // Fallback to legacy availability
        if (legacyAvailability != null && legacyAvailability.containsKey(dateKey)) {
            long booked = legacyAvailability.get(dateKey);
            return booked < defaultDailySlots;
        }
        
        return false;
    }

    @Exclude
    public List<String> getAvailableTimesForDate(String dateKey) {
        List<String> availableTimes = new java.util.ArrayList<>();
        
        if (timeSlots != null) {
            for (Map.Entry<String, Integer> entry : timeSlots.entrySet()) {
                String time = entry.getKey();
                int totalSlots = entry.getValue();
                int bookedSlots = getBookedSlotsForDateAndTime(dateKey, time);
                int availableSlots = totalSlots - bookedSlots;
                
                if (availableSlots > 0) {
                    availableTimes.add(time);
                }
            }
        }
        
        return availableTimes;
    }

    @Exclude
    private int getBookedSlotsForDateAndTime(String dateKey, String timeKey) {
        if (availability != null && availability.containsKey(dateKey)) {
            Map<String, Integer> dateAvailability = availability.get(dateKey);
            if (dateAvailability != null && dateAvailability.containsKey(timeKey)) {
                return dateAvailability.get(timeKey);
            }
        }
        return 0;
    }

    // Legacy compatibility method
    @Exclude
    public int getRemainingForDate(String dateKey) {
        // Try new availability system first
        if (hasAvailableSlotsForDate(dateKey)) {
            List<String> availableTimes = getAvailableTimesForDate(dateKey);
            int totalAvailable = 0;
            for (String time : availableTimes) {
                totalAvailable += getAvailableSlotsForDateAndTime(dateKey, time);
            }
            return totalAvailable;
        }
        
        // Fallback to legacy system
        int dailyCapacity = defaultDailySlots;
        if (dailyCapacity <= 0) return 0;
        long booked = 0L;
        if (legacyAvailability != null && legacyAvailability.containsKey(dateKey) && legacyAvailability.get(dateKey) != null) {
            booked = legacyAvailability.get(dateKey);
        }
        long remaining = (long) dailyCapacity - booked;
        return (int) Math.max(0L, remaining);
    }
}
