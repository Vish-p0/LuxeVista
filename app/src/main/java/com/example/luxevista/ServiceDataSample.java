package com.example.luxevista;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample service data structure demonstrating the new JSON format
 * This class shows how services should be structured in Firestore
 */
public class ServiceDataSample {
    
    /**
     * Example of how to create a service with the new availability structure
     * This represents the JSON structure specified in the requirements
     */
    public static Map<String, Object> createSampleService() {
        Map<String, Object> service = new HashMap<>();
        
        // Basic service information
        service.put("serviceId", "service001");
        service.put("name", "Luxury Spa 60min");
        service.put("category", "Spa");
        service.put("price", 85.0);
        service.put("currency", "USD");
        service.put("description", "Relaxing full-body massage using essential oils and aromatherapy.");
        service.put("durationMinutes", 60);
        service.put("imageUrls", new String[]{
            "https://i.imgur.com/x5BflkP.jpeg",
            "https://i.imgur.com/Ho4xMd3.jpeg",
            "https://i.imgur.com/nAaNFnV.jpeg"
        });
        
        // Available hours
        Map<String, String> availableHours = new HashMap<>();
        availableHours.put("start", "09:00");
        availableHours.put("end", "21:00");
        service.put("availableHours", availableHours);
        
        // Time slots with quantities
        Map<String, Integer> timeSlots = new HashMap<>();
        timeSlots.put("09:00", 2);
        timeSlots.put("10:00", 2);
        timeSlots.put("11:00", 2);
        timeSlots.put("12:00", 2);
        timeSlots.put("13:00", 2);
        timeSlots.put("14:00", 2);
        timeSlots.put("15:00", 2);
        timeSlots.put("16:00", 2);
        timeSlots.put("17:00", 2);
        timeSlots.put("18:00", 2);
        timeSlots.put("19:00", 2);
        timeSlots.put("20:00", 2);
        service.put("timeSlots", timeSlots);
        
        // Availability map (date -> time -> booked count)
        Map<String, Map<String, Integer>> availability = new HashMap<>();
        
        // Example: 2025-01-15 availability
        Map<String, Integer> dateAvailability = new HashMap<>();
        dateAvailability.put("09:00", 1); // 1 slot booked
        dateAvailability.put("10:00", 0); // 0 slots booked (fully available)
        dateAvailability.put("11:00", 2); // 2 slots booked (fully booked)
        dateAvailability.put("12:00", 0); // 0 slots booked (fully available)
        availability.put("2025-01-15", dateAvailability);
        
        // Example: 2025-01-16 availability
        Map<String, Integer> dateAvailability2 = new HashMap<>();
        dateAvailability2.put("09:00", 0); // 0 slots booked (fully available)
        dateAvailability2.put("10:00", 0); // 0 slots booked (fully available)
        dateAvailability2.put("11:00", 0); // 0 slots booked (fully available)
        dateAvailability2.put("12:00", 0); // 0 slots booked (fully available)
        availability.put("2025-01-16", dateAvailability2);
        
        service.put("availability", availability);
        
        return service;
    }
    
    /**
     * Example of how to create a booking with the new structure
     */
    public static Map<String, Object> createSampleBooking() {
        Map<String, Object> booking = new HashMap<>();
        
        booking.put("bookingId", "booking011");
        booking.put("userId", "user11");
        booking.put("startDate", "2025-01-15T10:00:00Z");
        booking.put("endDate", "2025-01-15T11:00:00Z");
        booking.put("type", "service");
        booking.put("status", "confirmed");
        booking.put("currency", "USD");
        booking.put("totalPrice", 85.0);
        booking.put("createdAt", "2025-01-10T08:00:00Z");
        
        // Services array
        Map<String, Object> service = new HashMap<>();
        service.put("serviceId", "service001");
        service.put("date", "2025-01-15T10:00:00Z");
        service.put("price", 85.0);
        
        booking.put("services", new Map[]{service});
        
        return booking;
    }
    
    /**
     * Helper method to update service availability after a booking
     */
    public static void updateServiceAvailability(Map<String, Object> service, String date, String time) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Integer>> availability = (Map<String, Map<String, Integer>>) service.get("availability");
        
        if (availability == null) {
            availability = new HashMap<>();
            service.put("availability", availability);
        }
        
        Map<String, Integer> dateAvailability = availability.get(date);
        if (dateAvailability == null) {
            dateAvailability = new HashMap<>();
            availability.put(date, dateAvailability);
        }
        
        // Get current booked count and increment it
        int currentBooked = dateAvailability.getOrDefault(time, 0);
        dateAvailability.put(time, currentBooked + 1);
    }
    
    /**
     * Helper method to check if a time slot is available
     */
    public static boolean isTimeSlotAvailable(Map<String, Object> service, String date, String time) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> timeSlots = (Map<String, Integer>) service.get("timeSlots");
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Integer>> availability = (Map<String, Map<String, Integer>>) service.get("availability");
        
        if (timeSlots == null || !timeSlots.containsKey(time)) {
            return false; // Time slot doesn't exist
        }
        
        int totalSlots = timeSlots.get(time);
        int bookedSlots = 0;
        
        if (availability != null && availability.containsKey(date)) {
            Map<String, Integer> dateAvailability = availability.get(date);
            if (dateAvailability != null && dateAvailability.containsKey(time)) {
                bookedSlots = dateAvailability.get(time);
            }
        }
        
        return bookedSlots < totalSlots;
    }
}

