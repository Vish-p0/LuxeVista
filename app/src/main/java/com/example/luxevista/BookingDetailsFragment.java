package com.example.luxevista;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.ImageUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingDetailsFragment extends Fragment {

    private static final String ARG_BOOKING_ID = "booking_id";
    
    private String bookingId;
    private NewBooking booking;
    private FirebaseFirestore db;
    
    // Views
    private TextView tvBookingId, tvStatus, tvTotalPrice, tvDates, tvCurrency;
    private LinearLayout roomsContainer, servicesContainer;
    private Button btnCancelBooking;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingId = getArguments().getString(ARG_BOOKING_ID);
        }
        db = FirebaseFirestore.getInstance();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_details, container, false);
        
        initViews(view);
        setupBackButton(view);
        loadBookingDetails();
        
        return view;
    }
    
    private void initViews(View view) {
        tvBookingId = view.findViewById(R.id.tvBookingId);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvDates = view.findViewById(R.id.tvDates);
        tvCurrency = view.findViewById(R.id.tvCurrency);
        roomsContainer = view.findViewById(R.id.roomsContainer);
        servicesContainer = view.findViewById(R.id.servicesContainer);
        btnCancelBooking = view.findViewById(R.id.btnCancelBooking);
        
        // Setup cancel booking button
        btnCancelBooking.setOnClickListener(v -> cancelBooking());
    }
    
    private void setupBackButton(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            androidx.navigation.NavController nav = androidx.navigation.Navigation.findNavController(requireView());
            nav.navigateUp();
        });
    }
    
    private void loadBookingDetails() {
        if (bookingId == null) {
            showError("Booking ID not found");
            return;
        }
        
        db.collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        booking = documentSnapshot.toObject(NewBooking.class);
                        if (booking != null) {
                            booking.setBookingId(documentSnapshot.getId());
                            displayBookingDetails();
                            fetchItemNames();
                        } else {
                            showError("Failed to parse booking data");
                        }
                    } else {
                        showError("Booking not found");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Failed to load booking: " + e.getMessage());
                });
    }
    
    private void displayBookingDetails() {
        if (booking == null) return;
        
        // Set booking ID
        tvBookingId.setText("Booking #" + booking.getBookingId());
        
        // Set status
        tvStatus.setText(booking.getDisplayStatus());
        String statusColor = booking.getStatusColor();
        try {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(statusColor)));
        } catch (Exception e) {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#9E9E9E")));
        }
        tvStatus.setTextColor(android.graphics.Color.WHITE);
        
        // Set total price
        tvTotalPrice.setText(booking.getFormattedPrice());
        
        // Set currency
        tvCurrency.setText(booking.getCurrency());
        
        // Set dates
        Date startDate = booking.getStartDateAsDate();
        Date endDate = booking.getEndDateAsDate();
        
        if (startDate != null && endDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            
            String startStr = dateFormat.format(startDate) + " at " + timeFormat.format(startDate);
            String endStr = dateFormat.format(endDate) + " at " + timeFormat.format(endDate);
            
            tvDates.setText("Check-in: " + startStr + "\nCheck-out: " + endStr);
        } else {
            tvDates.setText("Dates not specified");
        }
        
        // Show/hide cancel button based on status
        if ("confirmed".equals(booking.getStatus()) && booking.isActive()) {
            btnCancelBooking.setVisibility(View.VISIBLE);
        } else {
            btnCancelBooking.setVisibility(View.GONE);
        }
    }
    
    private void fetchItemNames() {
        if (booking == null) return;
        
        // Fetch room names
        if (booking.getRooms() != null) {
            for (NewBooking.RoomBooking room : booking.getRooms()) {
                db.collection("rooms")
                        .document(room.getRoomId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String roomName = documentSnapshot.getString("name");
                                if (roomName != null && !roomName.isEmpty()) {
                                    room.setRoomName(roomName);
                                }
                            }
                            displayRooms();
                        })
                        .addOnFailureListener(e -> {
                            displayRooms();
                        });
            }
        } else {
            displayRooms();
        }
        
        // Fetch service names
        if (booking.getServices() != null) {
            for (NewBooking.ServiceBooking service : booking.getServices()) {
                db.collection("services")
                        .document(service.getServiceId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String serviceName = documentSnapshot.getString("name");
                                if (serviceName != null && !serviceName.isEmpty()) {
                                    service.setServiceName(serviceName);
                                }
                            }
                            displayServices();
                        })
                        .addOnFailureListener(e -> {
                            displayServices();
                        });
            }
        } else {
            displayServices();
        }
    }
    
    private void displayRooms() {
        if (booking == null || booking.getRooms() == null) {
            roomsContainer.setVisibility(View.GONE);
            return;
        }
        
        roomsContainer.removeAllViews();
        roomsContainer.setVisibility(View.VISIBLE);
        
        for (NewBooking.RoomBooking room : booking.getRooms()) {
            View roomView = createRoomView(room);
            roomsContainer.addView(roomView);
        }
    }
    
    private void displayServices() {
        if (booking == null || booking.getServices() == null) {
            servicesContainer.setVisibility(View.GONE);
            return;
        }
        
        servicesContainer.removeAllViews();
        servicesContainer.setVisibility(View.VISIBLE);
        
        for (NewBooking.ServiceBooking service : booking.getServices()) {
            View serviceView = createServiceView(service);
            servicesContainer.addView(serviceView);
        }
    }
    
    private View createRoomView(NewBooking.RoomBooking room) {
        View roomView = LayoutInflater.from(getContext()).inflate(R.layout.item_booking_room_detail, null);
        
        ImageView ivRoomImage = roomView.findViewById(R.id.ivRoomImage);
        TextView tvRoomName = roomView.findViewById(R.id.tvRoomName);
        TextView tvRoomDetails = roomView.findViewById(R.id.tvRoomDetails);
        TextView tvRoomPrice = roomView.findViewById(R.id.tvRoomPrice);
        
        // Set room image (placeholder for now)
        ivRoomImage.setImageResource(R.drawable.ic_rooms);
        
        // Set room name
        String roomName = room.getRoomName();
        if (roomName == null || roomName.isEmpty()) {
            roomName = "Room";
        }
        tvRoomName.setText(roomName);
        
        // Set room details
        String details = String.format(Locale.US, "Quantity: %d × %d nights", room.getQuantity(), room.getNights());
        tvRoomDetails.setText(details);
        
        // Set room price
        tvRoomPrice.setText(room.getFormattedPrice());
        
        return roomView;
    }
    
    private View createServiceView(NewBooking.ServiceBooking service) {
        View serviceView = LayoutInflater.from(getContext()).inflate(R.layout.item_booking_service_detail, null);
        
        ImageView ivServiceImage = serviceView.findViewById(R.id.ivServiceImage);
        TextView tvServiceName = serviceView.findViewById(R.id.tvServiceName);
        TextView tvServiceDetails = serviceView.findViewById(R.id.tvServiceDetails);
        TextView tvServicePrice = serviceView.findViewById(R.id.tvServicePrice);
        
        // Set service image (placeholder for now)
        ivServiceImage.setImageResource(R.drawable.ic_services);
        
        // Set service name
        String serviceName = service.getServiceName();
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "Service";
        }
        tvServiceName.setText(serviceName);
        
        // Set service details
        String details = String.format(Locale.US, "Quantity: %d", service.getQuantity());
        if (service.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            details += " • " + sdf.format(service.getDate().toDate());
        }
        tvServiceDetails.setText(details);
        
        // Set service price
        tvServicePrice.setText(service.getFormattedPrice());
        
        return serviceView;
    }
    
    private void cancelBooking() {
        if (booking == null) return;
        
        // Show confirmation dialog
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
                .setPositiveButton("Cancel Booking", (dialog, which) -> {
                    performCancellation();
                })
                .setNegativeButton("Keep Booking", null)
                .show();
    }
    
    private void performCancellation() {
        if (booking == null) return;
        
        // Update booking status to cancelled
    db.collection("bookings")
                .document(bookingId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
            // Notify and navigate back to Bookings list
            Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
            androidx.navigation.NavController nav = androidx.navigation.Navigation.findNavController(requireView());
            nav.popBackStack();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(requireView(), "Failed to cancel booking: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }
    
    private void showError(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
    }
    
    public static BookingDetailsFragment newInstance(String bookingId) {
        BookingDetailsFragment fragment = new BookingDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOKING_ID, bookingId);
        fragment.setArguments(args);
        return fragment;
    }
}
