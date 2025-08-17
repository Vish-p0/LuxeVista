package com.example.luxevista.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.ImageUtils;
import com.example.luxevista.R;
import com.example.luxevista.models.Promotion;

import java.util.ArrayList;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private List<Promotion> promotions;
    private OnPromotionClickListener listener;

    public interface OnPromotionClickListener {
        void onPromotionClick(Promotion promotion);
    }

    public PromotionAdapter(OnPromotionClickListener listener) {
        this.promotions = new ArrayList<>();
        this.listener = listener;
    }

    public void updatePromotions(List<Promotion> newPromotions) {
        this.promotions.clear();
        this.promotions.addAll(newPromotions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promotion = promotions.get(position);
        holder.bind(promotion, listener);
    }

    @Override
    public int getItemCount() { return promotions.size(); }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPromotionImage;
        private final TextView tvPromotionBadge;
        private final TextView tvPromotionTitle;
        private final TextView tvPromotionDescription;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPromotionImage = itemView.findViewById(R.id.ivPromotionImage);
            tvPromotionBadge = itemView.findViewById(R.id.tvPromotionBadge);
            tvPromotionTitle = itemView.findViewById(R.id.tvPromotionTitle);
            tvPromotionDescription = itemView.findViewById(R.id.tvPromotionDescription);
        }

        public void bind(Promotion promotion, OnPromotionClickListener listener) {
            // Load promotion image
            ImageUtils.loadImageWithFallback(ivPromotionImage, promotion.getImageUrl());

            // Set promotion details
            tvPromotionTitle.setText(promotion.getTitle() != null ? promotion.getTitle() : "Special Offer");
            tvPromotionDescription.setText(promotion.getDescription() != null ? promotion.getDescription() : "");

            // Show "Limited Time" badge for active promotions
            if (promotion.isActive()) {
                tvPromotionBadge.setVisibility(View.VISIBLE);
                tvPromotionBadge.setText("Limited Time");
            } else {
                tvPromotionBadge.setVisibility(View.GONE);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPromotionClick(promotion);
                }
            });
        }
    }

}
