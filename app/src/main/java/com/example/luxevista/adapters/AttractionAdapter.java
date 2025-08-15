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
import com.example.luxevista.models.Attraction;

import java.util.ArrayList;
import java.util.List;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.AttractionViewHolder> {

    private List<Attraction> attractions;
    private OnAttractionClickListener listener;

    public interface OnAttractionClickListener {
        void onAttractionClick(Attraction attraction);
    }

    public AttractionAdapter(OnAttractionClickListener listener) {
        this.attractions = new ArrayList<>();
        this.listener = listener;
    }

    public void updateAttractions(List<Attraction> newAttractions) {
        this.attractions.clear();
        this.attractions.addAll(newAttractions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttractionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attraction, parent, false);
        return new AttractionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttractionViewHolder holder, int position) {
        Attraction attraction = attractions.get(position);
        holder.bind(attraction, listener);
    }

    @Override
    public int getItemCount() {
        return attractions.size();
    }

    static class AttractionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAttractionImage;
        private final TextView tvAttractionDistance;
        private final TextView tvAttractionName;
        private final TextView tvAttractionDescription;

        public AttractionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAttractionImage = itemView.findViewById(R.id.ivAttractionImage);
            tvAttractionDistance = itemView.findViewById(R.id.tvAttractionDistance);
            tvAttractionName = itemView.findViewById(R.id.tvAttractionName);
            tvAttractionDescription = itemView.findViewById(R.id.tvAttractionDescription);
        }

        public void bind(Attraction attraction, OnAttractionClickListener listener) {
            // Load attraction image
            ImageUtils.loadImageWithFallback(ivAttractionImage, attraction.getFirstImageUrl());

            // Set attraction details
            tvAttractionName.setText(attraction.getName() != null ? attraction.getName() : "Attraction");
            tvAttractionDescription.setText(attraction.getShortDescription());
            tvAttractionDistance.setText(attraction.getFormattedDistance());

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAttractionClick(attraction);
                }
            });
        }
    }
}
