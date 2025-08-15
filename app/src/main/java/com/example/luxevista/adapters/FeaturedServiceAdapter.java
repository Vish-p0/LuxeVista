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
import com.example.luxevista.models.Service;

import java.util.ArrayList;
import java.util.List;

public class FeaturedServiceAdapter extends RecyclerView.Adapter<FeaturedServiceAdapter.FeaturedServiceViewHolder> {

    private List<Service> services;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public FeaturedServiceAdapter(OnServiceClickListener listener) {
        this.services = new ArrayList<>();
        this.listener = listener;
    }

    public void updateServices(List<Service> newServices) {
        this.services.clear();
        this.services.addAll(newServices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_service, parent, false);
        return new FeaturedServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service, listener);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class FeaturedServiceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivServiceImage;
        private final TextView tvServiceCategory;
        private final TextView tvServicePrice;
        private final TextView tvServiceName;
        private final TextView tvServiceDuration;

        public FeaturedServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.ivServiceImage);
            tvServiceCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDuration = itemView.findViewById(R.id.tvServiceDuration);
        }

        public void bind(Service service, OnServiceClickListener listener) {
            // Load service image
            ImageUtils.loadImageWithFallback(ivServiceImage, service.getFirstImageUrl());

            // Set service details
            tvServiceName.setText(service.getName() != null ? service.getName() : "Service");
            tvServiceCategory.setText(service.getCategory() != null ? service.getCategory() : "Service");
            tvServicePrice.setText(service.getFormattedPrice());
            tvServiceDuration.setText(service.getFormattedDuration());

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServiceClick(service);
                }
            });
        }
    }
}
