package com.example.luxevista;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.adapters.PromotionsGridAdapter;
import com.example.luxevista.models.Promotion;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PromotionsGridFragment extends Fragment implements PromotionsGridAdapter.OnPromotionClickListener {

    private static final String TAG = "PromotionsGridFragment";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private com.google.android.material.textfield.TextInputEditText etSearch;
    private com.google.android.material.chip.ChipGroup chipGroupCategory;
    private com.google.android.material.chip.ChipGroup chipGroupDiscount;
    private PromotionsGridAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotions_grid, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recyclerPromotionsGrid);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        chipGroupDiscount = view.findViewById(R.id.chipGroupDiscount);
        db = FirebaseFirestore.getInstance();

        toolbar.setTitle("Promotions");
        toolbar.setNavigationOnClickListener(v -> { if (getActivity() != null) getActivity().onBackPressed(); });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PromotionsGridAdapter(this);
        recyclerView.setAdapter(adapter);

        loadPromotions();
        wireFilters();
        return view;
    }

    private void loadPromotions() {
        db.collection("promotions")
                .get()
                .addOnSuccessListener(snap -> {
                    List<Promotion> all = new ArrayList<>();
                    Date now = new Date();
                    master.clear();
                    for (DocumentSnapshot doc : snap) {
                        try {
                            // Build Promotion manually to avoid type mismatches
                            Promotion p = new Promotion();
                            p.setPromotionId(doc.getString("promotionId"));
                            p.setTitle(doc.getString("title"));
                            p.setDescription(doc.getString("description"));
                            String imageUrl = doc.getString("imageUrl");
                            if (imageUrl == null) imageUrl = doc.getString("imageURL");
                            if (imageUrl != null) p.setImageUrl(imageUrl);
                            p.setPromoCode(doc.getString("promoCode"));
                            Object discountRaw = doc.get("discountPercent");
                            if (discountRaw instanceof Number) {
                                p.setDiscountPercent(((Number) discountRaw).intValue());
                            } else if (discountRaw instanceof String) {
                                try { p.setDiscountPercent(Integer.parseInt((String) discountRaw)); } catch (Exception ignored) {}
                            }
                            Object targetRaw = doc.get("target");
                            if (targetRaw instanceof java.util.Map) {
                                //noinspection unchecked
                                p.setTarget((java.util.Map<String, Object>) targetRaw);
                            }

                            Object startRaw = doc.get("startAt");
                            Object endRaw = doc.get("endAt");
                            Date startDate = parseDateFlexible(startRaw);
                            Date endDate = parseDateFlexible(endRaw);

                            boolean isActive = false;
                            if (startDate != null && endDate != null) {
                                isActive = !now.before(startDate) && !now.after(endDate);
                            }
                            if (isActive) { all.add(p); master.add(p); }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing promotion", e);
                        }
                    }
                    adapter.update(all);
                    // re-apply filters after data load
                    applyFilters();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load promotions", e));
    }

    private final List<Promotion> master = new ArrayList<>();

    private void wireFilters() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
        if (chipGroupCategory != null) chipGroupCategory.setOnCheckedChangeListener((g, id) -> applyFilters());
        if (chipGroupDiscount != null) chipGroupDiscount.setOnCheckedChangeListener((g, id) -> applyFilters());
    }

    private void applyFilters() {
        String q = etSearch != null && etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase() : "";
        int catId = chipGroupCategory != null ? chipGroupCategory.getCheckedChipId() : View.NO_ID;
        int disId = chipGroupDiscount != null ? chipGroupDiscount.getCheckedChipId() : View.NO_ID;

        List<Promotion> filtered = new ArrayList<>();
        for (Promotion p : master) {
            if (!matchesSearch(p, q)) continue;
            if (!matchesCategory(p, catId)) continue;
            if (!matchesDiscount(p, disId)) continue;
            filtered.add(p);
        }
        adapter.update(filtered);
    }

    private boolean matchesSearch(Promotion p, String q) {
        if (q.isEmpty()) return true;
        String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
        String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
        String code = p.getPromoCode() != null ? p.getPromoCode().toLowerCase() : "";
        return title.contains(q) || desc.contains(q) || code.contains(q);
    }

    private boolean matchesCategory(Promotion p, int checkedId) {
        if (checkedId == R.id.chipAll || checkedId == View.NO_ID) return true;
        if (checkedId == R.id.chipRooms) {
            return p.getTarget() != null && p.getTarget().containsKey("roomTypes");
        }
        if (checkedId == R.id.chipServices) {
            return p.getTarget() != null && p.getTarget().containsKey("serviceCategories");
        }
        return true;
    }

    private boolean matchesDiscount(Promotion p, int checkedId) {
        int dis = p.getDiscountPercent();
        if (checkedId == R.id.chipAnyDiscount || checkedId == View.NO_ID) return true;
        if (checkedId == R.id.chipDis10) return dis >= 10;
        if (checkedId == R.id.chipDis20) return dis >= 20;
        if (checkedId == R.id.chipDis30) return dis >= 30;
        return true;
    }

    private Date parseDateFlexible(Object raw) {
        if (raw == null) return null;
        if (raw instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) raw).toDate();
        }
        if (raw instanceof String) {
            String s = (String) raw;
            String[] patterns = new String[] {
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
            };
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return sdf.parse(s);
                } catch (ParseException ignored) {}
            }
        }
        return null;
    }

    @Override
    public void onPromotionClick(Promotion promotion) {
        // Optional: open details activity
    }
}


