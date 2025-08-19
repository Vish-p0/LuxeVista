package com.example.luxevista;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewBookingAdapter extends RecyclerView.Adapter<NewBookingAdapter.BookingViewHolder> {
    
    private List<NewBooking> bookings = new ArrayList<>();
    private List<NewBooking> filteredBookings = new ArrayList<>();
    private OnBookingClickListener listener;
    
    public interface OnBookingClickListener {
        void onBookingClick(NewBooking booking);
        void onViewDetailsClick(NewBooking booking);
    }
    
    public NewBookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_new_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        NewBooking booking = filteredBookings.get(position);
        holder.bind(booking, listener);
    }
    
    @Override
    public int getItemCount() {
        return filteredBookings.size();
    }
    
    public void updateBookings(List<NewBooking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        this.filteredBookings.clear();
        this.filteredBookings.addAll(newBookings);
        notifyDataSetChanged();
    }
    
    public void filter(String query, String filterType) {
        filteredBookings.clear();
        
        for (NewBooking booking : bookings) {
            boolean matchesQuery = query.isEmpty() || 
                    booking.getDisplayName().toLowerCase().contains(query.toLowerCase()) ||
                    booking.getSummaryText().toLowerCase().contains(query.toLowerCase());
            
            boolean matchesFilter = filterType.equals("All") ||
                    (filterType.equals("Active") && booking.isActive()) ||
                    (filterType.equals("Completed") && booking.isCompleted()) ||
                    (filterType.equals("Cancelled") && booking.isCancelled());
            
            if (matchesQuery && matchesFilter) {
                filteredBookings.add(booking);
            }
        }
        
        notifyDataSetChanged();
    }
    
    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconType;
        private final TextView textRoomName;
        private final TextView textSummary;
        private final TextView textStatus;
        private final TextView textDates;
        private final TextView textPrice;
        private final Button btnViewDetails;
        
        // Static date formatters for ViewHolder
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            iconType = itemView.findViewById(R.id.iconType);
            textRoomName = itemView.findViewById(R.id.textRoomName);
            textSummary = itemView.findViewById(R.id.textSummary);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDates = itemView.findViewById(R.id.textDates);
            textPrice = itemView.findViewById(R.id.textPrice);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
        
        public void bind(NewBooking booking, OnBookingClickListener listener) {
            // Set display name (room names instead of booking ID)
            textRoomName.setText(booking.getDisplayName());
            
            // Set summary text (rooms + services count)
            textSummary.setText(booking.getSummaryText());
            
            // Set dates
            Date startDate = booking.getStartDateAsDate();
            Date endDate = booking.getEndDateAsDate();
            
            if (startDate != null && endDate != null) {
                String startStr = dateFormat.format(startDate) + " at " + timeFormat.format(startDate);
                String endStr = dateFormat.format(endDate) + " at " + timeFormat.format(endDate);
                textDates.setText("Check-in: " + startStr + "\nCheck-out: " + endStr);
            } else {
                textDates.setText("Dates not specified");
            }
            
            // Set price
            textPrice.setText(booking.getFormattedPrice());
            
            // Set status with color
            textStatus.setText(booking.getDisplayStatus());
            String statusColor = booking.getStatusColor();
            try {
                textStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(statusColor)));
            } catch (IllegalArgumentException e) {
                // Fallback to default color if parsing fails
                textStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
            }
            
            // Set status text color to white for better contrast
            textStatus.setTextColor(Color.WHITE);
            
            // Set icon based on booking type
            if (booking.getRooms() != null && !booking.getRooms().isEmpty()) {
                if (booking.getServices() != null && !booking.getServices().isEmpty()) {
                    // Both rooms and services
                    iconType.setImageResource(R.drawable.ic_bookings);
                } else {
                    // Only rooms
                    iconType.setImageResource(R.drawable.ic_rooms);
                }
            } else if (booking.getServices() != null && !booking.getServices().isEmpty()) {
                // Only services
                iconType.setImageResource(R.drawable.ic_services);
            } else {
                // Fallback
                iconType.setImageResource(R.drawable.ic_bookings);
            }
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onBookingClick(booking);
                    }
                }
            });
            
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onViewDetailsClick(booking);
                    }
                }
            });
        }
    }
}
