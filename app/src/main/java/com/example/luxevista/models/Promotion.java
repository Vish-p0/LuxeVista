package com.example.luxevista.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Promotion {
    private String promotionId;
    private String title;
    private String description;
    private String imageUrl;  // Matches Firestore field name
    private Timestamp startAt;
    private Timestamp endAt;
    private Map<String, Object> target;
    private String promoCode;
    private int discountPercent;

    // Default constructor required for Firestore
    public Promotion() {}

    // Constructor
    public Promotion(String promotionId, String title, String description, String imageUrl, Timestamp startAt,
                     Timestamp endAt, Map<String, Object> target, String promoCode, int discountPercent) {
        this.promotionId = promotionId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.startAt = startAt;
        this.endAt = endAt;
        this.target = target;
        this.promoCode = promoCode;
        this.discountPercent = discountPercent;
    }

    // Getters
    public String getPromotionId() {
        return promotionId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() { return imageUrl; }
    public String getImageUrl() { return imageUrl; }

    public Timestamp getStartAt() {
        return startAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public Map<String, Object> getTarget() {
        return target;
    }

    public String getPromoCode() { return promoCode; }
    public int getDiscountPercent() { return discountPercent; }

    // Setters
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageURL(String imageURL) { this.imageUrl = imageURL; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(Timestamp endAt) {
        this.endAt = endAt;
    }

    public void setTarget(Map<String, Object> target) {
        this.target = target;
    }

    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

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

    // keep compatibility helper if needed
}
