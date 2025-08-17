package com.example.luxevista;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.luxevista.adapters.RoomImageAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttractionDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorContainer;
    private TextView tvName, tvDistance, tvDescription;

    private String name, description;
    private double distanceKM;
    private String[] imageUrls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attraction_details);

        toolbar = findViewById(R.id.toolbar);
        viewPagerImages = findViewById(R.id.viewPagerImages);
        indicatorContainer = findViewById(R.id.indicatorContainer);
        tvName = findViewById(R.id.tvName);
        tvDistance = findViewById(R.id.tvDistance);
        tvDescription = findViewById(R.id.tvDescription);

        name = getIntent().getStringExtra("name");
        description = getIntent().getStringExtra("description");
        distanceKM = getIntent().getDoubleExtra("distanceKM", 0);
        imageUrls = getIntent().getStringArrayExtra("imageUrls");

        setupToolbar();
        setupImageCarousel();
        displayInfo();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Attraction Details");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
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

        indicatorContainer.removeAllViews();
        if (images.size() <= 1) {
            indicatorContainer.setVisibility(View.GONE);
        } else {
            indicatorContainer.setVisibility(View.VISIBLE);
            for (int i = 0; i < images.size(); i++) {
                View dot = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
                if (i > 0) params.leftMargin = 16;
                dot.setLayoutParams(params);
                dot.setBackground(getDrawable(R.drawable.bg_indicator_inactive));
                indicatorContainer.addView(dot);
            }
            indicatorContainer.getChildAt(0).setBackground(getDrawable(R.drawable.bg_indicator_active));

            viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
                        View child = indicatorContainer.getChildAt(i);
                        child.setBackground(getDrawable(i == position ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive));
                    }
                }
            });
        }
    }

    private void displayInfo() {
        tvName.setText(name != null ? name : "Attraction");
        tvDistance.setText(String.format(java.util.Locale.getDefault(), "%.1f km away", distanceKM));
        tvDescription.setText(description);
    }
}


