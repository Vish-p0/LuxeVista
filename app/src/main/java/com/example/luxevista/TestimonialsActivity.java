package com.example.luxevista;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.luxevista.adapters.TestimonialAdapter;
import com.example.luxevista.models.Testimonial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TestimonialsActivity extends AppCompatActivity {

    private static final String TAG = "TestimonialsActivity";

    private RecyclerView recyclerTestimonials;
    private TestimonialAdapter testimonialAdapter;
    private List<Testimonial> allTestimonials = new ArrayList<>();
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testimonials);

        db = FirebaseFirestore.getInstance();

        setupViews();
        loadAllTestimonials();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Guest Testimonials");
        }

        recyclerTestimonials = findViewById(R.id.recyclerAllTestimonials);
        testimonialAdapter = new TestimonialAdapter(this, allTestimonials);
        recyclerTestimonials.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerTestimonials.setAdapter(testimonialAdapter);
        
        bottomNavigation = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();
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
                finish(); // Go back to home
                return true;
            } else if (itemId == R.id.roomsFragment) {
                // Navigate to rooms
                return true;
            } else if (itemId == R.id.servicesFragment) {
                // Navigate to services
                return true;
            } else if (itemId == R.id.bookingsFragment) {
                // Navigate to bookings
                return true;
            } else if (itemId == R.id.profileFragment) {
                // Navigate to profile
                return true;
            }
            return false;
        });
    }

    private void loadAllTestimonials() {
        db.collection("testimonials")
                .orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allTestimonials.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No testimonials found");
                        return;
                    }

                    java.util.concurrent.atomic.AtomicInteger pendingUserLookups = 
                            new java.util.concurrent.atomic.AtomicInteger(queryDocumentSnapshots.size());

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Testimonial testimonial = document.toObject(Testimonial.class);
                            if (testimonial != null) {
                                Log.d(TAG, "Processing testimonial: " + testimonial.getComment());
                                
                                // Fetch user name from users collection
                                String userId = testimonial.getUserId();
                                if (userId != null) {
                                    db.collection("users").document(userId)
                                            .get()
                                            .addOnSuccessListener(userDoc -> {
                                                if (userDoc.exists()) {
                                                    String userName = userDoc.getString("name");
                                                    if (userName != null) {
                                                        testimonial.setUserName(userName);
                                                    } else {
                                                        testimonial.setUserName("Guest User");
                                                    }
                                                } else {
                                                    testimonial.setUserName("Guest User");
                                                }
                                                
                                                allTestimonials.add(testimonial);
                                                
                                                if (pendingUserLookups.decrementAndGet() == 0) {
                                                    testimonialAdapter.updateTestimonials(allTestimonials);
                                                    Log.d(TAG, "Loaded " + allTestimonials.size() + " total testimonials");
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error fetching user name for userId: " + userId, e);
                                                testimonial.setUserName("Guest User");
                                                allTestimonials.add(testimonial);
                                                
                                                if (pendingUserLookups.decrementAndGet() == 0) {
                                                    testimonialAdapter.updateTestimonials(allTestimonials);
                                                    Log.d(TAG, "Loaded " + allTestimonials.size() + " total testimonials");
                                                }
                                            });
                                } else {
                                    // No userId, set default name
                                    testimonial.setUserName("Guest User");
                                    allTestimonials.add(testimonial);
                                    
                                    if (pendingUserLookups.decrementAndGet() == 0) {
                                        testimonialAdapter.updateTestimonials(allTestimonials);
                                        Log.d(TAG, "Loaded " + allTestimonials.size() + " total testimonials");
                                    }
                                }
                            } else {
                                if (pendingUserLookups.decrementAndGet() == 0) {
                                    testimonialAdapter.updateTestimonials(allTestimonials);
                                    Log.d(TAG, "Loaded " + allTestimonials.size() + " total testimonials");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing testimonial document", e);
                            if (pendingUserLookups.decrementAndGet() == 0) {
                                testimonialAdapter.updateTestimonials(allTestimonials);
                                Log.d(TAG, "Loaded " + allTestimonials.size() + " total testimonials");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading all testimonials", e);
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
