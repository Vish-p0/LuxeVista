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
import com.example.luxevista.models.Room;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FeaturedRoomAdapter extends RecyclerView.Adapter<FeaturedRoomAdapter.FeaturedRoomViewHolder> {

    private List<Room> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public FeaturedRoomAdapter(OnRoomClickListener listener) {
        this.rooms = new ArrayList<>();
        this.listener = listener;
    }

    public void updateRooms(List<Room> newRooms) {
        this.rooms.clear();
        this.rooms.addAll(newRooms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_room, parent, false);
        return new FeaturedRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedRoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room, listener);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class FeaturedRoomViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivRoomImage;
        private final TextView tvRoomType;
        private final TextView tvRoomPrice;
        private final TextView tvRoomName;
        private final ImageView ivAmenity1;
        private final ImageView ivAmenity2;
        private final ImageView ivAmenity3;
        private final TextView tvMoreAmenities;

        public FeaturedRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            ivAmenity1 = itemView.findViewById(R.id.ivAmenity1);
            ivAmenity2 = itemView.findViewById(R.id.ivAmenity2);
            ivAmenity3 = itemView.findViewById(R.id.ivAmenity3);
            tvMoreAmenities = itemView.findViewById(R.id.tvMoreAmenities);
        }

        public void bind(Room room, OnRoomClickListener listener) {
            // Load room image
            ImageUtils.loadImageWithFallback(ivRoomImage, room.getFirstImageUrl());

            // Set room details
            tvRoomName.setText(room.getName() != null ? room.getName() : "Room");
            tvRoomType.setText(room.getType() != null ? room.getType() : "Standard");
            
            // Format price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedPrice = currencyFormat.format(room.getPricePerNight());
            tvRoomPrice.setText(formattedPrice);

            // Display key amenities
            displayAmenities(room.getAmenities());

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(room);
                }
            });
        }

        private void displayAmenities(Map<String, Boolean> amenities) {
            // Reset visibility
            ivAmenity1.setVisibility(View.GONE);
            ivAmenity2.setVisibility(View.GONE);
            ivAmenity3.setVisibility(View.GONE);
            tvMoreAmenities.setVisibility(View.GONE);

            if (amenities == null) return;

            // Key amenities to display in order of priority
            String[] keyAmenities = {"wifi", "balcony", "oceanView", "airConditioning", "television", "roomService"};
            ImageView[] amenityViews = {ivAmenity1, ivAmenity2, ivAmenity3};
            
            int displayedCount = 0;
            int totalCount = 0;

            // Count total true amenities
            for (Boolean value : amenities.values()) {
                if (Boolean.TRUE.equals(value)) {
                    totalCount++;
                }
            }

            // Display up to 3 key amenities
            for (String amenityKey : keyAmenities) {
                if (displayedCount >= 3) break;
                
                if (Boolean.TRUE.equals(amenities.get(amenityKey))) {
                    ImageView amenityView = amenityViews[displayedCount];
                    int iconRes = getAmenityIconResource(amenityKey);
                    if (iconRes != 0) {
                        amenityView.setImageResource(iconRes);
                        amenityView.setVisibility(View.VISIBLE);
                        displayedCount++;
                    }
                }
            }

            // Show "more" text if there are additional amenities
            int remainingCount = totalCount - displayedCount;
            if (remainingCount > 0) {
                tvMoreAmenities.setText("+" + remainingCount + " more");
                tvMoreAmenities.setVisibility(View.VISIBLE);
            }
        }

        private int getAmenityIconResource(String amenityKey) {
            switch (amenityKey) {
                case "wifi": return R.drawable.amenities_wifi;
                case "balcony": return R.drawable.amenities_balcony;
                case "oceanView": return R.drawable.amenities_ocean_view;
                case "airConditioning": return R.drawable.amenities_air_conditioning;
                case "television": return R.drawable.amenities_television;
                case "roomService": return R.drawable.amenities_room_service;
                case "safe": return R.drawable.amenities_safe;
                case "miniBar": return R.drawable.amenities_mini_bar;
                case "kingBed": return R.drawable.amenities_king_bed;
                case "coffeeMaker": return R.drawable.amenities_coffee_maker;
                case "jacuzzi": return R.drawable.amenities_jacuzzi;
                case "nonSmoking": return R.drawable.amenities_non_smoking;
                case "wheelchairAccessible": return R.drawable.amenities_wheelchair_accessible;
                default: return 0;
            }
        }
    }
}
