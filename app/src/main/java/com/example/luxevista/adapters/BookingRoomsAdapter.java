package com.example.luxevista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.models.Room;
import com.example.luxevista.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingRoomsAdapter extends RecyclerView.Adapter<BookingRoomsAdapter.RoomViewHolder> {

    private List<Room> rooms = new ArrayList<>();
    private List<String> nightsKeys = new ArrayList<>();
    private OnRoomClickListener listener;
    private int selectedPosition = -1;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public BookingRoomsAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
        selectedPosition = -1; // Reset selection when rooms change
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

    public void setSelectedRoom(String roomId) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId().equals(roomId)) {
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_room_card, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRoomImage;
        private TextView tvRoomName, tvPricePerNight, tvMaxGuests, tvAvailable, tvAddedQuantity;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvPricePerNight = itemView.findViewById(R.id.tvPricePerNight);
            tvMaxGuests = itemView.findViewById(R.id.tvMaxGuests);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
            tvAddedQuantity = itemView.findViewById(R.id.tvAddedQuantity);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onRoomClick(rooms.get(position));
                    }
                }
            });
        }

        public void bind(Room room, boolean isSelected) {
            // Set room image (first image only)
            if (room.getImageUrls() != null && !room.getImageUrls().isEmpty()) {
                ImageUtils.loadImageWithFallback(ivRoomImage, room.getImageUrls().get(0));
            }

            tvRoomName.setText(room.getName());
            tvPricePerNight.setText(String.format("$%.2f/night", room.getPricePerNight()));
            tvMaxGuests.setText("Max " + room.getMaxGuests() + " guests");

            // Calculate and show availability
            int minAvailable = Integer.MAX_VALUE;
            for (String date : nightsKeys) {
                int available = room.getRemainingForDate(date);
                minAvailable = Math.min(minAvailable, available);
            }
            if (minAvailable == Integer.MAX_VALUE) minAvailable = 0;
            tvAvailable.setText(minAvailable + " available");

            // Show added quantity if any
            int addedQty = BookingCart.getInstance().getRoomQuantity(room.getRoomId());
            if (addedQty > 0) {
                tvAddedQuantity.setText("Added: " + addedQty);
                tvAddedQuantity.setVisibility(View.VISIBLE);
            } else {
                tvAddedQuantity.setVisibility(View.GONE);
            }

            // Highlight selected room using MaterialCardView stroke
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


