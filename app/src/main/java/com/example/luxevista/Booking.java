package com.example.luxevista;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Booking {
    private String bookingId;
    private String userId;
    private String type; // "room" or "package"
    private String itemId;
    private Timestamp startDate;
    private Timestamp endDate;
    private String status; // "confirmed", "cancelled", "pending"
    private double price;
    private String currency;
    private Timestamp createdAt;
    
    // Additional fields for UI
    private String itemName; // Will be fetched from rooms/services collection
    
    // Default constructor required for Firestore
    public Booking() {}
    
    // Full constructor
    public Booking(String bookingId, String userId, String type, String itemId, 
                   Timestamp startDate, Timestamp endDate, String status, 
                   double price, String currency, Timestamp createdAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.type = type;
        this.itemId = itemId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.price = price;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    // Optional: list of services for package bookings
    private java.util.List<String> serviceIds;
    private java.util.Map<String, Integer> serviceQuantities; // dateKey -> count per service, can be extended as needed

    public java.util.List<String> getServiceIds() { return serviceIds; }
    public void setServiceIds(java.util.List<String> serviceIds) { this.serviceIds = serviceIds; }
    public java.util.Map<String, Integer> getServiceQuantities() { return serviceQuantities; }
    public void setServiceQuantities(java.util.Map<String, Integer> serviceQuantities) { this.serviceQuantities = serviceQuantities; }
    
    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public Timestamp getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }
    
    public Timestamp getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    // Helper methods for UI
    public Date getStartDateAsDate() {
        return startDate != null ? startDate.toDate() : null;
    }
    
    public Date getEndDateAsDate() {
        return endDate != null ? endDate.toDate() : null;
    }
    
    public Date getCreatedAtAsDate() {
        return createdAt != null ? createdAt.toDate() : null;
    }
    
    // Check if booking is active (confirmed and end date in future)
    public boolean isActive() {
        if (!"confirmed".equals(status)) {
            return false;
        }
        Date now = new Date();
        Date end = getEndDateAsDate();
        return end != null && end.after(now);
    }
    
    // Check if booking is completed (confirmed and end date in past)
    public boolean isCompleted() {
        if (!"confirmed".equals(status)) {
            return false;
        }
        Date now = new Date();
        Date end = getEndDateAsDate();
        return end != null && end.before(now);
    }
    
    // Check if booking is cancelled
    public boolean isCancelled() {
        return "cancelled".equals(status);
    }
    
    // Get formatted price with currency
    public String getFormattedPrice() {
        if ("USD".equals(currency)) {
            return String.format("$%.2f", price);
        } else {
            return String.format("%.2f %s", price, currency);
        }
    }
    
    // Get capitalized type for display
    public String getDisplayType() {
        if (type == null) return "";
        return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
    }
    
    // Get status color for UI
    public String getStatusColor() {
        switch (status) {
            case "confirmed":
                return isActive() ? "#4CAF50" : "#FF9800"; // Green for active, Orange for completed
            case "cancelled":
                return "#F44336"; // Red
            case "pending":
                return "#FF9800"; // Orange
            default:
                return "#9E9E9E"; // Gray
        }
    }
    
    // Get status display text
    public String getDisplayStatus() {
        if ("confirmed".equals(status)) {
            return isActive() ? "Active" : "Completed";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }
}