package com.example.luxevista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.adapters.AttractionGridAdapter;
import com.example.luxevista.models.Attraction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AttractionsFragment extends Fragment implements AttractionGridAdapter.OnAttractionClickListener {

    private static final String TAG = "AttractionsFragment";

    private MaterialToolbar toolbar;
    private FrameLayout loadingOverlay;
    private RecyclerView recyclerView;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupDistance;
    private TextView tvEmptyState;

    private FirebaseFirestore db;
    private AttractionGridAdapter adapter;

    private List<Attraction> allAttractions = new ArrayList<>();
    private List<Attraction> filteredAttractions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attractions, container, false);

        db = FirebaseFirestore.getInstance();

        toolbar = view.findViewById(R.id.toolbar);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        recyclerView = view.findViewById(R.id.recyclerAttractionsGrid);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroupDistance = view.findViewById(R.id.chipGroupDistance);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupToolbar();
        setupRecycler();
        setupSearchAndFilters();
        loadAttractions();

        return view;
    }

    private void setupToolbar() {
        toolbar.setTitle("Attractions");
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void setupRecycler() {
        adapter = new AttractionGridAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroupDistance.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private void loadAttractions() {
        showLoading(true);
        db.collection("attractions")
                .whereEqualTo("visible", true)
                .get()
                .addOnSuccessListener(snap -> {
                    allAttractions.clear();
                    for (DocumentSnapshot doc : snap) {
                        try {
                            Attraction a = doc.toObject(Attraction.class);
                            if (a != null && a.isVisible()) {
                                allAttractions.add(a);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing attraction doc: " + doc.getId(), e);
                        }
                    }
                    allAttractions.sort((a1, a2) -> Double.compare(a1.getDistanceKM(), a2.getDistanceKM()));
                    applyFilters();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load attractions", e);
                    showLoading(false);
                });
    }

    private void applyFilters() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.getDefault()) : "";
        int checkedId = chipGroupDistance.getCheckedChipId();

        filteredAttractions.clear();
        for (Attraction a : allAttractions) {
            if (!matchesSearch(a, query)) continue;
            if (!matchesDistance(a, checkedId)) continue;
            filteredAttractions.add(a);
        }

        tvEmptyState.setVisibility(filteredAttractions.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.updateAttractions(filteredAttractions);
    }

    private boolean matchesSearch(Attraction a, String query) {
        if (query.isEmpty()) return true;
        String name = a.getName() != null ? a.getName().toLowerCase(Locale.getDefault()) : "";
        String desc = a.getDescription() != null ? a.getDescription().toLowerCase(Locale.getDefault()) : "";
        return name.contains(query) || desc.contains(query);
    }

    private boolean matchesDistance(Attraction a, int checkedId) {
        double d = a.getDistanceKM();
        if (checkedId == View.NO_ID) return true; // All
        if (checkedId == R.id.chipUnder1) {
            return d < 1.0;
        } else if (checkedId == R.id.chip1to3) {
            return d >= 1.0 && d <= 3.0;
        } else if (checkedId == R.id.chip3to5) {
            return d > 3.0 && d <= 5.0;
        } else if (checkedId == R.id.chipOver5) {
            return d > 5.0;
        }
        return true;
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAttractionClick(Attraction attraction) {
        if (getContext() == null) return;
        android.content.Intent intent = new android.content.Intent(getContext(), AttractionDetailsActivity.class);
        intent.putExtra("name", attraction.getName());
        intent.putExtra("description", attraction.getDescription());
        intent.putExtra("distanceKM", attraction.getDistanceKM());
        if (attraction.getImageUrls() != null) {
            intent.putExtra("imageUrls", attraction.getImageUrls().toArray(new String[0]));
        }
        startActivity(intent);
    }
}


