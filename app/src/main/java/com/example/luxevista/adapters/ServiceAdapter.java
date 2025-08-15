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

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services = new ArrayList<>();
    private List<Service> filteredServices = new ArrayList<>();
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public ServiceAdapter(OnServiceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_card, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = filteredServices.get(position);
        holder.bind(service, listener);
    }

    @Override
    public int getItemCount() {
        return filteredServices.size();
    }

    public void updateServices(List<Service> newServices) {
        this.services.clear();
        this.services.addAll(newServices);
        this.filteredServices.clear();
        this.filteredServices.addAll(newServices);
        notifyDataSetChanged();
    }

    public void filter(String query, double minPrice, double maxPrice, List<String> selectedCategories) {
        filteredServices.clear();

        for (Service service : services) {
            boolean matchesSearch = service.matchesSearchQuery(query);
            boolean matchesPrice = service.matchesPriceRange(minPrice, maxPrice);
            boolean matchesCategory = service.matchesCategory(selectedCategories);

            if (matchesSearch && matchesPrice && matchesCategory) {
                filteredServices.add(service);
            }
        }

        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivServiceImage;
        private final TextView tvServiceCategory;
        private final TextView tvServicePrice;
        private final TextView tvServiceName;
        private final TextView tvServiceDuration;
        private final TextView tvServiceDescription;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.ivServiceImage);
            tvServiceCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDuration = itemView.findViewById(R.id.tvServiceDuration);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
        }

        public void bind(Service service, OnServiceClickListener listener) {
            // Set service image with fallback
            ImageUtils.loadImageWithFallback(ivServiceImage, service.getFirstImageUrl());

            // Set service details
            tvServiceCategory.setText(service.getCategory() != null ? service.getCategory() : "Service");
            tvServicePrice.setText(service.getFormattedPrice());
            tvServiceName.setText(service.getName() != null ? service.getName() : "Service");
            tvServiceDuration.setText(service.getFormattedDuration());
            tvServiceDescription.setText(service.getDescription() != null ? service.getDescription() : "");

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServiceClick(service);
                }
            });
        }
    }
}
