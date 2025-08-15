package com.example.luxevista;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    
    private List<Booking> bookings = new ArrayList<>();
    private List<Booking> filteredBookings = new ArrayList<>();
    private OnBookingClickListener listener;
    
    // Date formatter for display
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
    
    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
        void onViewDetailsClick(Booking booking);
    }
    
    public BookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = filteredBookings.get(position);
        holder.bind(booking, listener);
    }
    
    @Override
    public int getItemCount() {
        return filteredBookings.size();
    }
    
    public void updateBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        this.filteredBookings.clear();
        this.filteredBookings.addAll(newBookings);
        notifyDataSetChanged();
    }
    
    public void filter(String query, String filterType) {
        filteredBookings.clear();
        
        for (Booking booking : bookings) {
            boolean matchesQuery = query.isEmpty() || 
                    booking.getBookingId().toLowerCase().contains(query.toLowerCase()) ||
                    booking.getType().toLowerCase().contains(query.toLowerCase()) ||
                    (booking.getItemName() != null && booking.getItemName().toLowerCase().contains(query.toLowerCase()));
            
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
        private final ImageView imageItem;
        private final TextView textType;
        private final TextView textStatus;
        private final TextView textBookingId;
        private final TextView textItemName;
        private final TextView textItemDescription;
        private final TextView textStartDate;
        private final TextView textEndDate;
        private final TextView textPrice;
        private final MaterialButton btnViewDetails;
        private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            iconType = itemView.findViewById(R.id.iconType);
            imageItem = itemView.findViewById(R.id.imageItem);
            textType = itemView.findViewById(R.id.textType);
            textStatus = itemView.findViewById(R.id.textStatus);
            textBookingId = itemView.findViewById(R.id.textBookingId);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemDescription = itemView.findViewById(R.id.textItemDescription);
            textStartDate = itemView.findViewById(R.id.textStartDate);
            textEndDate = itemView.findViewById(R.id.textEndDate);
            textPrice = itemView.findViewById(R.id.textPrice);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
        
        public void bind(Booking booking, OnBookingClickListener listener) {
            // Set type icon and text
            if ("room".equals(booking.getType())) {
                iconType.setImageResource(R.drawable.ic_rooms);
                textType.setText("Room");
            } else if ("service".equals(booking.getType())) {
                iconType.setImageResource(R.drawable.ic_services);
                textType.setText("Service");
            } else {
                iconType.setImageResource(R.drawable.ic_bookings);
                textType.setText(booking.getDisplayType());
            }
            
            // Set booking ID
            textBookingId.setText("Booking #" + booking.getBookingId());
            
            // Set item image with fallback using ImageUtils
            ImageUtils.loadItemImageWithFallback(imageItem, null, booking.getType());
            
            // Set item name and description
            String itemName = booking.getItemName();
            if (itemName == null || itemName.isEmpty()) {
                itemName = booking.getDisplayType() + " Booking";
            }
            textItemName.setText(itemName);
            
            // Set item description based on type
            String description = "room".equals(booking.getType()) ? 
                "Luxury accommodation with modern amenities" : 
                "Premium service experience";
            textItemDescription.setText(description);
            
            // Set dates
            Date startDate = booking.getStartDateAsDate();
            Date endDate = booking.getEndDateAsDate();
            
            if (startDate != null) {
                String startLabel = "room".equals(booking.getType()) ? "Check-in: " : "Start: ";
                textStartDate.setText(startLabel + dateTimeFormat.format(startDate));
            } else {
                textStartDate.setText("Start: Not specified");
            }
            
            if (endDate != null) {
                String endLabel = "room".equals(booking.getType()) ? "Check-out: " : "End: ";
                textEndDate.setText(endLabel + dateTimeFormat.format(endDate));
            } else {
                textEndDate.setText("End: Not specified");
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
