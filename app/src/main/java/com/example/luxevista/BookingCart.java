package com.example.luxevista;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingCart {
    public static class RoomSelection {
        public String roomId;
        public String name;
        public double pricePerNight;
        public int quantity;
        public RoomSelection(String roomId, String name, double pricePerNight, int quantity) {
            this.roomId = roomId;
            this.name = name;
            this.pricePerNight = pricePerNight;
            this.quantity = quantity;
        }
    }

    public static class ServiceSelection {
        public String serviceId;
        public String name;
        public double price;
        public int quantity;
        public Timestamp scheduledAt; // precise time chosen
        public ServiceSelection(String serviceId, String name, double price, int quantity, Timestamp scheduledAt) {
            this.serviceId = serviceId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.scheduledAt = scheduledAt;
        }
    }

    private static BookingCart instance;

    public static BookingCart getInstance() {
        if (instance == null) instance = new BookingCart();
        return instance;
    }

    private BookingCart() {}

    public Timestamp checkIn;
    public Timestamp checkOut;
    public String currency = "USD";

    public final Map<String, RoomSelection> roomSelections = new HashMap<>();
    public final List<ServiceSelection> serviceSelections = new ArrayList<>();

    public void clear() {
        checkIn = null;
        checkOut = null;
        roomSelections.clear();
        serviceSelections.clear();
    }

    public int getNights() {
        if (checkIn == null || checkOut == null) return 0;
        long ms = checkOut.toDate().getTime() - checkIn.toDate().getTime();
        return (int) Math.max(0, Math.round(ms / (1000.0 * 60 * 60 * 24)));
    }

    public double getRoomsSubtotal() {
        int nights = getNights();
        double total = 0;
        for (RoomSelection r : roomSelections.values()) {
            total += r.pricePerNight * nights * r.quantity;
        }
        return total;
    }

    public double getServicesSubtotal() {
        double total = 0;
        for (ServiceSelection s : serviceSelections) {
            total += s.price * s.quantity;
        }
        return total;
    }

    public double getTotal() { return getRoomsSubtotal() + getServicesSubtotal(); }

    public double calculateTotalPrice() {
        double total = 0.0;
        
        // Add room costs
        for (RoomSelection roomSel : roomSelections.values()) {
            total += roomSel.quantity * roomSel.pricePerNight * getNights();
        }
        
        // Add service costs
        for (ServiceSelection selection : serviceSelections) {
            total += selection.quantity * selection.price;
        }
        
        return total;
    }

    public int getRoomQuantity(String roomId) {
        RoomSelection selection = roomSelections.get(roomId);
        return selection != null ? selection.quantity : 0;
    }

    public int getServiceQuantity(String serviceId) {
        int total = 0;
        for (ServiceSelection selection : serviceSelections) {
            if (selection.serviceId.equals(serviceId)) {
                total += selection.quantity;
            }
        }
        return total;
    }
}


