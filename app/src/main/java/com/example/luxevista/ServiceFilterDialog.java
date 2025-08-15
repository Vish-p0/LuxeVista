package com.example.luxevista;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.List;

public class ServiceFilterDialog {

    public interface OnFilterAppliedListener {
        void onFilterApplied(double minPrice, double maxPrice, List<String> selectedCategories);
    }

    private final Context context;
    private double currentMinPrice;
    private double currentMaxPrice;
    private List<String> currentSelectedCategories;
    private final List<String> availableCategories;
    private final OnFilterAppliedListener listener;

    private RangeSlider priceRangeSlider;
    private LinearLayout categoriesContainer;
    private final List<String> tempSelectedCategories = new ArrayList<>();

    public ServiceFilterDialog(Context context, double minPrice, double maxPrice, 
                              List<String> selectedCategories, List<String> availableCategories,
                              OnFilterAppliedListener listener) {
        this.context = context;
        this.currentMinPrice = minPrice;
        this.currentMaxPrice = maxPrice;
        this.currentSelectedCategories = new ArrayList<>(selectedCategories);
        this.availableCategories = availableCategories;
        this.listener = listener;
    }

    public void show() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_service_filter, null);
        
        initViews(dialogView);
        setupPriceRange();
        setupCategories();

        Dialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Filter Services")
                .setView(dialogView)
                .setPositiveButton("Apply", (d, which) -> applyFilters())
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Clear All", (d, which) -> clearAllFilters())
                .create();

        dialog.show();
    }

    private void initViews(View view) {
        priceRangeSlider = view.findViewById(R.id.priceRangeSlider);
        categoriesContainer = view.findViewById(R.id.categoriesContainer);
    }

    private void setupPriceRange() {
        priceRangeSlider.setValueFrom(0f);
        priceRangeSlider.setValueTo(500f);
        priceRangeSlider.setStepSize(5f);
        
        // Set current values
        float minVal = currentMinPrice > 0 ? (float) currentMinPrice : 0f;
        float maxVal = currentMaxPrice < Double.MAX_VALUE ? (float) currentMaxPrice : 500f;
        priceRangeSlider.setValues(minVal, maxVal);
    }

    private void setupCategories() {
        tempSelectedCategories.clear();
        tempSelectedCategories.addAll(currentSelectedCategories);

        for (String category : availableCategories) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(category);
            checkBox.setChecked(currentSelectedCategories.contains(category));
            checkBox.setTextSize(14f);
            checkBox.setPadding(16, 8, 16, 8);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!tempSelectedCategories.contains(category)) {
                        tempSelectedCategories.add(category);
                    }
                } else {
                    tempSelectedCategories.remove(category);
                }
            });

            categoriesContainer.addView(checkBox);
        }
    }

    private void applyFilters() {
        List<Float> values = priceRangeSlider.getValues();
        double minPrice = values.get(0).doubleValue();
        double maxPrice = values.get(1).doubleValue();
        
        // Convert max price back to Double.MAX_VALUE if it's at the slider maximum
        if (maxPrice >= 500) {
            maxPrice = Double.MAX_VALUE;
        }

        if (listener != null) {
            listener.onFilterApplied(minPrice, maxPrice, new ArrayList<>(tempSelectedCategories));
        }
    }

    private void clearAllFilters() {
        if (listener != null) {
            listener.onFilterApplied(0, Double.MAX_VALUE, new ArrayList<>());
        }
    }
}
