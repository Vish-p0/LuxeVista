package com.example.luxevista;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.luxevista.adapters.RoomImageAdapter;
import com.example.luxevista.models.Service;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServiceBookingActivity extends AppCompatActivity {

    private static final String TAG = "ServiceBookingActivity";

    // Views
    private MaterialToolbar toolbar;
    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorContainer;
    private TextView tvServiceName, tvServiceCategory, tvServicePrice, tvServiceDuration, tvServiceDescription;
    
    // Booking type selection
    private ChipGroup chipGroupBookingType;
    private Chip chipWithRoom, chipStandalone;
    
    // Date and time selection
    private MaterialCardView cardDateTime;
    private TextView tvSelectedDate, tvSelectedTime;
    private MaterialButton btnSelectDateTime;
    
    // Time slots selection
    private LinearLayout timeSlotsContainer;
    private TextView tvTimeSlotsTitle;
    
    // Room selection (only shown for "Booking with Room")
    private LinearLayout roomSelectionContainer;
    private TextView tvRoomSelectionTitle;
    private MaterialButton btnSelectRoom;
    
    // Booking summary
    private LinearLayout bookingSummaryLayout;
    private TextView tvSummaryService, tvSummaryDuration, tvSummaryPrice, tvSummaryTotal;
    private MaterialButton btnConfirmBooking;
    private BottomNavigationView bottomNavigation;

    // Data
    private String serviceId, serviceName, serviceCategory, currency, description;
    private double price;
    private int durationMinutes;
    private String[] imageUrls;
    private Service service;

    // Booking data
    private String selectedBookingType = "standalone"; // "with_room" or "standalone"
    private Calendar selectedDateTime;
    private String selectedTimeSlot;
    private String selectedRoomId;
    private String selectedRoomName;
    private double selectedRoomPrice;
    
    // Date and time formatters
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat fullDateTimeFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Adapters
    private RoomImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_booking);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadDataFromIntent();
        setupToolbar();
        setupImageCarousel();
        setupBookingTypeSelection();
        setupDateTimeSelection();
        setupTimeSlots();
        setupRoomSelection();
        setupConfirmButton();
        displayServiceInfo();
        setupBottomNavigation();
        
        // Load service details from Firestore
        loadServiceDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        indicatorContainer = findViewById(R.id.indicatorContainer);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServiceCategory = findViewById(R.id.tvServiceCategory);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        tvServiceDuration = findViewById(R.id.tvServiceDuration);
        tvServiceDescription = findViewById(R.id.tvServiceDescription);
        
        // Booking type selection
        chipGroupBookingType = findViewById(R.id.chipGroupBookingType);
        chipWithRoom = findViewById(R.id.chipWithRoom);
        chipStandalone = findViewById(R.id.chipStandalone);
        
        // Date and time selection
        cardDateTime = findViewById(R.id.cardDateTime);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnSelectDateTime = findViewById(R.id.btnSelectDateTime);
        
        // Time slots
        timeSlotsContainer = findViewById(R.id.timeSlotsContainer);
        tvTimeSlotsTitle = findViewById(R.id.tvTimeSlotsTitle);
        
        // Room selection
        roomSelectionContainer = findViewById(R.id.roomSelectionContainer);
        tvRoomSelectionTitle = findViewById(R.id.tvRoomSelectionTitle);
        btnSelectRoom = findViewById(R.id.btnSelectRoom);
        
        // Booking summary
        bookingSummaryLayout = findViewById(R.id.bookingSummaryLayout);
        tvSummaryService = findViewById(R.id.tvSummaryService);
        tvSummaryDuration = findViewById(R.id.tvSummaryDuration);
        tvSummaryPrice = findViewById(R.id.tvSummaryPrice);
        tvSummaryTotal = findViewById(R.id.tvSummaryTotal);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        serviceId = intent.getStringExtra("serviceId");
        serviceName = intent.getStringExtra("serviceName");
        serviceCategory = intent.getStringExtra("serviceCategory");
        price = intent.getDoubleExtra("price", 0);
        currency = intent.getStringExtra("currency");
        description = intent.getStringExtra("description");
        durationMinutes = intent.getIntExtra("durationMinutes", 60);
        imageUrls = intent.getStringArrayExtra("imageUrls");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Service");
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
            List<String> placeholderList = Arrays.asList((String) null); // Will use placeholder
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

    private void setupBookingTypeSelection() {
        // Set standalone as default
        chipStandalone.setChecked(true);
        selectedBookingType = "standalone";
        
        chipWithRoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedBookingType = "with_room";
                chipStandalone.setChecked(false);
                updateUIForBookingType();
            }
        });
        
        chipStandalone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedBookingType = "standalone";
                chipWithRoom.setChecked(false);
                updateUIForBookingType();
            }
        });
    }

    private void updateUIForBookingType() {
        if ("with_room".equals(selectedBookingType)) {
            roomSelectionContainer.setVisibility(View.VISIBLE);
            // Show room selection UI
        } else {
            roomSelectionContainer.setVisibility(View.GONE);
            selectedRoomId = null;
            selectedRoomName = null;
            selectedRoomPrice = 0;
        }
        updateBookingSummary();
    }

    private void setupDateTimeSelection() {
        selectedDateTime = Calendar.getInstance();
        
        btnSelectDateTime.setOnClickListener(v -> showDateTimePicker());
    }

    private void setupTimeSlots() {
        // Time slots will be populated when a date is selected
    }

    private void setupRoomSelection() {
        btnSelectRoom.setOnClickListener(v -> showRoomSelectionDialog());
    }

    private void setupConfirmButton() {
        btnConfirmBooking.setOnClickListener(v -> {
            if (validateBooking()) {
                confirmBooking();
            }
        });
    }

    private void displayServiceInfo() {
        tvServiceName.setText(serviceName);
        tvServiceCategory.setText(serviceCategory);
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        tvServicePrice.setText(currencyFormat.format(price));
        
        tvServiceDuration.setText(getFormattedDuration(durationMinutes));
        tvServiceDescription.setText(description);
        
        // Update summary
        tvSummaryService.setText(serviceName);
        tvSummaryDuration.setText(getFormattedDuration(durationMinutes));
        tvSummaryPrice.setText(currencyFormat.format(price));
    }

    private void loadServiceDetails() {
        if (serviceId == null) return;
        
        db.collection("services").document(serviceId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    service = documentSnapshot.toObject(Service.class);
                    if (service != null) {
                        // Service loaded successfully
                        Log.d(TAG, "Service loaded: " + service.getName());
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading service details", e);
                Toast.makeText(this, "Error loading service details", Toast.LENGTH_SHORT).show();
            });
    }

    private void showDateTimePicker() {
        Calendar minDate = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);
                    showTimePicker();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                    loadAvailableTimeSlots();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true // Use 24-hour format
        );
        
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
        tvSelectedTime.setVisibility(View.VISIBLE);
        tvSelectedTime.setText("Tap to change appointment time");
    }

    private void loadAvailableTimeSlots() {
        if (service == null || selectedDateTime == null) return;
        
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDateTime.getTime());
        List<String> availableTimes = service.getAvailableTimesForDate(dateKey);
        
        displayTimeSlots(availableTimes);
    }

    private void displayTimeSlots(List<String> availableTimes) {
        timeSlotsContainer.removeAllViews();
        
        if (availableTimes.isEmpty()) {
            tvTimeSlotsTitle.setText("No available time slots for selected date");
            return;
        }
        
        tvTimeSlotsTitle.setText("Select Time Slot");
        
        for (String time : availableTimes) {
            Chip timeChip = new Chip(this);
            timeChip.setText(time);
            timeChip.setCheckable(true);
            timeChip.setChipBackgroundColorResource(R.color.light_blue_background);
            timeChip.setTextColor(getResources().getColor(R.color.dark_blue_primary));
            
            timeChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck other chips
                    for (int i = 0; i < timeSlotsContainer.getChildCount(); i++) {
                        View child = timeSlotsContainer.getChildAt(i);
                        if (child instanceof Chip && child != buttonView) {
                            ((Chip) child).setChecked(false);
                        }
                    }
                    selectedTimeSlot = time;
                    updateBookingSummary();
                } else {
                    selectedTimeSlot = null;
                    updateBookingSummary();
                }
            });
            
            timeSlotsContainer.addView(timeChip);
        }
    }

    private void showRoomSelectionDialog() {
        // Load available rooms for the selected dates
        if (selectedDateTime == null) {
            Toast.makeText(this, "Please select dates first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // For now, show a simple dialog. In a full implementation, you'd load rooms from Firestore
        new MaterialAlertDialogBuilder(this)
            .setTitle("Select Room")
            .setItems(new String[]{"Deluxe Room - $150/night", "Suite - $250/night"}, (dialog, which) -> {
                if (which == 0) {
                    selectedRoomId = "room001";
                    selectedRoomName = "Deluxe Room";
                    selectedRoomPrice = 150;
                } else {
                    selectedRoomId = "room002";
                    selectedRoomName = "Suite";
                    selectedRoomPrice = 250;
                }
                updateBookingSummary();
            })
            .show();
    }

    private boolean validateBooking() {
        if (selectedDateTime == null) {
            Toast.makeText(this, "Please select appointment date and time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if ("with_room".equals(selectedBookingType) && selectedRoomId == null) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        Calendar now = Calendar.getInstance();
        if (selectedDateTime.before(now)) {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    private void updateBookingSummary() {
        if (selectedDateTime != null && selectedTimeSlot != null) {
            bookingSummaryLayout.setVisibility(View.VISIBLE);
            
            // Calculate total
            double total = price;
            if ("with_room".equals(selectedBookingType) && selectedRoomId != null) {
                // Add room cost for one night
                total += selectedRoomPrice;
            }
            
            tvSummaryTotal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(total));
        } else {
            bookingSummaryLayout.setVisibility(View.GONE);
        }
    }

    private void confirmBooking() {
        // Create booking in Firestore
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to book services", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create the booking
        createBookingInFirestore();
    }

    private void createBookingInFirestore() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        
        // Generate booking ID
        String bookingId = "booking_" + System.currentTimeMillis();
        
        // Calculate end time
        Calendar endDateTime = (Calendar) selectedDateTime.clone();
        endDateTime.add(Calendar.MINUTE, durationMinutes);
        
        // Create service booking
        NewBooking.ServiceBooking serviceBooking = new NewBooking.ServiceBooking(
            serviceId, new Timestamp(selectedDateTime.getTime()), price, 1
        );
        serviceBooking.setServiceName(serviceName);
        
        // Create the main booking
        NewBooking booking = new NewBooking();
        booking.setBookingId(bookingId);
        booking.setUserId(currentUser.getUid());
        booking.setStartDate(new Timestamp(selectedDateTime.getTime()));
        booking.setEndDate(new Timestamp(endDateTime.getTime()));
        booking.setStatus("confirmed");
        booking.setCurrency(currency != null ? currency : "USD");
        booking.setTotalPrice(price);
        booking.setCreatedAt(new Timestamp(new Date()));
        booking.setServices(Arrays.asList(serviceBooking));
        
        // Save to Firestore
        db.collection("bookings").document(bookingId).set(booking)
            .addOnSuccessListener(aVoid -> {
                // Update service availability
                updateServiceAvailability();
                
                // Show success and navigate
                showBookingSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating booking", e);
                Toast.makeText(this, "Error creating booking. Please try again.", Toast.LENGTH_SHORT).show();
            });
    }

    private void updateServiceAvailability() {
        if (service == null || selectedDateTime == null || selectedTimeSlot == null) return;
        
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDateTime.getTime());
        
        // Update the availability map
        Map<String, Map<String, Integer>> availability = service.getAvailability();
        if (availability == null) {
            availability = new HashMap<>();
        }
        
        Map<String, Integer> dateAvailability = availability.get(dateKey);
        if (dateAvailability == null) {
            dateAvailability = new HashMap<>();
        }
        
        // Increment booked count (availability map stores booked counts)
        int currentBooked = dateAvailability.getOrDefault(selectedTimeSlot, 0);
        dateAvailability.put(selectedTimeSlot, currentBooked + 1);
        availability.put(dateKey, dateAvailability);
        
        // Update service in Firestore
        db.collection("services").document(serviceId).update("availability", availability)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Service availability updated successfully");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating service availability", e);
            });
    }

    private void showBookingSuccess() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Booking Confirmed!")
            .setMessage("Your service has been booked successfully.")
            .setPositiveButton("View Bookings", (dialog, which) -> {
                // Navigate to bookings
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "bookings");
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Book Another", (dialog, which) -> {
                // Stay in this activity to book another service
                resetForm();
            })
            .setCancelable(false)
            .show();
    }

    private void resetForm() {
        selectedDateTime = null;
        selectedTimeSlot = null;
        selectedRoomId = null;
        selectedRoomName = null;
        selectedRoomPrice = 0;
        
        tvSelectedDate.setText("Select Date");
        tvSelectedTime.setVisibility(View.GONE);
        timeSlotsContainer.removeAllViews();
        bookingSummaryLayout.setVisibility(View.GONE);
        
        // Reset time slot chips
        for (int i = 0; i < timeSlotsContainer.getChildCount(); i++) {
            View child = timeSlotsContainer.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }
    }

    private String getFormattedDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + (hours == 1 ? " hour" : " hours");
            } else {
                return hours + "h " + remainingMinutes + "m";
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setItemActiveIndicatorEnabled(false);
        bottomNavigation.setItemRippleColor(null);
        bottomNavigation.setItemBackground(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeFragment) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.roomsFragment) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "rooms"));
                finish();
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
        
        // Set services as selected
        bottomNavigation.setSelectedItemId(R.id.servicesFragment);
    }
}

