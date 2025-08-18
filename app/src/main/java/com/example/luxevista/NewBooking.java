package com.example.luxevista;

import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NewBooking {
    private String bookingId;
    private String userId;
    private Timestamp startDate;
    private Timestamp endDate;
    private String status;
    private String currency;
    private double totalPrice;
    private Timestamp createdAt;
    
    // Arrays for rooms and services
    private List<RoomBooking> rooms;
    private List<ServiceBooking> services;
    
    // Default constructor required for Firestore
    public NewBooking() {}
    
    // Full constructor
    public NewBooking(String bookingId, String userId, Timestamp startDate, Timestamp endDate, 
                     String status, String currency, double totalPrice, Timestamp createdAt,
                     List<RoomBooking> rooms, List<ServiceBooking> services) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.currency = currency;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.rooms = rooms;
        this.services = services;
    }
    
    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    
    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public List<RoomBooking> getRooms() { return rooms; }
    public void setRooms(List<RoomBooking> rooms) { this.rooms = rooms; }
    
    public List<ServiceBooking> getServices() { return services; }
    public void setServices(List<ServiceBooking> services) { this.services = services; }
    
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
            return String.format("$%.2f", totalPrice);
        } else {
            return String.format("%.2f %s", totalPrice, currency);
        }
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
    
    // Get total number of items (rooms + services)
    public int getTotalItems() {
        int total = 0;
        if (rooms != null) total += rooms.size();
        if (services != null) total += services.size();
        return total;
    }
    
    // Get summary text for display
    public String getSummaryText() {
        StringBuilder summary = new StringBuilder();
        
        if (rooms != null && !rooms.isEmpty()) {
            summary.append(rooms.size()).append(" room(s)");
        }
        
        if (services != null && !services.isEmpty()) {
            if (summary.length() > 0) summary.append(" + ");
            summary.append(services.size()).append(" service(s)");
        }
        
        return summary.toString();
    }
    
    // Inner class for room bookings
    public static class RoomBooking {
        private String roomId;
        private int quantity;
        private double pricePerNight;
        private int nights;
        private double subTotal;
        private String roomName; // Will be fetched from Firestore
        
        public RoomBooking() {}
        
        public RoomBooking(String roomId, int quantity, double pricePerNight, int nights, double subTotal) {
            this.roomId = roomId;
            this.quantity = quantity;
            this.pricePerNight = pricePerNight;
            this.nights = nights;
            this.subTotal = subTotal;
        }
        
        // Getters and Setters
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public double getPricePerNight() { return pricePerNight; }
        public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
        
        public int getNights() { return nights; }
        public void setNights(int nights) { this.nights = nights; }
        
        public double getSubTotal() { return subTotal; }
        public void setSubTotal(double subTotal) { this.subTotal = subTotal; }
        
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        
        // Get formatted price
        public String getFormattedPrice() {
            return String.format("$%.2f", subTotal);
        }
        
        // Get display text
        public String getDisplayText() {
            if (roomName != null && !roomName.isEmpty()) {
                return String.format("%s × %d (%d nights)", roomName, quantity, nights);
            }
            return String.format("Room × %d (%d nights)", quantity, nights);
        }
    }
    
    // Inner class for service bookings
    public static class ServiceBooking {
        private String serviceId;
        private Timestamp date;
        private double price;
        private int quantity; // Added missing quantity field
        private String serviceName; // Will be fetched from Firestore
        
        public ServiceBooking() {}
        
        public ServiceBooking(String serviceId, Timestamp date, double price, int quantity) {
            this.serviceId = serviceId;
            this.date = date;
            this.price = price;
            this.quantity = quantity;
        }
        
        // Getters and Setters
        public String getServiceId() { return serviceId; }
        public void setServiceId(String serviceId) { this.serviceId = serviceId; }
        
        public Timestamp getDate() { return date; }
        public void setDate(Timestamp date) { this.date = date; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        // Get formatted price
        public String getFormattedPrice() {
            return String.format("$%.2f", price * quantity);
        }
        
        // Get formatted date
        public String getFormattedDate() {
            if (date != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault());
                return sdf.format(date.toDate());
            }
            return "Date not specified";
        }
        
        // Get display text
        public String getDisplayText() {
            if (serviceName != null && !serviceName.isEmpty()) {
                return serviceName + " × " + quantity;
            }
            return "Service × " + quantity;
        }
    }
}
