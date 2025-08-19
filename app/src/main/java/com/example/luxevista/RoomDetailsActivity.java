package com.example.luxevista;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.luxevista.adapters.AmenityAdapter;
import com.example.luxevista.adapters.RoomImageAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RoomDetailsActivity extends AppCompatActivity {

    private static final String TAG = "RoomDetailsActivity";

    // Views
    private MaterialToolbar toolbar;
    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorContainer;
    private TextView tvRoomName, tvRoomType, tvRoomPrice, tvMaxGuests, tvRoomDescription;
    private RecyclerView recyclerAmenities;
    private MaterialCardView cardCheckIn, cardCheckOut;
    private TextView tvCheckInDate, tvCheckOutDate;
    private LinearLayout totalPriceLayout;
    private TextView tvTotalPrice, tvTotalNights;
    private MaterialButton btnBookNow;
    private BottomNavigationView bottomNavigation;

    // Data
    private String roomId, roomName, roomType, currency, description;
    private double pricePerNight;
    private int maxGuests;
    private String[] imageUrls;
    private List<AmenityItem> amenities = new ArrayList<>();

    // Booking data
    private Calendar checkInCalendar, checkOutCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Adapters
    private RoomImageAdapter imageAdapter;
    private AmenityAdapter amenityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadDataFromIntent();
        setupToolbar();
        setupImageCarousel();
        setupAmenities();
        setupDatePickers();
        setupBookingButton();
        displayRoomInfo();
        setupBottomNavigation();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        indicatorContainer = findViewById(R.id.indicatorContainer);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvRoomPrice = findViewById(R.id.tvRoomPrice);
        tvMaxGuests = findViewById(R.id.tvMaxGuests);
        tvRoomDescription = findViewById(R.id.tvRoomDescription);
        recyclerAmenities = findViewById(R.id.recyclerAmenities);
        cardCheckIn = findViewById(R.id.cardCheckIn);
        cardCheckOut = findViewById(R.id.cardCheckOut);
        tvCheckInDate = findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = findViewById(R.id.tvCheckOutDate);
        totalPriceLayout = findViewById(R.id.totalPriceLayout);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvTotalNights = findViewById(R.id.tvTotalNights);
        btnBookNow = findViewById(R.id.btnBookNow);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        roomName = intent.getStringExtra("roomName");
        roomType = intent.getStringExtra("roomType");
        pricePerNight = intent.getDoubleExtra("pricePerNight", 0);
        currency = intent.getStringExtra("currency");
        description = intent.getStringExtra("description");
        maxGuests = intent.getIntExtra("maxGuests", 1);
        imageUrls = intent.getStringArrayExtra("imageUrls");

        // Load amenities from room data (you might want to pass this as well or fetch from Firestore)
        loadAmenitiesFromFirestore();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Room Details");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupImageCarousel() {
        if (imageUrls != null && imageUrls.length > 0) {
            imageAdapter = new RoomImageAdapter(Arrays.asList(imageUrls));
            viewPagerImages.setAdapter(imageAdapter);
            
            // Setup page indicators
            setupImageIndicators();
            
            viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateIndicators(position);
                }
            });
        } else {
            // Show placeholder if no images
            List<String> placeholderList = new ArrayList<>();
            placeholderList.add(null); // Will use placeholder
            imageAdapter = new RoomImageAdapter(placeholderList);
            viewPagerImages.setAdapter(imageAdapter);
        }
    }

    private void setupImageIndicators() {
        if (imageUrls == null || imageUrls.length <= 1) {
            indicatorContainer.setVisibility(View.GONE);
            return;
        }

        indicatorContainer.removeAllViews();
        for (int i = 0; i < imageUrls.length; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
            if (i > 0) params.leftMargin = 16;
            indicator.setLayoutParams(params);
            indicator.setBackground(getDrawable(R.drawable.bg_indicator_inactive));
            indicatorContainer.addView(indicator);
        }
        
        if (indicatorContainer.getChildCount() > 0) {
            indicatorContainer.getChildAt(0).setBackground(getDrawable(R.drawable.bg_indicator_active));
        }
    }

    private void updateIndicators(int selectedPosition) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            View indicator = indicatorContainer.getChildAt(i);
            if (i == selectedPosition) {
                indicator.setBackground(getDrawable(R.drawable.bg_indicator_active));
            } else {
                indicator.setBackground(getDrawable(R.drawable.bg_indicator_inactive));
            }
        }
    }

    private void setupBottomNavigation() {
        try {
            bottomNavigation.setItemActiveIndicatorEnabled(false);
        } catch (Throwable ignored) { }
        bottomNavigation.setItemRippleColor(null);
        bottomNavigation.setItemBackground(new ColorDrawable(Color.TRANSPARENT));

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeFragment) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.roomsFragment) {
                // Already in rooms section
                return true;
            } else if (itemId == R.id.servicesFragment) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "services"));
                finish();
                return true;
            } else if (itemId == R.id.bookingsFragment) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "bookings"));
                finish();
                return true;
            } else if (itemId == R.id.profileFragment) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "profile"));
                finish();
                return true;
            }
            return false;
        });
        
        // Set rooms as selected
        bottomNavigation.setSelectedItemId(R.id.roomsFragment);
    }

    private void setupAmenities() {
        amenityAdapter = new AmenityAdapter();
        recyclerAmenities.setLayoutManager(new LinearLayoutManager(this));
        recyclerAmenities.setAdapter(amenityAdapter);
        
        // Convert AmenityItem list to String list
        List<String> amenityKeys = new ArrayList<>();
        for (AmenityItem item : amenities) {
            amenityKeys.add(item.getKey());
        }
        amenityAdapter.updateAmenities(amenityKeys);
    }

    private void setupDatePickers() {
        checkInCalendar = Calendar.getInstance();
        checkOutCalendar = Calendar.getInstance();
        checkOutCalendar.add(Calendar.DAY_OF_MONTH, 1); // Default to next day

        cardCheckIn.setOnClickListener(v -> showCheckInDatePicker());
        cardCheckOut.setOnClickListener(v -> showCheckOutDatePicker());
    }

    private void setupBookingButton() {
        btnBookNow.setText("Continue Booking");
        btnBookNow.setOnClickListener(v -> {
            if (validateBookingDates()) {
                // Pre-fill the booking cart and redirect to main booking flow
                prefillBookingCartAndRedirect();
            }
        });
    }
    
    private void prefillBookingCartAndRedirect() {
        // Calculate total price for display
        long diffInMillis = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long nights = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        double totalPrice = nights * pricePerNight;
        
        // Pre-fill the booking cart
        BookingCart cart = BookingCart.getInstance();
        cart.clear(); // Clear any existing selections
        cart.checkIn = new Timestamp(checkInCalendar.getTime());
        cart.checkOut = new Timestamp(checkOutCalendar.getTime());
        cart.currency = currency != null ? currency : "USD";
        
        // Add the selected room
        cart.roomSelections.put(roomId, new BookingCart.RoomSelection(
            roomId, roomName, pricePerNight, 1
        ));
        
        // Show confirmation and redirect
        showRedirectDialog(totalPrice, nights);
    }
    
    private void showRedirectDialog(double totalPrice, long nights) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String message = "Room added to booking cart:\n\n" +
                "Room: " + roomName + "\n" +
                "Check-in: " + dateFormat.format(checkInCalendar.getTime()) + "\n" +
                "Check-out: " + dateFormat.format(checkOutCalendar.getTime()) + "\n" +
                "Nights: " + nights + "\n" +
                "Price: " + currencyFormat.format(totalPrice) + "\n\n" +
                "You can add more rooms or services in the main booking flow.";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Continue to Booking")
                .setMessage(message)
                .setPositiveButton("Continue", (dialog, which) -> {
                    // Navigate to main booking flow
                    redirectToMainBookingFlow();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void redirectToMainBookingFlow() {
        try {
            // Navigate to the main booking flow
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "booking");
            intent.putExtra("prefilled", true);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to booking flow", e);
            // Fallback: show toast and navigate to main activity
            Toast.makeText(this, "Redirecting to booking...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "booking");
            startActivity(intent);
            finish();
        }
    }

    private void displayRoomInfo() {
        tvRoomName.setText(roomName);
        tvRoomType.setText(roomType);
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        tvRoomPrice.setText(currencyFormat.format(pricePerNight));
        
        tvMaxGuests.setText(maxGuests + (maxGuests == 1 ? " guest" : " guests"));
        tvRoomDescription.setText(description);
    }

    private void loadAmenitiesFromFirestore() {
        // Fetch room details from Firestore to get amenities
        db.collection("rooms").document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        return;
                    }
                    Map<String, Object> amenityMap = (Map<String, Object>) documentSnapshot.get("amenities");
                    if (amenityMap == null) {
                        // No amenities field
                        amenityAdapter.updateAmenities(new ArrayList<>());
                        return;
                    }

                    amenities.clear();
                    for (Map.Entry<String, Object> entry : amenityMap.entrySet()) {
                        if (isTruthy(entry.getValue())) {
                            String key = entry.getKey();
                            amenities.add(new AmenityItem(key, getAmenityDisplayName(key)));
                        }
                    }

                    // Push the updated amenity keys into the adapter
                    List<String> amenityKeys = new ArrayList<>();
                    for (AmenityItem item : amenities) {
                        amenityKeys.add(item.getKey());
                    }
                    amenityAdapter.updateAmenities(amenityKeys);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading amenities", e));
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        if (value instanceof String) {
            String s = ((String) value).trim();
            return "true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s);
        }
        return false;
    }

    private String getAmenityDisplayName(String amenityKey) {
        switch (amenityKey) {
            case "wifi": return "WiFi";
            case "airConditioning": return "Air Conditioning";
            case "television": return "Television";
            case "roomService": return "Room Service";
            case "nonSmoking": return "Non-Smoking";
            case "wheelchairAccessible": return "Wheelchair Accessible";
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

    private void showCheckInDatePicker() {
        Calendar minDate = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    checkInCalendar.set(year, month, dayOfMonth);
                    tvCheckInDate.setText(dateFormat.format(checkInCalendar.getTime()));
                    
                    // Update check-out minimum date
                    Calendar minCheckOut = (Calendar) checkInCalendar.clone();
                    minCheckOut.add(Calendar.DAY_OF_MONTH, 1);
                    if (checkOutCalendar.before(minCheckOut)) {
                        checkOutCalendar = minCheckOut;
                        tvCheckOutDate.setText(dateFormat.format(checkOutCalendar.getTime()));
                    }
                    
                    updateTotalPrice();
                },
                checkInCalendar.get(Calendar.YEAR),
                checkInCalendar.get(Calendar.MONTH),
                checkInCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showCheckOutDatePicker() {
        Calendar minDate = (Calendar) checkInCalendar.clone();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    checkOutCalendar.set(year, month, dayOfMonth);
                    tvCheckOutDate.setText(dateFormat.format(checkOutCalendar.getTime()));
                    updateTotalPrice();
                },
                checkOutCalendar.get(Calendar.YEAR),
                checkOutCalendar.get(Calendar.MONTH),
                checkOutCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateTotalPrice() {
        if (checkInCalendar != null && checkOutCalendar != null) {
            long diffInMillis = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
            long nights = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            
            if (nights > 0) {
                double totalPrice = nights * pricePerNight;
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                tvTotalPrice.setText(currencyFormat.format(totalPrice));
                tvTotalNights.setText(" (" + nights + (nights == 1 ? " night)" : " nights)"));
                totalPriceLayout.setVisibility(View.VISIBLE);
            } else {
                totalPriceLayout.setVisibility(View.GONE);
            }
        }
    }

    private boolean validateBookingDates() {
        if (checkInCalendar == null || checkOutCalendar == null) {
            Toast.makeText(this, "Please select check-in and check-out dates", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (checkOutCalendar.before(checkInCalendar) || checkOutCalendar.equals(checkInCalendar)) {
            Toast.makeText(this, "Check-out date must be after check-in date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    public static class AmenityItem {
        private final String key;
        private final String displayName;

        public AmenityItem(String key, String displayName) {
            this.key = key;
            this.displayName = displayName;
        }

        public String getKey() { return key; }
        public String getDisplayName() { return displayName; }
    }
}
