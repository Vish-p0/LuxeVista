package com.example.luxevista;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.ImageUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingConfirmationFragment extends Fragment {

    private RecyclerView recyclerBreakdown;
    private TextView tvDates, tvTotal;
    private Button btnConfirm;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_confirmation, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerBreakdown = view.findViewById(R.id.recyclerBreakdown);
        tvDates = view.findViewById(R.id.tvDates);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        // Setup back button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            androidx.navigation.NavController nav = androidx.navigation.Navigation.findNavController(requireView());
            nav.navigateUp();
        });

        recyclerBreakdown.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBreakdown.setAdapter(new ModernBreakdownAdapter());

        bindCart();

        btnConfirm.setOnClickListener(v -> finalizeBooking());

        return view;
    }

    private void bindCart() {
        BookingCart cart = BookingCart.getInstance();
        String dates = DateFormat.format("MMM d, yyyy 2:00 PM", cart.checkIn != null ? cart.checkIn.toDate() : new java.util.Date())
                + "  -  " + DateFormat.format("MMM d, yyyy 11:00 AM", cart.checkOut != null ? cart.checkOut.toDate() : new java.util.Date());
        tvDates.setText(dates);
        tvTotal.setText(String.format(Locale.US, "$%.2f", cart.getTotal()));
        ((ModernBreakdownAdapter) recyclerBreakdown.getAdapter()).setData(cart);
    }

    private List<String> getNightsKeys(Timestamp checkIn, Timestamp checkOut) {
        java.text.SimpleDateFormat api = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
        List<String> days = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.setTime(checkIn.toDate());
        while (c.getTime().before(checkOut.toDate())) {
            days.add(api.format(c.getTime()));
            c.add(Calendar.DATE, 1);
        }
        return days;
    }

    private void finalizeBooking() {
        BookingCart cart = BookingCart.getInstance();
        if (cart.checkIn == null || cart.checkOut == null) {
            Snackbar.make(requireView(), "Please select dates first", Snackbar.LENGTH_LONG).show();
            return;
        }
        // Require at least one room (services alone are not allowed)
        if (cart.roomSelections.isEmpty()) {
            Snackbar.make(requireView(), "Please select at least one room", Snackbar.LENGTH_LONG).show();
            return;
        }
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("startDate", cart.checkIn);
        booking.put("endDate", cart.checkOut);
        booking.put("status", "confirmed");
        booking.put("currency", cart.currency);
        booking.put("totalPrice", cart.getTotal());
        booking.put("createdAt", Timestamp.now());

        List<Map<String, Object>> rooms = new ArrayList<>();
        int nights = cart.getNights();
        for (BookingCart.RoomSelection r : cart.roomSelections.values()) {
            Map<String, Object> row = new HashMap<>();
            row.put("roomId", r.roomId);
            row.put("quantity", r.quantity);
            row.put("pricePerNight", r.pricePerNight);
            row.put("nights", nights);
            row.put("subTotal", r.pricePerNight * nights * r.quantity);
            rooms.add(row);
        }
        booking.put("rooms", rooms);

        List<Map<String, Object>> services = new ArrayList<>();
        for (BookingCart.ServiceSelection s : cart.serviceSelections) {
            Map<String, Object> row = new HashMap<>();
            row.put("serviceId", s.serviceId);
            row.put("date", s.scheduledAt);
            row.put("price", s.price);
            row.put("quantity", s.quantity);
            services.add(row);
        }
        booking.put("services", services);

        List<String> nightsKeys = getNightsKeys(cart.checkIn, cart.checkOut);

        // First, read all required documents
        List<DocumentReference> roomRefs = new ArrayList<>();
        List<DocumentReference> serviceRefs = new ArrayList<>();
        
        for (BookingCart.RoomSelection r : cart.roomSelections.values()) {
            roomRefs.add(db.collection("rooms").document(r.roomId));
        }
        
        for (BookingCart.ServiceSelection s : cart.serviceSelections) {
            serviceRefs.add(db.collection("services").document(s.serviceId));
        }

        // Read all documents first
        db.runTransaction(trx -> {
            // READ PHASE: Read all room documents
            Map<String, DocumentSnapshot> roomSnapshots = new HashMap<>();
            for (DocumentReference roomRef : roomRefs) {
                roomSnapshots.put(roomRef.getId(), trx.get(roomRef));
            }
            
            // READ PHASE: Read all service documents
            Map<String, DocumentSnapshot> serviceSnapshots = new HashMap<>();
            for (DocumentReference serviceRef : serviceRefs) {
                serviceSnapshots.put(serviceRef.getId(), trx.get(serviceRef));
            }
            
            // WRITE PHASE: Update all room availability
            for (BookingCart.RoomSelection r : cart.roomSelections.values()) {
                DocumentReference roomRef = db.collection("rooms").document(r.roomId);
                DocumentSnapshot snap = roomSnapshots.get(r.roomId);
                Map<String, Object> data = snap.getData();
                if (data == null) data = new HashMap<>();
                Map<String, Object> avail = (Map<String, Object>) data.get("availability");
                if (avail == null) avail = new HashMap<>();
                for (String d : nightsKeys) {
                    long booked = 0L;
                    Object raw = avail.get(d);
                    if (raw instanceof Number) booked = ((Number) raw).longValue();
                    avail.put(d, booked + r.quantity);
                }
                data.put("availability", avail);
                trx.set(roomRef, data, SetOptions.merge());
            }

            // WRITE PHASE: Update all service availability (nested date -> time -> booked count)
            for (BookingCart.ServiceSelection s : cart.serviceSelections) {
                DocumentReference sRef = db.collection("services").document(s.serviceId);
                DocumentSnapshot snap = serviceSnapshots.get(s.serviceId);
                Map<String, Object> data = snap.getData();
                if (data == null) data = new HashMap<>();
                Map<String, Object> avail = (Map<String, Object>) data.get("availability");
                if (avail == null) avail = new HashMap<>();

                java.text.SimpleDateFormat apiDay = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
                java.text.SimpleDateFormat apiTime = new java.text.SimpleDateFormat("HH:mm", Locale.US);
                String dayKey = apiDay.format(s.scheduledAt.toDate());
                String timeKey = apiTime.format(s.scheduledAt.toDate());

                // Get nested map for the day
                Object dayRaw = avail.get(dayKey);
                Map<String, Object> timesMap;
                if (dayRaw instanceof Map) {
                    timesMap = (Map<String, Object>) dayRaw;
                } else {
                    timesMap = new HashMap<>();
                }

                long booked = 0L;
                Object raw = timesMap.get(timeKey);
                if (raw instanceof Number) booked = ((Number) raw).longValue();
                timesMap.put(timeKey, booked + s.quantity);

                avail.put(dayKey, timesMap);
                data.put("availability", avail);
                trx.set(sRef, data, SetOptions.merge());
            }

            // WRITE PHASE: Create the booking document
            DocumentReference bookingRef = db.collection("bookings").document();
            trx.set(bookingRef, booking);
            
            return null;
        }).addOnSuccessListener(unused -> {
            Snackbar.make(requireView(), "Booking confirmed", Snackbar.LENGTH_LONG).show();
            BookingCart.getInstance().clear();
            
            // Automatically navigate to bookings fragment
            androidx.navigation.NavController nav = androidx.navigation.Navigation.findNavController(requireView());
            nav.navigate(R.id.action_bookingConfirmation_to_bookingsFragment);
        }).addOnFailureListener(e -> {
            Snackbar.make(requireView(), "Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        });
    }

    /** Modern breakdown adapter with custom layout showing rooms and services with images, dates, and prices */
    static class ModernBreakdownAdapter extends RecyclerView.Adapter<ModernBreakdownAdapter.ViewHolder> {
        private final List<BreakdownItem> items = new ArrayList<>();

        static class BreakdownItem {
            String name;
            String details;
            String dateTime;
            double price;
            String imageUrl;
            String type; // "room" or "service"

            BreakdownItem(String name, String details, String dateTime, double price, String imageUrl, String type) {
                this.name = name;
                this.details = details;
                this.dateTime = dateTime;
                this.price = price;
                this.imageUrl = imageUrl;
                this.type = type;
            }
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivItemImage;
            TextView tvItemName;
            TextView tvItemDetails;
            TextView tvItemDate;
            TextView tvItemPrice;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivItemImage = itemView.findViewById(R.id.ivItemImage);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvItemDetails = itemView.findViewById(R.id.tvItemDetails);
                tvItemDate = itemView.findViewById(R.id.tvItemDate);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_breakdown, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BreakdownItem item = items.get(position);
            holder.tvItemName.setText(item.name);
            holder.tvItemDetails.setText(item.details);
            holder.tvItemDate.setText(item.dateTime);
            holder.tvItemPrice.setText(String.format(Locale.US, "$%.2f", item.price));

            // Load image if available, otherwise use type-specific placeholder with tint
            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                ImageUtils.loadImageWithFallback(holder.ivItemImage, item.imageUrl);
            } else {
                // Use the specialized method that handles room/service placeholders
                ImageUtils.loadItemImageWithFallback(holder.ivItemImage, null, item.type);
                // Apply tint to the placeholder icons
                holder.ivItemImage.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.dark_blue_primary, null));
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        void setData(BookingCart cart) {
            items.clear();
            int nights = cart.getNights();
            
            // Rooms
            for (BookingCart.RoomSelection r : cart.roomSelections.values()) {
                double sub = r.pricePerNight * nights * r.quantity;
                String details = String.format(Locale.US, "Quantity: %d × %d nights", r.quantity, nights);
                String dateTime = String.format(Locale.US, "%d nights @ $%.2f/night", nights, r.pricePerNight);
                
                // Get room image from Firestore if available
                String imageUrl = null; // Will be populated if we have room data
                
                items.add(new BreakdownItem(r.name, details, dateTime, sub, imageUrl, "room"));
            }
            
            // Services
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US);
            for (BookingCart.ServiceSelection s : cart.serviceSelections) {
                double sub = s.price * s.quantity;
                String details = String.format(Locale.US, "Quantity: %d", s.quantity);
                String when = s.scheduledAt != null ? fmt.format(s.scheduledAt.toDate()) : "";
                
                // Get service image from Firestore if available
                String imageUrl = null; // Will be populated if we have service data
                
                items.add(new BreakdownItem(s.name, details, when, sub, imageUrl, "service"));
            }
            notifyDataSetChanged();
        }
    }
}


