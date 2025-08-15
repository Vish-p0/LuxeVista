package com.example.luxevista;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
                createBooking();
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

    private void createBooking() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to make a booking", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        btnBookNow.setEnabled(false);
        btnBookNow.setText("Creating Booking...");

        // Generate booking ID
        generateBookingId((bookingId) -> {
            // Calculate end time
            Calendar endDateTime = (Calendar) selectedDateTime.clone();
            endDateTime.add(Calendar.MINUTE, durationMinutes);

            // Create booking document
            Map<String, Object> booking = new HashMap<>();
            booking.put("bookingId", bookingId);
            booking.put("userId", currentUser.getUid());
            booking.put("type", "service");
            booking.put("itemId", serviceId);
            booking.put("startDate", new Timestamp(selectedDateTime.getTime()));
            booking.put("endDate", new Timestamp(endDateTime.getTime()));
            booking.put("status", "confirmed");
            booking.put("price", price);
            booking.put("currency", currency != null ? currency : "USD");
            booking.put("createdAt", Timestamp.now());

            // Save to Firestore
            db.collection("bookings")
                    .add(booking)
                    .addOnSuccessListener(documentReference -> {
                        btnBookNow.setEnabled(true);
                        btnBookNow.setText("Book Now");
                        showBookingSuccessDialog(bookingId);
                    })
                    .addOnFailureListener(e -> {
                        btnBookNow.setEnabled(true);
                        btnBookNow.setText("Book Now");
                        Log.e(TAG, "Error creating booking", e);
                        Toast.makeText(this, "Failed to create booking. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void generateBookingId(BookingIdCallback callback) {
        db.collection("bookings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String newBookingId;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the last booking ID and increment
                        String lastBookingId = queryDocumentSnapshots.getDocuments().get(0).getString("bookingId");
                        if (lastBookingId != null && lastBookingId.startsWith("booking")) {
                            try {
                                int lastNumber = Integer.parseInt(lastBookingId.substring(7));
                                newBookingId = String.format("booking%03d", lastNumber + 1);
                            } catch (NumberFormatException e) {
                                newBookingId = "booking001";
                            }
                        } else {
                            newBookingId = "booking001";
                        }
                    } else {
                        newBookingId = "booking001";
                    }
                    callback.onBookingIdGenerated(newBookingId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error generating booking ID", e);
                    // Fallback to timestamp-based ID
                    String fallbackId = "booking" + System.currentTimeMillis();
                    callback.onBookingIdGenerated(fallbackId);
                });
    }

    private void showBookingSuccessDialog(String bookingId) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        // Calculate end time for display
        Calendar endDateTime = (Calendar) selectedDateTime.clone();
        endDateTime.add(Calendar.MINUTE, durationMinutes);
        
        String message = "Service Booking Confirmation\n\n" +
                "Booking ID: " + bookingId + "\n" +
                "Service: " + serviceName + "\n" +
                "Appointment: " + fullDateTimeFormat.format(selectedDateTime.getTime()) + "\n" +
                "Duration: " + getFormattedDuration(durationMinutes) + "\n" +
                "Total: " + currencyFormat.format(price);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Booking Successful!")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back or to bookings page
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    interface BookingIdCallback {
        void onBookingIdGenerated(String bookingId);
    }
}
