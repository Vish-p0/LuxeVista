package com.example.luxevista;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingsFragment extends Fragment implements NewBookingAdapter.OnBookingClickListener {

    private static final String TAG = "BookingsFragment";
    
    // Views
    private EditText searchEditText;
    private MaterialButton btnFilterAll, btnFilterActive, btnFilterCompleted, btnFilterCancelled;
    private RecyclerView recyclerViewBookings;
    private LinearLayout loadingLayout, emptyStateLayout, errorStateLayout;
    private TextView errorMessageText;
    private MaterialButton btnRetry;
    
    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String firebaseAuthUid;
    
    // Data
    private NewBookingAdapter adapter;
    private List<NewBooking> allBookings = new ArrayList<>();
    private String currentFilter = "All";
    private String currentSearchQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new Fade());
        setExitTransition(new Fade());
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            firebaseAuthUid = currentUser.getUid();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);
        initViews(view);
        setupRecyclerView();
        setupSearchAndFilters();
        loadBookings();
        return view;
    }
    
    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterActive = view.findViewById(R.id.btnFilterActive);
        btnFilterCompleted = view.findViewById(R.id.btnFilterCompleted);
        btnFilterCancelled = view.findViewById(R.id.btnFilterCancelled);
        recyclerViewBookings = view.findViewById(R.id.recyclerViewBookings);
        loadingLayout = view.findViewById(R.id.loadingLayout);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        errorStateLayout = view.findViewById(R.id.errorStateLayout);
        errorMessageText = view.findViewById(R.id.errorMessageText);
        btnRetry = view.findViewById(R.id.btnRetry);
    }
    
    private void setupRecyclerView() {
        adapter = new NewBookingAdapter(this);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBookings.setAdapter(adapter);
    }
    
    private void setupSearchAndFilters() {
        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Filter button listeners
        btnFilterAll.setOnClickListener(v -> setFilter("All"));
        btnFilterActive.setOnClickListener(v -> setFilter("Active"));
        btnFilterCompleted.setOnClickListener(v -> setFilter("Completed"));
        btnFilterCancelled.setOnClickListener(v -> setFilter("Cancelled"));
        
        // Retry button
        btnRetry.setOnClickListener(v -> loadBookings());
    }
    
    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterButtons();
        applyFilters();
    }
    
    private void updateFilterButtons() {
        // Reset all buttons
        resetFilterButton(btnFilterAll);
        resetFilterButton(btnFilterActive);
        resetFilterButton(btnFilterCompleted);
        resetFilterButton(btnFilterCancelled);
        
        // Highlight selected button
        MaterialButton selectedButton = null;
        switch (currentFilter) {
            case "All":
                selectedButton = btnFilterAll;
                break;
            case "Active":
                selectedButton = btnFilterActive;
                break;
            case "Completed":
                selectedButton = btnFilterCompleted;
                break;
            case "Cancelled":
                selectedButton = btnFilterCancelled;
                break;
        }
        
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF")));
            selectedButton.setTextColor(Color.WHITE);
        }
    }
    
    private void resetFilterButton(MaterialButton button) {
        button.setBackgroundTintList(null);
        button.setTextColor(Color.parseColor("#007AFF"));
    }
    
    private void applyFilters() {
        if (adapter != null) {
            adapter.filter(currentSearchQuery, currentFilter);
            updateEmptyState();
        }
    }
    
    private void loadBookings() {
        if (firebaseAuthUid == null) {
            showError("User not authenticated");
            return;
        }
        
        showLoading(true);
        
        // First, find the custom user ID from the users collection using the current user's email
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            showError("User email not available");
            return;
        }
        
        // Look up user by email to get custom user ID
        String userEmail = currentUser.getEmail();
        Log.d(TAG, "Looking up user with email: " + userEmail);
        
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "User lookup query returned " + querySnapshot.size() + " documents");
                    
                    if (!querySnapshot.isEmpty()) {
                        // Found user document, let's examine its contents
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        Log.d(TAG, "User document ID: " + userDoc.getId());
                        Log.d(TAG, "User document data: " + userDoc.getData());
                        
                        // Try different field names for the custom user ID
                        String customUserId = userDoc.getString("uid");
                        if (customUserId == null) {
                            customUserId = userDoc.getString("userId");
                        }
                        if (customUserId == null) {
                            customUserId = userDoc.getId(); // Use document ID as fallback
                        }
                        
                        if (customUserId != null) {
                            currentUserId = customUserId;
                            Log.d(TAG, "Found custom user ID: " + customUserId + " for email: " + userEmail);
                            // Now load bookings with the custom user ID
                            loadBookingsWithUserId();
                        } else {
                            Log.e(TAG, "No uid, userId field found in user document, and document ID is null");
                            showError("User ID not found in profile. Please contact support.");
                        }
                    } else {
                        Log.e(TAG, "No user document found for email: " + userEmail);
                        // Try alternative approach - look for user document with ID "user01", "user02", etc.
                        tryDirectUserLookup(userEmail);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error looking up user", e);
                    showError("Failed to load user profile");
                });
    }
    
    private void tryDirectUserLookup(String userEmail) {
        Log.d(TAG, "Trying direct user lookup approach for email: " + userEmail);
        
        // Extract potential user ID from email (user01@example.com -> user01)
        String potentialUserId = userEmail.split("@")[0];
        Log.d(TAG, "Trying direct document lookup with ID: " + potentialUserId);
        
        // Try to get document directly by ID
        db.collection("users")
                .document(potentialUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Found user document with direct ID lookup: " + potentialUserId);
                        Log.d(TAG, "Document data: " + documentSnapshot.getData());
                        
                        // Check if email matches
                        String docEmail = documentSnapshot.getString("email");
                        if (userEmail.equals(docEmail)) {
                            currentUserId = potentialUserId;
                            Log.d(TAG, "Email matches! Using user ID: " + potentialUserId);
                            loadBookingsWithUserId();
                        } else {
                            Log.e(TAG, "Email mismatch. Document email: " + docEmail + ", Expected: " + userEmail);
                            showError("User profile email mismatch. Please contact support.");
                        }
                    } else {
                        Log.e(TAG, "No user document found with ID: " + potentialUserId);
                        showError("User profile not found in database. Please contact support.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error in direct user lookup", e);
                    showError("Failed to load user profile");
                });
    }
    
    private void loadBookingsWithUserId() {
        Log.d(TAG, "Loading bookings for custom user ID: " + currentUserId);
        
        db.collection("bookings")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allBookings.clear();
                    
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " bookings for user: " + currentUserId);
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No bookings found for user: " + currentUserId);
                        showLoading(false);
                        updateEmptyState();
                        return;
                    }
                    
                    // Counter to track completion of item name fetching
                    AtomicInteger pendingRequests = new AtomicInteger(queryDocumentSnapshots.size());
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            NewBooking booking = document.toObject(NewBooking.class);
                            if (booking != null) {
                                // Set the booking ID from the document ID
                                booking.setBookingId(document.getId());
                                
                                // Fetch item names for rooms and services
                                fetchItemNames(booking, () -> {
                                    allBookings.add(booking);
                                    
                                    // Check if all requests are completed
                                    if (pendingRequests.decrementAndGet() == 0) {
                                        showLoading(false);
                                        // Sort bookings by creation date (newest first)
                                        sortBookingsByDate();
                                        adapter.updateBookings(allBookings);
                                        applyFilters();
                                    }
                                });
                            } else {
                                if (pendingRequests.decrementAndGet() == 0) {
                                    showLoading(false);
                                    sortBookingsByDate();
                                    adapter.updateBookings(allBookings);
                                    applyFilters();
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing booking document", e);
                            if (pendingRequests.decrementAndGet() == 0) {
                                showLoading(false);
                                sortBookingsByDate();
                                adapter.updateBookings(allBookings);
                                applyFilters();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading bookings", e);
                    showLoading(false);
                    
                    String errorMessage;
                    if (e.getMessage() != null && e.getMessage().contains("FAILED_PRECONDITION")) {
                        errorMessage = "Database configuration needed. Please contact support or try again later.";
                    } else if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                        errorMessage = "Access denied. Please check your login status.";
                    } else if (e.getMessage() != null && e.getMessage().contains("UNAVAILABLE")) {
                        errorMessage = "Service temporarily unavailable. Please try again.";
                    } else {
                        errorMessage = "Failed to load bookings. Please check your connection.";
                    }
                    
                    showError(errorMessage);
                });
    }
    
    private void fetchItemNames(NewBooking booking, Runnable onComplete) {
        AtomicInteger pendingRequests = new AtomicInteger(0);
        
        // Count total pending requests
        if (booking.getRooms() != null) pendingRequests.addAndGet(booking.getRooms().size());
        if (booking.getServices() != null) pendingRequests.addAndGet(booking.getServices().size());
        
        if (pendingRequests.get() == 0) {
            onComplete.run();
            return;
        }
        
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
                            
                            if (pendingRequests.decrementAndGet() == 0) {
                                onComplete.run();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error fetching room name for " + room.getRoomId(), e);
                            if (pendingRequests.decrementAndGet() == 0) {
                                onComplete.run();
                            }
                        });
            }
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
                            
                            if (pendingRequests.decrementAndGet() == 0) {
                                onComplete.run();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error fetching service name for " + service.getServiceId(), e);
                            if (pendingRequests.decrementAndGet() == 0) {
                                onComplete.run();
                            }
                        });
            }
        }
    }
    
    private void sortBookingsByDate() {
        // Sort bookings by creation date (newest first)
        Collections.sort(allBookings, new Comparator<NewBooking>() {
            @Override
            public int compare(NewBooking booking1, NewBooking booking2) {
                Date date1 = booking1.getCreatedAtAsDate();
                Date date2 = booking2.getCreatedAtAsDate();

                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1;
                if (date2 == null) return -1;

                // Descending order (newest first)
                return date2.compareTo(date1);
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewBookings.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }
    
    private void showError(String message) {
        loadingLayout.setVisibility(View.GONE);
        recyclerViewBookings.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.VISIBLE);
        errorMessageText.setText(message);
    }
    
    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public void onBookingClick(NewBooking booking) {
        // Handle booking item click
        Toast.makeText(getContext(), "Clicked booking: " + booking.getBookingId(), Toast.LENGTH_SHORT).show();
        // You can navigate to booking details here
    }
    
    @Override
    public void onViewDetailsClick(NewBooking booking) {
        // Navigate to booking details fragment
        androidx.navigation.NavController nav = androidx.navigation.Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("booking_id", booking.getBookingId());
        nav.navigate(R.id.action_bookingsFragment_to_bookingDetailsFragment, args);
    }
}




