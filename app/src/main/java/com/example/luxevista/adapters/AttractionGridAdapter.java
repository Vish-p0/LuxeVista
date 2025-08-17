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

public class AttractionGridAdapter extends RecyclerView.Adapter<AttractionGridAdapter.AttractionViewHolder> {

    private List<Attraction> attractions = new ArrayList<>();
    private OnAttractionClickListener listener;

    public interface OnAttractionClickListener {
        void onAttractionClick(Attraction attraction);
    }

    public AttractionGridAdapter(OnAttractionClickListener listener) {
        this.listener = listener;
    }

    public void updateAttractions(List<Attraction> newAttractions) {
        this.attractions.clear();
        if (newAttractions != null) this.attractions.addAll(newAttractions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttractionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attraction_grid, parent, false);
        return new AttractionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttractionViewHolder holder, int position) {
        holder.bind(attractions.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return attractions.size();
    }

    static class AttractionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView tvName;
        private final TextView tvDistance;

        public AttractionViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }

        public void bind(Attraction attraction, OnAttractionClickListener listener) {
            ImageUtils.loadImageWithFallback(imageView, attraction.getFirstImageUrl());
            tvName.setText(attraction.getName() != null ? attraction.getName() : "Attraction");
            tvDistance.setText(attraction.getFormattedDistance());
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAttractionClick(attraction);
            });
        }
    }
}


