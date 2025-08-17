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

public class PromotionsGridAdapter extends RecyclerView.Adapter<PromotionsGridAdapter.ViewHolder> {

    public interface OnPromotionClickListener { void onPromotionClick(Promotion promotion); }

    private final List<Promotion> items = new ArrayList<>();
    private final OnPromotionClickListener listener;

    public PromotionsGridAdapter(OnPromotionClickListener listener) { this.listener = listener; }

    public void update(List<Promotion> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvDiscount;
        private final TextView tvPromoCode;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvPromoCode = itemView.findViewById(R.id.tvPromoCode);
        }

        void bind(Promotion p, OnPromotionClickListener listener) {
            ImageUtils.loadImageWithFallback(ivImage, p.getImageUrl());
            tvTitle.setText(p.getTitle());
            tvDescription.setText(p.getDescription());
            tvPromoCode.setText(p.getPromoCode() != null ? ("CODE: " + p.getPromoCode()) : "");
            tvDiscount.setText(p.getDiscountPercent() > 0 ? (p.getDiscountPercent() + "% OFF") : "");
            itemView.setOnClickListener(v -> { if (listener != null) listener.onPromotionClick(p); });
        }
    }
}


