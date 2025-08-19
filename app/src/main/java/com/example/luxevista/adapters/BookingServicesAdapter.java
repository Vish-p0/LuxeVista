package com.example.luxevista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.models.Service;
import com.example.luxevista.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingServicesAdapter extends RecyclerView.Adapter<BookingServicesAdapter.ServiceViewHolder> {

    private List<Service> services = new ArrayList<>();
    private List<String> nightsKeys = new ArrayList<>();
    private OnServiceSelectedListener selectedListener;
    private OnServiceClickListener clickListener;
    private int selectedPosition = -1;

    public interface OnServiceSelectedListener {
        void onServiceSelected(Service service, boolean selected);
    }

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public BookingServicesAdapter(OnServiceSelectedListener selectedListener, OnServiceClickListener clickListener) {
        this.selectedListener = selectedListener;
        this.clickListener = clickListener;
    }

    public void setServices(List<Service> services) {
        this.services = services;
        selectedPosition = -1; // Reset selection when services change
        notifyDataSetChanged();
    }

    public void setNightsKeys(List<String> nightsKeys) {
        this.nightsKeys = nightsKeys;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedService(String serviceId) {
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).getServiceId().equals(serviceId)) {
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_service_card, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
    private ImageView ivServiceImage;
    private TextView tvServiceName, tvPrice, tvDuration, tvAvailable, tvAddedQuantity, tvCategory, tvDescription;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.ivServiceImage);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
            tvAddedQuantity = itemView.findViewById(R.id.tvAddedQuantity);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    if (clickListener != null) {
                        clickListener.onServiceClick(services.get(position));
                    }
                }
            });
        }

        public void bind(Service service, boolean isSelected) {
            // Set service image (first image only)
            if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
                ImageUtils.loadImageWithFallback(ivServiceImage, service.getImageUrls().get(0));
            }

            tvServiceName.setText(service.getName());
            tvPrice.setText(String.format("$%.2f", service.getPrice()));
            tvDuration.setText(service.getFormattedDuration());
            if (tvCategory != null) tvCategory.setText(service.getCategory() != null ? service.getCategory() : "");
            if (tvDescription != null) tvDescription.setText(service.getDescription() != null ? service.getDescription() : "");

            // Calculate and show availability: any-day max helps users see potential slots
            int anyDayMax = 0;
            for (String date : nightsKeys) {
                int available = service.getRemainingForDate(date);
                anyDayMax = Math.max(anyDayMax, available);
            }
            tvAvailable.setText(anyDayMax + " slots");

            // Show added quantity if any
            int addedQty = BookingCart.getInstance().getServiceQuantity(service.getServiceId());
            if (addedQty > 0) {
                tvAddedQuantity.setText("Added: " + addedQty);
                tvAddedQuantity.setVisibility(View.VISIBLE);
            } else {
                tvAddedQuantity.setVisibility(View.GONE);
            }

            // Highlight selected service using MaterialCardView stroke
            com.google.android.material.card.MaterialCardView cardView = (com.google.android.material.card.MaterialCardView) itemView;
            if (isSelected) {
                cardView.setStrokeWidth(6);
                cardView.setStrokeColor(itemView.getContext().getResources().getColorStateList(R.color.dark_blue_primary, null));
            } else {
                cardView.setStrokeWidth(0);
            }
        }
    }
}


