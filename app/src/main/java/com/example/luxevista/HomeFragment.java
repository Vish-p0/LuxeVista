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
import android.content.Intent;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
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
            Navigation.findNavController(v).navigate(R.id.promotionsGridFragment);
        });

        tvSeeAllAttractions.setOnClickListener(v -> {
            // Navigate to attractions section
            Log.d(TAG, "See all attractions clicked");
            Navigation.findNavController(v).navigate(R.id.attractionsFragment);
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

                    Date now = new Date();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "Processing promotion document: " + document.getId());
                            // Build Promotion manually to avoid type mismatches on startAt/endAt
                            Promotion p = new Promotion();
                            p.setPromotionId(document.getString("promotionId"));
                            p.setTitle(document.getString("title"));
                            p.setDescription(document.getString("description"));
                            // handle both imageURL and imageUrl
                            String imageUrl = document.getString("imageUrl");
                            if (imageUrl == null) imageUrl = document.getString("imageURL");
                            if (imageUrl != null) p.setImageUrl(imageUrl);
                            p.setPromoCode(document.getString("promoCode"));
                            Object discountRaw = document.get("discountPercent");
                            if (discountRaw instanceof Number) {
                                p.setDiscountPercent(((Number) discountRaw).intValue());
                            } else if (discountRaw instanceof String) {
                                try { p.setDiscountPercent(Integer.parseInt((String) discountRaw)); } catch (Exception ignored) {}
                            }
                            Object targetRaw = document.get("target");
                            if (targetRaw instanceof java.util.Map) {
                                //noinspection unchecked
                                p.setTarget((java.util.Map<String, Object>) targetRaw);
                            }

                            // Parse dates flexibly
                            Object startRaw = document.get("startAt");
                            Object endRaw = document.get("endAt");
                            Date startDate = parseDateFlexible(startRaw);
                            Date endDate = parseDateFlexible(endRaw);

                            boolean isActive;
                            if (startDate != null && endDate != null) {
                                isActive = !now.before(startDate) && !now.after(endDate);
                            } else {
                                isActive = p.isActive();
                            }

                            if (isActive) { allPromotions.add(p); }
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

    private Date parseDateFlexible(Object raw) {
        if (raw == null) return null;
        if (raw instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) raw).toDate();
        }
        if (raw instanceof String) {
            String s = (String) raw;
            String[] patterns = new String[] {
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
            };
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return sdf.parse(s);
                } catch (ParseException ignored) {}
            }
        }
        return null;
    }

    private void loadAttractions(Runnable onComplete) {
        Log.d(TAG, "Loading attractions from Firestore...");
        db.collection("attractions")
                .whereEqualTo("visible", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Attractions query successful, documents found: " + queryDocumentSnapshots.size());
                    allAttractions.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String docId = document.getId();
                            String attractionId = document.getString("attractionId");
                            Log.d(TAG, "Processing attraction document: " + docId + ", attractionId field: " + attractionId);
                            Log.d(TAG, "Document data: " + document.getData());

                            Attraction attraction = document.toObject(Attraction.class);
                            if (attraction != null) {
                                // Enhanced debug details
                                String firstImage = (attraction.getImageUrls() != null && !attraction.getImageUrls().isEmpty()) ? attraction.getImageUrls().get(0) : null;
                                Log.d(TAG, "Attraction parsed: name=" + attraction.getName()
                                        + ", visible=" + attraction.isVisible()
                                        + ", distanceKM=" + attraction.getDistanceKM()
                                        + ", firstImageUrl=" + firstImage);
                                if (attraction.isVisible()) {
                                    allAttractions.add(attraction);
                                }
                            } else {
                                Log.w(TAG, "Attraction object is null for document: " + docId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing attraction document: " + document.getId(), e);
                        }
                    }

                    // Client-side sort by distanceKM ascending to avoid Firestore composite index requirement
                    allAttractions.sort((a1, a2) -> Double.compare(a1.getDistanceKM(), a2.getDistanceKM()));
                    // Limit to 10 after sorting if needed
                    if (allAttractions.size() > 10) {
                        allAttractions = new ArrayList<>(allAttractions.subList(0, 10));
                    }

                    attractionAdapter.updateAttractions(allAttractions);
                    Log.d(TAG, "Loaded " + allAttractions.size() + " attractions (sorted by distanceKM)");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    String message = e.getMessage();
                    if (message != null && message.contains("FAILED_PRECONDITION")) {
                        Log.e(TAG, "Attractions query requires an index. Consider client-side sorting (implemented) or create a composite index.");
                    }
                    Log.e(TAG, "Error loading attractions", e);
                    onComplete.run();
                });
    }

    private void loadFeaturedRooms(Runnable onComplete) {
        db.collection("rooms")
                .whereEqualTo("visible", true)
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

                    // Sort client-side by price
                    allRooms.sort((r1, r2) -> Double.compare(r1.getPricePerNight(), r2.getPricePerNight()));
                    // Limit to top 5
                    List<Room> featured = allRooms.size() > 5 ? new ArrayList<>(allRooms.subList(0, 5)) : new ArrayList<>(allRooms);

                    featuredRoomAdapter.updateRooms(featured);
                    Log.d(TAG, "Loaded " + featured.size() + " featured rooms");
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
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), AttractionDetailsActivity.class);
        intent.putExtra("name", attraction.getName());
        intent.putExtra("description", attraction.getDescription());
        intent.putExtra("distanceKM", attraction.getDistanceKM());
        if (attraction.getImageUrls() != null) {
            String[] imageUrls = attraction.getImageUrls().toArray(new String[0]);
            intent.putExtra("imageUrls", imageUrls);
        }
        startActivity(intent);
    }

    @Override
    public void onRoomClick(Room room) {
        Log.d(TAG, "Room clicked: " + room.getName());
        // Open details directly so back returns to Home
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), RoomDetailsActivity.class);
        intent.putExtra("roomId", room.getRoomId());
        intent.putExtra("roomName", room.getName());
        intent.putExtra("roomType", room.getType());
        intent.putExtra("pricePerNight", room.getPricePerNight());
        intent.putExtra("currency", room.getCurrency());
        intent.putExtra("description", room.getDescription());
        intent.putExtra("maxGuests", room.getMaxGuests());
        if (room.getImageUrls() != null) {
            String[] imageUrls = room.getImageUrls().toArray(new String[0]);
            intent.putExtra("imageUrls", imageUrls);
        }
        startActivity(intent);
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



