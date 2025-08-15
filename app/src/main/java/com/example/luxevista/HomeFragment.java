package com.example.luxevista;

import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.adapters.AttractionAdapter;
import com.example.luxevista.adapters.FeaturedRoomAdapter;
import com.example.luxevista.adapters.FeaturedServiceAdapter;
import com.example.luxevista.adapters.PromotionAdapter;
import com.example.luxevista.models.Attraction;
import com.example.luxevista.models.Promotion;
import com.example.luxevista.models.Room;
import com.example.luxevista.models.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment implements 
        PromotionAdapter.OnPromotionClickListener,
        AttractionAdapter.OnAttractionClickListener,
        FeaturedRoomAdapter.OnRoomClickListener,
        FeaturedServiceAdapter.OnServiceClickListener {

    private static final String TAG = "HomeFragment";

    // Views
    private FrameLayout loadingOverlay;
    private NestedScrollView mainContent;
    private TextView tvWelcomeUserName;
    private TextView tvSeeAllPromotions, tvSeeAllAttractions, tvSeeAllRooms, tvSeeAllServices;
    private RecyclerView recyclerPromotions, recyclerAttractions, recyclerFeaturedRooms, recyclerFeaturedServices;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Adapters
    private PromotionAdapter promotionAdapter;
    private AttractionAdapter attractionAdapter;
    private FeaturedRoomAdapter featuredRoomAdapter;
    private FeaturedServiceAdapter featuredServiceAdapter;

    // Data
    private List<Promotion> allPromotions = new ArrayList<>();
    private List<Attraction> allAttractions = new ArrayList<>();
    private List<Room> allRooms = new ArrayList<>();
    private List<Service> allServices = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new Fade());
        setExitTransition(new Fade());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerViews();
        setupClickListeners();
        loadUserName();
        loadAllData();

        return view;
    }

    private void initViews(View view) {
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        mainContent = view.findViewById(R.id.mainContent);
        tvWelcomeUserName = view.findViewById(R.id.tvWelcomeUserName);
        tvSeeAllPromotions = view.findViewById(R.id.tvSeeAllPromotions);
        tvSeeAllAttractions = view.findViewById(R.id.tvSeeAllAttractions);
        tvSeeAllRooms = view.findViewById(R.id.tvSeeAllRooms);
        tvSeeAllServices = view.findViewById(R.id.tvSeeAllServices);
        recyclerPromotions = view.findViewById(R.id.recyclerPromotions);
        recyclerAttractions = view.findViewById(R.id.recyclerAttractions);
        recyclerFeaturedRooms = view.findViewById(R.id.recyclerFeaturedRooms);
        recyclerFeaturedServices = view.findViewById(R.id.recyclerFeaturedServices);
    }

    private void setupRecyclerViews() {
        // Setup Promotions RecyclerView
        promotionAdapter = new PromotionAdapter(this);
        recyclerPromotions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerPromotions.setAdapter(promotionAdapter);

        // Setup Attractions RecyclerView
        attractionAdapter = new AttractionAdapter(this);
        recyclerAttractions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerAttractions.setAdapter(attractionAdapter);

        // Setup Featured Rooms RecyclerView
        featuredRoomAdapter = new FeaturedRoomAdapter(this);
        recyclerFeaturedRooms.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFeaturedRooms.setAdapter(featuredRoomAdapter);

        // Setup Featured Services RecyclerView
        featuredServiceAdapter = new FeaturedServiceAdapter(this);
        recyclerFeaturedServices.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFeaturedServices.setAdapter(featuredServiceAdapter);
    }

    private void setupClickListeners() {
        tvSeeAllPromotions.setOnClickListener(v -> {
            // Navigate to promotions section (could be part of another fragment)
            Log.d(TAG, "See all promotions clicked");
            // For now, navigate to rooms as promotions might be room-related
            Navigation.findNavController(v).navigate(R.id.roomsFragment);
        });

        tvSeeAllAttractions.setOnClickListener(v -> {
            // Navigate to attractions section
            Log.d(TAG, "See all attractions clicked");
            // For now, no dedicated attractions page, could be future feature
        });

        tvSeeAllRooms.setOnClickListener(v -> {
            // Navigate to rooms fragment
            Log.d(TAG, "Navigate to rooms fragment");
            Navigation.findNavController(v).navigate(R.id.roomsFragment);
        });

        tvSeeAllServices.setOnClickListener(v -> {
            // Navigate to services fragment
            Log.d(TAG, "Navigate to services fragment");
            Navigation.findNavController(v).navigate(R.id.servicesFragment);
        });
    }

    private void loadUserName() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Try to get user's display name or extract from email
                String displayName = currentUser.getDisplayName();
                if (displayName != null && !displayName.trim().isEmpty()) {
                    tvWelcomeUserName.setText(displayName);
                } else {
                    // Extract name from email or show email
                    String name = email.split("@")[0];
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    tvWelcomeUserName.setText(name);
                }
            } else {
                tvWelcomeUserName.setText("Guest");
            }
        } else {
            tvWelcomeUserName.setText("Guest");
        }
    }

    private void loadAllData() {
        showLoading(true);

        // Use AtomicInteger to track completion of all async operations
        AtomicInteger pendingOperations = new AtomicInteger(4);

        // Load promotions
        loadPromotions(() -> {
            if (pendingOperations.decrementAndGet() == 0) {
                showLoading(false);
            }
        });

        // Load attractions
        loadAttractions(() -> {
            if (pendingOperations.decrementAndGet() == 0) {
                showLoading(false);
            }
        });

        // Load featured rooms
        loadFeaturedRooms(() -> {
            if (pendingOperations.decrementAndGet() == 0) {
                showLoading(false);
            }
        });

        // Load featured services
        loadFeaturedServices(() -> {
            if (pendingOperations.decrementAndGet() == 0) {
                showLoading(false);
            }
        });
    }

    private void loadPromotions(Runnable onComplete) {
        Log.d(TAG, "Loading promotions from Firestore...");
        db.collection("promotions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Promotions query successful, documents found: " + queryDocumentSnapshots.size());
                    allPromotions.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "Processing promotion document: " + document.getId());
                            Log.d(TAG, "Document data: " + document.getData());
                            
                            Promotion promotion = document.toObject(Promotion.class);
                            if (promotion != null) {
                                Log.d(TAG, "Promotion parsed: " + promotion.getTitle() + ", active: " + promotion.isActive());
                                if (promotion.isActive()) {
                                    allPromotions.add(promotion);
                                    Log.d(TAG, "Added active promotion: " + promotion.getTitle());
                                }
                            } else {
                                Log.w(TAG, "Promotion object is null for document: " + document.getId());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing promotion document: " + document.getId(), e);
                        }
                    }

                    promotionAdapter.updatePromotions(allPromotions);
                    Log.d(TAG, "Loaded " + allPromotions.size() + " active promotions");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading promotions", e);
                    onComplete.run();
                });
    }

    private void loadAttractions(Runnable onComplete) {
        Log.d(TAG, "Loading attractions from Firestore...");
        db.collection("attractions")
                .whereEqualTo("visible", true)
                .orderBy("distanceKM", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Attractions query successful, documents found: " + queryDocumentSnapshots.size());
                    allAttractions.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "Processing attraction document: " + document.getId());
                            Log.d(TAG, "Document data: " + document.getData());
                            
                            Attraction attraction = document.toObject(Attraction.class);
                            if (attraction != null) {
                                Log.d(TAG, "Attraction parsed: " + attraction.getName() + ", visible: " + attraction.isVisible());
                                if (attraction.isVisible()) {
                                    allAttractions.add(attraction);
                                    Log.d(TAG, "Added attraction: " + attraction.getName());
                                }
                            } else {
                                Log.w(TAG, "Attraction object is null for document: " + document.getId());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing attraction document: " + document.getId(), e);
                        }
                    }

                    attractionAdapter.updateAttractions(allAttractions);
                    Log.d(TAG, "Loaded " + allAttractions.size() + " attractions");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading attractions", e);
                    onComplete.run();
                });
    }

    private void loadFeaturedRooms(Runnable onComplete) {
        db.collection("rooms")
                .whereEqualTo("visible", true)
                .orderBy("pricePerNight", Query.Direction.ASCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allRooms.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Room room = document.toObject(Room.class);
                            if (room != null && room.isVisible()) {
                                allRooms.add(room);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing room document", e);
                        }
                    }

                    featuredRoomAdapter.updateRooms(allRooms);
                    Log.d(TAG, "Loaded " + allRooms.size() + " featured rooms");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading featured rooms", e);
                    onComplete.run();
                });
    }

    private void loadFeaturedServices(Runnable onComplete) {
        db.collection("services")
                .orderBy("price", Query.Direction.ASCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allServices.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Service service = document.toObject(Service.class);
                            if (service != null) {
                                allServices.add(service);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing service document", e);
                        }
                    }

                    featuredServiceAdapter.updateServices(allServices);
                    Log.d(TAG, "Loaded " + allServices.size() + " featured services");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading featured services", e);
                    onComplete.run();
                });
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null && mainContent != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            mainContent.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // Click handlers
    @Override
    public void onPromotionClick(Promotion promotion) {
        Log.d(TAG, "Promotion clicked: " + promotion.getTitle());
        // Navigate to rooms fragment as promotions are often room-related
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.roomsFragment);
        }
    }

    @Override
    public void onAttractionClick(Attraction attraction) {
        Log.d(TAG, "Attraction clicked: " + attraction.getName());
        // Handle attraction click - could open details or booking
        // For now, just log the click as there's no dedicated attractions page
    }

    @Override
    public void onRoomClick(Room room) {
        Log.d(TAG, "Room clicked: " + room.getName());
        // Navigate to rooms fragment
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.roomsFragment);
        }
    }

    @Override
    public void onServiceClick(Service service) {
        Log.d(TAG, "Service clicked: " + service.getName());
        // Navigate to services fragment
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.servicesFragment);
        }
    }
}



