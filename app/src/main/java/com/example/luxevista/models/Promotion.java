package com.example.luxevista.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Promotion {
    private String title;
    private String description;
    private String imageURL;  // Changed to match your database field name
    private Timestamp startAt;
    private Timestamp endAt;
    private Map<String, Object> target;

    // Default constructor required for Firestore
    public Promotion() {}

    // Constructor
    public Promotion(String title, String description, String imageURL, Timestamp startAt, 
                    Timestamp endAt, Map<String, Object> target) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.startAt = startAt;
        this.endAt = endAt;
        this.target = target;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {  // Changed getter name
        return imageURL;
    }

    public Timestamp getStartAt() {
        return startAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public Map<String, Object> getTarget() {
        return target;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageURL(String imageURL) {  // Changed setter name
        this.imageURL = imageURL;
    }

    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(Timestamp endAt) {
        this.endAt = endAt;
    }

    public void setTarget(Map<String, Object> target) {
        this.target = target;
    }

    // Helper methods
    @Exclude
    public Date getStartDate() {
        return startAt != null ? startAt.toDate() : null;
    }

    @Exclude
    public Date getEndDate() {
        return endAt != null ? endAt.toDate() : null;
    }

    @Exclude
    public boolean isActive() {
        Date now = new Date();
        Date start = getStartDate();
        Date end = getEndDate();
        
        if (start == null || end == null) {
            return false;
        }
        
        return now.compareTo(start) >= 0 && now.compareTo(end) <= 0;
    }

    @Exclude
    public List<String> getTargetRoomTypes() {
        if (target != null && target.containsKey("roomTypes")) {
            Object roomTypes = target.get("roomTypes");
            if (roomTypes instanceof List) {
                return (List<String>) roomTypes;
            }
        }
        return null;
    }

    // For compatibility with existing adapter
    @Exclude
    public String getImageUrl() {
        return imageURL;
    }
}
