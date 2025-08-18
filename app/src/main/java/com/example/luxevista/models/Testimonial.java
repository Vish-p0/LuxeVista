package com.example.luxevista.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Testimonial {
    private String testimonialId;
    private String userId;
    private String comment;
    private int rating;
    private String createdAt; // Changed to String to match database
    private String userName; // This will be fetched from users collection

    // Default constructor required for Firestore
    public Testimonial() {}

    // Constructor
    public Testimonial(String testimonialId, String userId, String comment, int rating, String createdAt) {
        this.testimonialId = testimonialId;
        this.userId = userId;
        this.comment = comment;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    // Getters
    public String getTestimonialId() {
        return testimonialId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setTestimonialId(String testimonialId) {
        this.testimonialId = testimonialId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Helper method to get formatted date
    @Exclude
    public String getFormattedDate() {
        if (createdAt != null) {
            try {
                // Parse the ISO date string from database
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(createdAt);
                return outputFormat.format(date);
            } catch (Exception e) {
                // Fallback: return the original string or a default
                return createdAt.substring(0, Math.min(10, createdAt.length())); // Just the date part
            }
        }
        return "";
    }
}
