package com.example.luxevista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.luxevista.adapters.RoomImageAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttractionDetailsActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorContainer;
    private TextView tvName, tvDistance, tvDescription, tvImageCounter;
    private FloatingActionButton fabBack;
    private MaterialButton btnSave;
    private BottomNavigationView bottomNavigation;

    private String name, description, attractionId;
    private double distanceKM;
    private String[] imageUrls;
    
    private SharedPreferences sharedPreferences;
    private static final String FAVORITES_PREFS = "attraction_favorites";
    private static final String FAVORITES_KEY = "favorite_attractions";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attraction_details);

        sharedPreferences = getSharedPreferences(FAVORITES_PREFS, MODE_PRIVATE);
        
        initViews();
        getIntentData();
        setupImageCarousel();
        setupClickListeners();
        displayInfo();
        updateFavoriteButtonState();
        setupBottomNavigation();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        indicatorContainer = findViewById(R.id.indicatorContainer);
        tvName = findViewById(R.id.tvName);
        tvDistance = findViewById(R.id.tvDistance);
        tvDescription = findViewById(R.id.tvDescription);
        tvImageCounter = findViewById(R.id.tvImageCounter);
        fabBack = findViewById(R.id.fabBack);
        btnSave = findViewById(R.id.btnSave);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void getIntentData() {
        name = getIntent().getStringExtra("name");
        description = getIntent().getStringExtra("description");
        distanceKM = getIntent().getDoubleExtra("distanceKM", 0);
        imageUrls = getIntent().getStringArrayExtra("imageUrls");
        attractionId = getIntent().getStringExtra("attractionId");
        
        // Generate an ID if not provided
        if (attractionId == null || attractionId.isEmpty()) {
            attractionId = name != null ? name.replaceAll("\\s+", "_").toLowerCase() : "unknown";
        }
    }

    private void setupClickListeners() {
        fabBack.setOnClickListener(v -> onBackPressed());
        btnSave.setOnClickListener(v -> toggleFavorite());
    }

    private void toggleFavorite() {
        Set<String> favorites = getFavorites();
        
        if (favorites.contains(attractionId)) {
            // Remove from favorites
            favorites.remove(attractionId);
            saveFavorites(favorites);
            Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
        } else {
            // Add to favorites
            favorites.add(attractionId);
            saveFavorites(favorites);
            Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
        }
        
        updateFavoriteButtonState();
    }
    
    private Set<String> getFavorites() {
        return new HashSet<>(sharedPreferences.getStringSet(FAVORITES_KEY, new HashSet<>()));
    }
    
    private void saveFavorites(Set<String> favorites) {
        sharedPreferences.edit()
                .putStringSet(FAVORITES_KEY, favorites)
                .apply();
    }
    
    private boolean isFavorite() {
        return getFavorites().contains(attractionId);
    }
    
    private void updateFavoriteButtonState() {
        if (isFavorite()) {
            btnSave.setText(getString(R.string.remove_from_favorites));
            btnSave.setIcon(getDrawable(R.drawable.ic_bookmark_filled));
        } else {
            btnSave.setText(getString(R.string.add_to_favorites));
            btnSave.setIcon(getDrawable(R.drawable.ic_bookmark));
        }
    }

    private void setupImageCarousel() {
        List<String> images;
        if (imageUrls != null && imageUrls.length > 0) {
            images = Arrays.asList(imageUrls);
        } else {
            images = new ArrayList<>();
            images.add(null); // placeholder
        }
        
        RoomImageAdapter imageAdapter = new RoomImageAdapter(images);
        viewPagerImages.setAdapter(imageAdapter);

        // Setup image counter
        if (images.size() > 1) {
            tvImageCounter.setVisibility(View.VISIBLE);
            tvImageCounter.setText(String.format("1 / %d", images.size()));
        } else {
            tvImageCounter.setVisibility(View.GONE);
        }

        // Setup indicators
        setupIndicators(images.size());

        // Setup page change callback
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                if (images.size() > 1) {
                    tvImageCounter.setText(String.format("%d / %d", position + 1, images.size()));
                }
            }
        });
    }

    private void setupIndicators(int count) {
        indicatorContainer.removeAllViews();
        if (count <= 1) {
            indicatorContainer.setVisibility(View.GONE);
            return;
        }

        indicatorContainer.setVisibility(View.VISIBLE);
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.indicator_size),
                getResources().getDimensionPixelSize(R.dimen.indicator_size)
            );
            if (i > 0) {
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.indicator_margin);
            }
            dot.setLayoutParams(params);
            dot.setBackground(getDrawable(R.drawable.bg_indicator_inactive));
            indicatorContainer.addView(dot);
        }
        
        // Set first indicator as active
        if (indicatorContainer.getChildCount() > 0) {
            indicatorContainer.getChildAt(0).setBackground(getDrawable(R.drawable.bg_indicator_active));
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            View child = indicatorContainer.getChildAt(i);
            child.setBackground(getDrawable(i == position ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive));
        }
    }

    private void displayInfo() {
        tvName.setText(name != null ? name : "Attraction");
        tvDistance.setText(String.format(java.util.Locale.getDefault(), "%.1f km away", distanceKM));
        tvDescription.setText(description);
    }

    private void setupBottomNavigation() {
        // Match Home bottom nav visuals
        try {
            bottomNavigation.setItemActiveIndicatorEnabled(false);
        } catch (Throwable ignored) { }
        bottomNavigation.setItemRippleColor(null);
        bottomNavigation.setItemBackground(new ColorDrawable(Color.TRANSPARENT));

        // Select Home tab first so it doesn't trigger navigation callback (attach listener after)
        bottomNavigation.setSelectedItemId(R.id.homeFragment);

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
    }
}


