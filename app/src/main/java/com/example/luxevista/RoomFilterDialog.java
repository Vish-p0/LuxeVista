package com.example.luxevista;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomFilterDialog {

    public interface OnFilterAppliedListener {
        void onFilterApplied(double minPrice, double maxPrice, List<String> selectedAmenities);
    }

    private final Context context;
    private double currentMinPrice;
    private double currentMaxPrice;
    private List<String> currentSelectedAmenities;
    private final OnFilterAppliedListener listener;

    private RangeSlider priceRangeSlider;
    private LinearLayout amenitiesContainer;
    private final List<String> tempSelectedAmenities = new ArrayList<>();

    private static final List<AmenityItem> AMENITY_ITEMS = Arrays.asList(
        new AmenityItem("wifi", "WiFi"),
        new AmenityItem("airConditioning", "Air Conditioning"),
        new AmenityItem("television", "Television"),
        new AmenityItem("roomService", "Room Service"),
        new AmenityItem("nonSmoking", "Non-Smoking"),
        new AmenityItem("wheelchairAccessible", "Wheelchair Accessible"),
        new AmenityItem("balcony", "Balcony"),
        new AmenityItem("oceanView", "Ocean View"),
        new AmenityItem("kingBed", "King Bed"),
        new AmenityItem("coffeeMaker", "Coffee Maker"),
        new AmenityItem("miniBar", "Mini Bar"),
        new AmenityItem("safe", "Safe"),
        new AmenityItem("jacuzzi", "Jacuzzi")
    );

    public RoomFilterDialog(Context context, double minPrice, double maxPrice, 
                           List<String> selectedAmenities, OnFilterAppliedListener listener) {
        this.context = context;
        this.currentMinPrice = minPrice;
        this.currentMaxPrice = maxPrice;
        this.currentSelectedAmenities = new ArrayList<>(selectedAmenities);
        this.listener = listener;
    }

    public void show() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_room_filter, null);
        
        initViews(dialogView);
        setupPriceRange();
        setupAmenities();

        Dialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Filter Rooms")
                .setView(dialogView)
                .setPositiveButton("Apply", (d, which) -> applyFilters())
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Clear All", (d, which) -> clearAllFilters())
                .create();

        dialog.show();
    }

    private void initViews(View view) {
        priceRangeSlider = view.findViewById(R.id.priceRangeSlider);
        amenitiesContainer = view.findViewById(R.id.amenitiesContainer);
    }

    private void setupPriceRange() {
        priceRangeSlider.setValueFrom(0f);
        priceRangeSlider.setValueTo(1000f);
        priceRangeSlider.setStepSize(10f);
        
        // Set current values
        float minVal = currentMinPrice > 0 ? (float) currentMinPrice : 0f;
        float maxVal = currentMaxPrice < Double.MAX_VALUE ? (float) currentMaxPrice : 1000f;
        priceRangeSlider.setValues(minVal, maxVal);
    }

    private void setupAmenities() {
        tempSelectedAmenities.clear();
        tempSelectedAmenities.addAll(currentSelectedAmenities);

        for (AmenityItem amenity : AMENITY_ITEMS) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(amenity.displayName);
            checkBox.setChecked(currentSelectedAmenities.contains(amenity.key));
            checkBox.setTextSize(14f);
            checkBox.setPadding(16, 8, 16, 8);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!tempSelectedAmenities.contains(amenity.key)) {
                        tempSelectedAmenities.add(amenity.key);
                    }
                } else {
                    tempSelectedAmenities.remove(amenity.key);
                }
            });

            amenitiesContainer.addView(checkBox);
        }
    }

    private void applyFilters() {
        List<Float> values = priceRangeSlider.getValues();
        double minPrice = values.get(0).doubleValue();
        double maxPrice = values.get(1).doubleValue();
        
        // Convert max price back to Double.MAX_VALUE if it's at the slider maximum
        if (maxPrice >= 1000) {
            maxPrice = Double.MAX_VALUE;
        }

        if (listener != null) {
            listener.onFilterApplied(minPrice, maxPrice, new ArrayList<>(tempSelectedAmenities));
        }
    }

    private void clearAllFilters() {
        if (listener != null) {
            listener.onFilterApplied(0, Double.MAX_VALUE, new ArrayList<>());
        }
    }

    private static class AmenityItem {
        final String key;
        final String displayName;

        AmenityItem(String key, String displayName) {
            this.key = key;
            this.displayName = displayName;
        }
    }
}
