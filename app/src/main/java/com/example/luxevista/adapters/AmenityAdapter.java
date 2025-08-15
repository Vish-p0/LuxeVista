package com.example.luxevista.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.R;

import java.util.ArrayList;
import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder> {

    private List<String> amenities;

    public AmenityAdapter() {
        this.amenities = new ArrayList<>();
    }

    public void updateAmenities(List<String> newAmenities) {
        this.amenities.clear();
        this.amenities.addAll(newAmenities);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AmenityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_amenity, parent, false);
        return new AmenityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityViewHolder holder, int position) {
        String amenityKey = amenities.get(position);
        holder.bind(amenityKey);
    }

    @Override
    public int getItemCount() {
        return amenities.size();
    }

    static class AmenityViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAmenityIcon;
        private final TextView tvAmenityName;

        public AmenityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAmenityIcon = itemView.findViewById(R.id.ivAmenityIcon);
            tvAmenityName = itemView.findViewById(R.id.tvAmenityName);
        }

        public void bind(String amenityKey) {
            tvAmenityName.setText(getAmenityDisplayName(amenityKey));
            
            // Set the appropriate icon based on amenity key
            int iconResId = getAmenityIconResource(amenityKey);
            ivAmenityIcon.setImageResource(iconResId);
        }

        private String getAmenityDisplayName(String amenityKey) {
            switch (amenityKey) {
                case "wifi": return "Wi-Fi";
                case "airConditioning": return "AC";
                case "television": return "TV";
                case "roomService": return "Room Service";
                case "nonSmoking": return "Non-Smoking";
                case "wheelchairAccessible": return "Accessible";
                case "balcony": return "Balcony";
                case "oceanView": return "Ocean View";
                case "kingBed": return "King Bed";
                case "coffeeMaker": return "Coffee";
                case "miniBar": return "Mini Bar";
                case "safe": return "Safe";
                case "jacuzzi": return "Jacuzzi";
                default: return amenityKey;
            }
        }

        private int getAmenityIconResource(String amenityKey) {
            switch (amenityKey) {
                case "wifi": return R.drawable.amenities_wifi;
                case "airConditioning": return R.drawable.amenities_air_conditioning;
                case "television": return R.drawable.amenities_television;
                case "roomService": return R.drawable.amenities_room_service;
                case "nonSmoking": return R.drawable.amenities_non_smoking;
                case "wheelchairAccessible": return R.drawable.amenities_wheelchair_accessible;
                case "balcony": return R.drawable.amenities_balcony;
                case "oceanView": return R.drawable.amenities_ocean_view;
                case "kingBed": return R.drawable.amenities_king_bed;
                case "coffeeMaker": return R.drawable.amenities_coffee_maker;
                case "miniBar": return R.drawable.amenities_mini_bar;
                case "safe": return R.drawable.amenities_safe;
                case "jacuzzi": return R.drawable.amenities_jacuzzi;
                default: return R.drawable.ic_image_placeholder;
            }
        }
    }
}
