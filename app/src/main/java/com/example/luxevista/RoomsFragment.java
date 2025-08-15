package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.adapters.RoomAdapter;
import com.example.luxevista.models.Room;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoomsFragment extends Fragment implements RoomAdapter.OnRoomClickListener {

    private static final String TAG = "RoomsFragment";

    // Views
    private ImageView btnSearch, btnFilter, btnClearSearch;
    private LinearLayout searchContainer;
    private EditText etSearch;
    private ChipGroup chipGroupFilters;
    private RecyclerView recyclerRooms;
    private LinearLayout loadingLayout, contentLayout, emptyStateLayout;

    // Firebase
    private FirebaseFirestore db;

    // Data and Adapter
    private RoomAdapter roomAdapter;
    private List<Room> allRooms = new ArrayList<>();

    // Filter state
    private String currentSearchQuery = "";
    private double minPrice = 0;
    private double maxPrice = Double.MAX_VALUE;
    private List<String> selectedAmenities = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new Fade());
        setExitTransition(new Fade());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearchFunctionality();
        setupClickListeners();
        loadRooms();

        return view;
    }

    private void initViews(View view) {
        btnSearch = view.findViewById(R.id.btnSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        searchContainer = view.findViewById(R.id.searchContainer);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        recyclerRooms = view.findViewById(R.id.recyclerRooms);
        loadingLayout = view.findViewById(R.id.loadingLayout);
        contentLayout = view.findViewById(R.id.contentLayout);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(this);
        recyclerRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRooms.setAdapter(roomAdapter);
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
                
                // Show/hide clear button
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentSearchQuery = "";
            applyFilters();
        });
    }

    private void setupClickListeners() {
        btnSearch.setOnClickListener(v -> toggleSearchBar());
        
        btnFilter.setOnClickListener(v -> {
            // Show filter dialog
            showFilterDialog();
        });
    }

    private void toggleSearchBar() {
        if (searchContainer.getVisibility() == View.VISIBLE) {
            searchContainer.setVisibility(View.GONE);
            etSearch.setText("");
        } else {
            searchContainer.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        }
    }

    private void showFilterDialog() {
        // Create and show filter dialog
        RoomFilterDialog dialog = new RoomFilterDialog(
            getContext(),
            minPrice,
            maxPrice,
            selectedAmenities,
            (newMinPrice, newMaxPrice, newSelectedAmenities) -> {
                minPrice = newMinPrice;
                maxPrice = newMaxPrice;
                selectedAmenities = newSelectedAmenities;
                applyFilters();
                updateFilterChips();
            }
        );
        dialog.show();
    }

    private void applyFilters() {
        if (roomAdapter != null) {
            roomAdapter.filter(currentSearchQuery, minPrice, maxPrice, selectedAmenities);
            updateEmptyState();
        }
    }

    private void updateFilterChips() {
        chipGroupFilters.removeAllViews();
        
        boolean hasFilters = false;

        // Add price range chip if set
        if (minPrice > 0 || maxPrice < Double.MAX_VALUE) {
            String priceText = "$" + (int)minPrice + " - $" + (int)maxPrice;
            addFilterChip("Price: " + priceText, () -> {
                minPrice = 0;
                maxPrice = Double.MAX_VALUE;
                applyFilters();
                updateFilterChips();
            });
            hasFilters = true;
        }

        // Add amenity chips
        for (String amenity : selectedAmenities) {
            addFilterChip(getAmenityDisplayName(amenity), () -> {
                selectedAmenities.remove(amenity);
                applyFilters();
                updateFilterChips();
            });
            hasFilters = true;
        }

        chipGroupFilters.setVisibility(hasFilters ? View.VISIBLE : View.GONE);
    }

    private void addFilterChip(String text, Runnable onCloseClick) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.chip_background);
        chip.setOnCloseIconClickListener(v -> onCloseClick.run());
        chipGroupFilters.addView(chip);
    }

    private String getAmenityDisplayName(String amenityKey) {
        switch (amenityKey) {
            case "wifi": return "WiFi";
            case "airConditioning": return "AC";
            case "television": return "TV";
            case "roomService": return "Room Service";
            case "nonSmoking": return "Non-Smoking";
            case "wheelchairAccessible": return "Accessible";
            case "balcony": return "Balcony";
            case "oceanView": return "Ocean View";
            case "kingBed": return "King Bed";
            case "coffeeMaker": return "Coffee Maker";
            case "miniBar": return "Mini Bar";
            case "safe": return "Safe";
            case "jacuzzi": return "Jacuzzi";
            default: return amenityKey;
        }
    }

    private void loadRooms() {
        showLoading(true);
        Log.d(TAG, "Starting to load rooms from Firestore...");

        db.collection("rooms")
                .whereEqualTo("visible", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firestore query successful, documents found: " + queryDocumentSnapshots.size());
                    allRooms.clear();
                    
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "Processing document: " + document.getId());
                            Room room = document.toObject(Room.class);
                            if (room != null) {
                                Log.d(TAG, "Room parsed: " + room.getName() + ", visible: " + room.isVisible());
                                if (room.isVisible()) {
                                    allRooms.add(room);
                                    Log.d(TAG, "Added room: " + room.getName());
                                }
                            } else {
                                Log.w(TAG, "Room object is null for document: " + document.getId());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing room document: " + document.getId(), e);
                            Log.e(TAG, "Document data: " + document.getData());
                        }
                    }
                    
                    Log.d(TAG, "Total rooms added to list: " + allRooms.size());
                    
                    // Sort by price client-side
                    sortRoomsByPrice();
                    
                    roomAdapter.updateRooms(allRooms);
                    applyFilters();
                    showLoading(false);
                    Log.d(TAG, "Loaded " + allRooms.size() + " rooms");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading rooms", e);
                    showLoading(false);
                    showEmptyState(true);
                });
    }

    private void sortRoomsByPrice() {
        Collections.sort(allRooms, new Comparator<Room>() {
            @Override
            public int compare(Room room1, Room room2) {
                return Double.compare(room1.getPricePerNight(), room2.getPricePerNight());
            }
        });
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        boolean isEmpty = roomAdapter.getItemCount() == 0;
        if (isEmpty && !allRooms.isEmpty()) {
            // Show empty state for filtered results
            showEmptyState(true);
        } else if (isEmpty && allRooms.isEmpty()) {
            // Show empty state for no data
            showEmptyState(true);
        } else {
            // Show content
            showEmptyState(false);
        }
    }

    @Override
    public void onRoomClick(Room room) {
        // Navigate to room details
        Intent intent = new Intent(getContext(), RoomDetailsActivity.class);
        intent.putExtra("roomId", room.getRoomId());
        intent.putExtra("roomName", room.getName());
        intent.putExtra("roomType", room.getType());
        intent.putExtra("pricePerNight", room.getPricePerNight());
        intent.putExtra("currency", room.getCurrency());
        intent.putExtra("description", room.getDescription());
        intent.putExtra("maxGuests", room.getMaxGuests());
        
        // Convert image URLs to array
        if (room.getImageUrls() != null) {
            String[] imageUrls = room.getImageUrls().toArray(new String[0]);
            intent.putExtra("imageUrls", imageUrls);
        }
        
        startActivity(intent);
    }
}



