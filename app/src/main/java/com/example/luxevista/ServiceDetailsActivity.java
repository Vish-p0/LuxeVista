package com.example.luxevista;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServiceDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ServiceDetailsActivity";

    // Views
    private MaterialToolbar toolbar;
    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorContainer;
    private TextView tvServiceName, tvServiceCategory, tvServicePrice, tvServiceDuration, tvServiceDescription;
    private MaterialCardView cardDateTime;
    private TextView tvSelectedDate, tvSelectedTime;
    private LinearLayout bookingSummaryLayout;
    private TextView tvSummaryService, tvSummaryDuration, tvSummaryPrice;
    private MaterialButton btnBookNow;
    private BottomNavigationView bottomNavigation;

    // Data
    private String serviceId, serviceName, serviceCategory, currency, description;
    private double price;
    private int durationMinutes;
    private String[] imageUrls;

    // Booking data
    private Calendar selectedDateTime;
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
        setContentView(R.layout.activity_service_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadDataFromIntent();
        setupToolbar();
        setupImageCarousel();
        setupDateTimeSelection();
        setupBookingButton();
        displayServiceInfo();
        setupBottomNavigation();
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
        cardDateTime = findViewById(R.id.cardDateTime);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        bookingSummaryLayout = findViewById(R.id.bookingSummaryLayout);
        tvSummaryService = findViewById(R.id.tvSummaryService);
        tvSummaryDuration = findViewById(R.id.tvSummaryDuration);
        tvSummaryPrice = findViewById(R.id.tvSummaryPrice);
        btnBookNow = findViewById(R.id.btnBookNow);
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
            getSupportActionBar().setTitle("Service Details");
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

    private void setupDateTimeSelection() {
        selectedDateTime = Calendar.getInstance();
        
        cardDateTime.setOnClickListener(v -> showDateTimePicker());
    }

    private void setupBookingButton() {
        btnBookNow.setOnClickListener(v -> {
            if (validateBookingDateTime()) {
                // Pre-fill the booking cart and redirect to main booking flow
                prefillBookingCartAndRedirect();
            }
        });
    }
    
    private void prefillBookingCartAndRedirect() {
        // Calculate end time
        Calendar endDateTime = (Calendar) selectedDateTime.clone();
        endDateTime.add(Calendar.MINUTE, durationMinutes);
        
        // Pre-fill the booking cart
        BookingCart cart = BookingCart.getInstance();
        cart.clear(); // Clear any existing selections
        cart.currency = currency != null ? currency : "USD";
        
        // Add the selected service
        cart.serviceSelections.add(new BookingCart.ServiceSelection(
            serviceId, serviceName, price, 1, new Timestamp(selectedDateTime.getTime())
        ));
        
        // Show confirmation and redirect
        showRedirectDialog();
    }
    
    private void showRedirectDialog() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String message = "Service added to booking cart:\n\n" +
                "Service: " + serviceName + "\n" +
                "Appointment: " + fullDateTimeFormat.format(selectedDateTime.getTime()) + "\n" +
                "Duration: " + getFormattedDuration(durationMinutes) + "\n" +
                "Price: " + currencyFormat.format(price) + "\n\n" +
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
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true // Use 24-hour format
        );
        
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        tvSelectedDate.setText(fullDateTimeFormat.format(selectedDateTime.getTime()));
        tvSelectedTime.setVisibility(View.VISIBLE);
        tvSelectedTime.setText("Tap to change appointment time");
        bookingSummaryLayout.setVisibility(View.VISIBLE);
    }

    private boolean validateBookingDateTime() {
        if (selectedDateTime == null) {
            Toast.makeText(this, "Please select appointment date and time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        Calendar now = Calendar.getInstance();
        if (selectedDateTime.before(now)) {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
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
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "rooms"));
                finish();
                return true;
            } else if (itemId == R.id.servicesFragment) {
                // Already in services section
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

    interface BookingIdCallback {
        void onBookingIdGenerated(String bookingId);
    }
}
