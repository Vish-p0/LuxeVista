package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.adapters.ServiceAdapter;
import com.example.luxevista.models.Service;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ServicesFragment extends Fragment implements ServiceAdapter.OnServiceClickListener {

    private static final String TAG = "ServicesFragment";

    // Views
    private ImageView btnSearch, btnFilter, btnClearSearch;
    private LinearLayout searchContainer;
    private EditText etSearch;
    private ChipGroup chipGroupFilters;
    private RecyclerView recyclerServices;
    private LinearLayout loadingLayout, contentLayout, emptyStateLayout;

    // Firebase
    private FirebaseFirestore db;

    // Data and Adapter
    private ServiceAdapter serviceAdapter;
    private List<Service> allServices = new ArrayList<>();

    // Filter state
    private String currentSearchQuery = "";
    private double minPrice = 0;
    private double maxPrice = Double.MAX_VALUE;
    private List<String> selectedCategories = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new Fade());
        setExitTransition(new Fade());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_services, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearchFunctionality();
        setupClickListeners();
        loadServices();

        return view;
    }

    private void initViews(View view) {
        btnSearch = view.findViewById(R.id.btnSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        searchContainer = view.findViewById(R.id.searchContainer);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        recyclerServices = view.findViewById(R.id.recyclerServices);
        loadingLayout = view.findViewById(R.id.loadingLayout);
        contentLayout = view.findViewById(R.id.contentLayout);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        serviceAdapter = new ServiceAdapter(this);
        recyclerServices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerServices.setAdapter(serviceAdapter);
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
                
                // Show/hide clear button
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentSearchQuery = "";
            applyFilters();
        });
    }

    private void setupClickListeners() {
        btnSearch.setOnClickListener(v -> toggleSearchBar());
        
        btnFilter.setOnClickListener(v -> {
            // Show filter dialog
            showFilterDialog();
        });
    }

    private void toggleSearchBar() {
        if (searchContainer.getVisibility() == View.VISIBLE) {
            searchContainer.setVisibility(View.GONE);
            etSearch.setText("");
        } else {
            searchContainer.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        }
    }

    private void showFilterDialog() {
        // Create and show filter dialog
        ServiceFilterDialog dialog = new ServiceFilterDialog(
            getContext(),
            minPrice,
            maxPrice,
            selectedCategories,
            getAllCategories(),
            (newMinPrice, newMaxPrice, newSelectedCategories) -> {
                minPrice = newMinPrice;
                maxPrice = newMaxPrice;
                selectedCategories = newSelectedCategories;
                applyFilters();
                updateFilterChips();
            }
        );
        dialog.show();
    }

    private List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        for (Service service : allServices) {
            if (service.getCategory() != null && !categories.contains(service.getCategory())) {
                categories.add(service.getCategory());
            }
        }
        return categories;
    }

    private void applyFilters() {
        if (serviceAdapter != null) {
            serviceAdapter.filter(currentSearchQuery, minPrice, maxPrice, selectedCategories);
            updateEmptyState();
        }
    }

    private void updateFilterChips() {
        chipGroupFilters.removeAllViews();
        
        boolean hasFilters = false;

        // Add price range chip if set
        if (minPrice > 0 || maxPrice < Double.MAX_VALUE) {
            String priceText = "$" + (int)minPrice + " - $" + (int)maxPrice;
            addFilterChip("Price: " + priceText, () -> {
                minPrice = 0;
                maxPrice = Double.MAX_VALUE;
                applyFilters();
                updateFilterChips();
            });
            hasFilters = true;
        }

        // Add category chips
        for (String category : selectedCategories) {
            addFilterChip(category, () -> {
                selectedCategories.remove(category);
                applyFilters();
                updateFilterChips();
            });
            hasFilters = true;
        }

        chipGroupFilters.setVisibility(hasFilters ? View.VISIBLE : View.GONE);
    }

    private void addFilterChip(String text, Runnable onCloseClick) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.chip_background);
        chip.setOnCloseIconClickListener(v -> onCloseClick.run());
        chipGroupFilters.addView(chip);
    }

    private void loadServices() {
        showLoading(true);

        db.collection("services")
                .orderBy("price", Query.Direction.ASCENDING)
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
                    
                    serviceAdapter.updateServices(allServices);
                    applyFilters();
                    showLoading(false);
                    Log.d(TAG, "Loaded " + allServices.size() + " services");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading services", e);
                    showLoading(false);
                    showEmptyState(true);
                });
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        boolean isEmpty = serviceAdapter.getItemCount() == 0;
        if (isEmpty && !allServices.isEmpty()) {
            // Show empty state for filtered results
            showEmptyState(true);
        } else if (isEmpty && allServices.isEmpty()) {
            // Show empty state for no data
            showEmptyState(true);
        } else {
            // Show content
            showEmptyState(false);
        }
    }

    @Override
    public void onServiceClick(Service service) {
        // Navigate to service details
        Intent intent = new Intent(getContext(), ServiceDetailsActivity.class);
        intent.putExtra("serviceId", service.getServiceId());
        intent.putExtra("serviceName", service.getName());
        intent.putExtra("serviceCategory", service.getCategory());
        intent.putExtra("price", service.getPrice());
        intent.putExtra("currency", service.getCurrency());
        intent.putExtra("description", service.getDescription());
        intent.putExtra("durationMinutes", service.getDurationMinutes());
        
        // Convert image URLs to array
        if (service.getImageUrls() != null) {
            String[] imageUrls = service.getImageUrls().toArray(new String[0]);
            intent.putExtra("imageUrls", imageUrls);
        }
        
        startActivity(intent);
    }

    // View-only; no Book Now handler anymore
}



