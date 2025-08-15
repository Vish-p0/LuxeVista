package com.example.luxevista.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.ImageUtils;
import com.example.luxevista.R;
import com.example.luxevista.models.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> rooms = new ArrayList<>();
    private List<Room> filteredRooms = new ArrayList<>();
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public RoomAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_card, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = filteredRooms.get(position);
        holder.bind(room, listener);
    }

    @Override
    public int getItemCount() {
        return filteredRooms.size();
    }

    public void updateRooms(List<Room> newRooms) {
        this.rooms.clear();
        this.rooms.addAll(newRooms);
        this.filteredRooms.clear();
        this.filteredRooms.addAll(newRooms);
        notifyDataSetChanged();
    }

    public void filter(String query, double minPrice, double maxPrice, List<String> selectedAmenities) {
        filteredRooms.clear();

        for (Room room : rooms) {
            boolean matchesSearch = room.matchesSearchQuery(query);
            boolean matchesPrice = room.matchesPriceRange(minPrice, maxPrice);
            boolean matchesAmenities = selectedAmenities == null || selectedAmenities.isEmpty() || 
                                     room.hasAnyAmenity(selectedAmenities);

            if (matchesSearch && matchesPrice && matchesAmenities) {
                filteredRooms.add(room);
            }
        }

        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivRoomImage;
        private final TextView tvRoomPrice;
        private final TextView tvRoomType;
        private final TextView tvRoomName;
        private final TextView tvMaxGuests;
        private final TextView tvRoomDescription;
        private final LinearLayout layoutAmenities;
        private final TextView tvMoreAmenities;

        // Amenity icon views
        private final ImageView amenityWifi;
        private final ImageView amenityAirConditioning;
        private final ImageView amenityTelevision;
        private final ImageView amenityRoomService;
        private final ImageView amenityNonSmoking;
        private final ImageView amenityWheelchairAccessible;
        private final ImageView amenityBalcony;
        private final ImageView amenityOceanView;
        private final ImageView amenityKingBed;
        private final ImageView amenityCoffeeMaker;
        private final ImageView amenityMiniBar;
        private final ImageView amenitySafe;
        private final ImageView amenityJacuzzi;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvMaxGuests = itemView.findViewById(R.id.tvMaxGuests);
            tvRoomDescription = itemView.findViewById(R.id.tvRoomDescription);
            layoutAmenities = itemView.findViewById(R.id.layoutAmenities);
            tvMoreAmenities = itemView.findViewById(R.id.tvMoreAmenities);

            // Initialize amenity icon views
            amenityWifi = itemView.findViewById(R.id.amenityWifi);
            amenityAirConditioning = itemView.findViewById(R.id.amenityAirConditioning);
            amenityTelevision = itemView.findViewById(R.id.amenityTelevision);
            amenityRoomService = itemView.findViewById(R.id.amenityRoomService);
            amenityNonSmoking = itemView.findViewById(R.id.amenityNonSmoking);
            amenityWheelchairAccessible = itemView.findViewById(R.id.amenityWheelchairAccessible);
            amenityBalcony = itemView.findViewById(R.id.amenityBalcony);
            amenityOceanView = itemView.findViewById(R.id.amenityOceanView);
            amenityKingBed = itemView.findViewById(R.id.amenityKingBed);
            amenityCoffeeMaker = itemView.findViewById(R.id.amenityCoffeeMaker);
            amenityMiniBar = itemView.findViewById(R.id.amenityMiniBar);
            amenitySafe = itemView.findViewById(R.id.amenitySafe);
            amenityJacuzzi = itemView.findViewById(R.id.amenityJacuzzi);
        }

        public void bind(Room room, OnRoomClickListener listener) {
            // Set room image with fallback
            ImageUtils.loadImageWithFallback(ivRoomImage, room.getFirstImageUrl());

            // Set room details
            tvRoomPrice.setText(room.getFormattedPrice());
            tvRoomType.setText(room.getType() != null ? room.getType() : "");
            tvRoomName.setText(room.getName() != null ? room.getName() : "Room");
            tvMaxGuests.setText(String.valueOf(room.getMaxGuests()));
            tvRoomDescription.setText(room.getDescription() != null ? room.getDescription() : "");

            // Show/hide amenity icons based on room amenities
            showAmenityIcon(amenityWifi, room.hasAmenity("wifi"));
            showAmenityIcon(amenityAirConditioning, room.hasAmenity("airConditioning"));
            showAmenityIcon(amenityTelevision, room.hasAmenity("television"));
            showAmenityIcon(amenityRoomService, room.hasAmenity("roomService"));
            showAmenityIcon(amenityNonSmoking, room.hasAmenity("nonSmoking"));
            showAmenityIcon(amenityWheelchairAccessible, room.hasAmenity("wheelchairAccessible"));
            showAmenityIcon(amenityBalcony, room.hasAmenity("balcony"));
            showAmenityIcon(amenityOceanView, room.hasAmenity("oceanView"));
            showAmenityIcon(amenityKingBed, room.hasAmenity("kingBed"));
            showAmenityIcon(amenityCoffeeMaker, room.hasAmenity("coffeeMaker"));
            showAmenityIcon(amenityMiniBar, room.hasAmenity("miniBar"));
            showAmenityIcon(amenitySafe, room.hasAmenity("safe"));
            showAmenityIcon(amenityJacuzzi, room.hasAmenity("jacuzzi"));

            // Show count of additional amenities
            int totalAmenities = room.getAmenityCount();
            int visibleAmenities = getVisibleAmenitiesCount(room);
            int remainingAmenities = totalAmenities - visibleAmenities;

            if (remainingAmenities > 0) {
                tvMoreAmenities.setVisibility(View.VISIBLE);
                tvMoreAmenities.setText("+" + remainingAmenities);
            } else {
                tvMoreAmenities.setVisibility(View.GONE);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(room);
                }
            });
        }

        private void showAmenityIcon(ImageView icon, boolean show) {
            icon.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        private int getVisibleAmenitiesCount(Room room) {
            int count = 0;
            if (room.hasAmenity("wifi")) count++;
            if (room.hasAmenity("airConditioning")) count++;
            if (room.hasAmenity("television")) count++;
            if (room.hasAmenity("roomService")) count++;
            if (room.hasAmenity("nonSmoking")) count++;
            if (room.hasAmenity("wheelchairAccessible")) count++;
            if (room.hasAmenity("balcony")) count++;
            if (room.hasAmenity("oceanView")) count++;
            if (room.hasAmenity("kingBed")) count++;
            if (room.hasAmenity("coffeeMaker")) count++;
            if (room.hasAmenity("miniBar")) count++;
            if (room.hasAmenity("safe")) count++;
            if (room.hasAmenity("jacuzzi")) count++;
            return count;
        }
    }
}
