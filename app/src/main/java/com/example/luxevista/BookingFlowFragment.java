package com.example.luxevista;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.BookingRoomsAdapter;
import com.example.luxevista.BookingServicesAdapter;
import com.example.luxevista.models.Room;
import com.example.luxevista.models.Service;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.luxevista.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingFlowFragment extends Fragment {

    private TextView tvCheckIn, tvCheckOut;
    private Button btnSelectDates, btnConfirm;
    private RecyclerView recyclerRooms, recyclerServices;
    private LinearLayout selectedItemsSection, selectedRoomsContainer, selectedServicesContainer;
    private LinearLayout selectedRoomsList, selectedServicesList;
    private TextView tvEstimatedTotal;

    private BookingRoomsAdapter roomsAdapter;
    private BookingServicesAdapter servicesAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Date checkInDate;
    private Date checkOutDate;

    private final SimpleDateFormat apiDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Now that the view is created, check for pre-filled data
        // Use a small delay to ensure everything is properly initialized
        view.post(() -> {
            checkForPrefilledData();
        });
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_flow, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews(view);
        setupRecyclerViews();
        setupClickListeners();
        
        // Don't check for pre-filled data here, wait for onViewCreated
        // Only show placeholders initially
        togglePlaceholders(true);
        
        updateSelectedItemsSection();

        return view;
    }
    
    private void checkForPrefilledData() {
        BookingCart cart = BookingCart.getInstance();
        Log.d("BookingFlow", "Checking for pre-filled data. Cart has dates: " + (cart.checkIn != null && cart.checkOut != null));
        Log.d("BookingFlow", "Cart has rooms: " + cart.roomSelections.size() + ", services: " + cart.serviceSelections.size());
        
        // Check if we have pre-filled dates
        if (cart.checkIn != null && cart.checkOut != null) {
            checkInDate = cart.checkIn.toDate();
            checkOutDate = cart.checkOut.toDate();
            Log.d("BookingFlow", "Setting pre-filled dates: " + checkInDate + " to " + checkOutDate);
            
            // Update date display
            tvCheckIn.setText("Check-in: " + DateFormat.format("MMM d, yyyy 2:00 PM", checkInDate));
            tvCheckOut.setText("Check-out: " + DateFormat.format("MMM d, yyyy 11:00 AM", checkOutDate));
            
            // Use post to ensure view is fully initialized before toggling placeholders
            getView().post(() -> {
                // Immediately hide placeholders and show lists for the selected dates
                togglePlaceholders(false);
                
                // Reload lists for the selected dates
                reloadListsForDates();
            });
        }
        
        // Check if we have pre-filled items
        if (!cart.roomSelections.isEmpty() || !cart.serviceSelections.isEmpty()) {
            Log.d("BookingFlow", "Updating selected items section for pre-filled items");
            // Update the selected items section
            updateSelectedItemsSection();
            
            // Update adapters to show selected items
            updateAdaptersForPrefilledItems();
        }
    }
    
    private void updateAdaptersForPrefilledItems() {
        BookingCart cart = BookingCart.getInstance();
        
        // Update room adapter selections
        for (String roomId : cart.roomSelections.keySet()) {
            roomsAdapter.setSelectedRoom(roomId);
        }
        
        // Update service adapter selections
        for (BookingCart.ServiceSelection serviceSel : cart.serviceSelections) {
            servicesAdapter.setSelectedService(serviceSel.serviceId);
        }
    }

    private void initViews(View view) {
        tvCheckIn = view.findViewById(R.id.tvCheckIn);
        tvCheckOut = view.findViewById(R.id.tvCheckOut);
        btnSelectDates = view.findViewById(R.id.btnSelectDates);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        recyclerRooms = view.findViewById(R.id.recyclerRooms);
        recyclerServices = view.findViewById(R.id.recyclerServices);
        
        // Selected items section
        selectedItemsSection = view.findViewById(R.id.selectedItemsSection);
        selectedRoomsContainer = view.findViewById(R.id.selectedRoomsContainer);
        selectedServicesContainer = view.findViewById(R.id.selectedServicesContainer);
        selectedRoomsList = view.findViewById(R.id.selectedRoomsList);
        selectedServicesList = view.findViewById(R.id.selectedServicesList);
        tvEstimatedTotal = view.findViewById(R.id.tvEstimatedTotal);

        // Back button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Navigate back to home
            androidx.navigation.NavController nav = androidx.navigation.Navigation.findNavController(requireView());
            nav.navigateUp();
        });
    }

    private void setupRecyclerViews() {
        recyclerRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerServices.setLayoutManager(new GridLayoutManager(getContext(), 2));

        roomsAdapter = new BookingRoomsAdapter(room -> {
            List<String> nights = getNightsInclusive();
            int maxQty = getMinRemaining(room, nights);
            showRoomDetailsDialog(room, Math.max(0, maxQty));
        });
        recyclerRooms.setAdapter(roomsAdapter);

        servicesAdapter = new BookingServicesAdapter(
            new BookingServicesAdapter.OnServiceSelectedListener() {
                @Override
                public void onServiceSelected(Service service, boolean selected) {
                    // Handle service selection if needed
                }
            },
            new BookingServicesAdapter.OnServiceClickListener() {
                @Override
                public void onServiceClick(Service service) {
                    List<String> nights = getNightsInclusive();
                    int maxQty = getMinRemaining(service, nights);
                    showServiceDetailsDialog(service, Math.max(0, maxQty));
                }
            }
        );
        recyclerServices.setAdapter(servicesAdapter);
    }

    private void setupClickListeners() {
        btnSelectDates.setOnClickListener(v -> openDateRangePicker());
        btnConfirm.setOnClickListener(v -> goToConfirmation());
    }

    private void openDateRangePicker() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select stay dates");
        builder.setSelection(new androidx.core.util.Pair<>(today, today + 86_400_000L));
        builder.setCalendarConstraints(constraints.build());
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(range -> {
            if (range.first == null || range.second == null) return;
            Calendar calIn = Calendar.getInstance();
            calIn.setTimeInMillis(range.first);
            calIn.set(Calendar.HOUR_OF_DAY, 14); // 2 PM check-in
            calIn.set(Calendar.MINUTE, 0);
            calIn.set(Calendar.SECOND, 0);
            checkInDate = calIn.getTime();

            Calendar calOut = Calendar.getInstance();
            calOut.setTimeInMillis(range.second);
            calOut.set(Calendar.HOUR_OF_DAY, 11); // 11 AM checkout per requirement
            calOut.set(Calendar.MINUTE, 0);
            calOut.set(Calendar.SECOND, 0);
            checkOutDate = calOut.getTime();

            tvCheckIn.setText("Check-in: " + DateFormat.format("MMM d, yyyy 2:00 PM", checkInDate));
            tvCheckOut.setText("Check-out: " + DateFormat.format("MMM d, yyyy 11:00 AM", checkOutDate));
            reloadListsForDates();
        });
        picker.show(getParentFragmentManager(), "date_range");
    }

    private List<String> getNightsInclusive() {
        List<String> days = new ArrayList<>();
        if (checkInDate == null || checkOutDate == null) return days;

        Calendar c = Calendar.getInstance();
        c.setTime(checkInDate);
        while (c.getTime().before(checkOutDate)) { // exclude checkout date
            days.add(apiDate.format(c.getTime()));
            c.add(Calendar.DATE, 1);
        }
        return days;
    }

    private int getMinRemaining(Room room, List<String> nights) {
        int minRemaining = Integer.MAX_VALUE;
        for (String d : nights) minRemaining = Math.min(minRemaining, room.getRemainingForDate(d));
        if (minRemaining == Integer.MAX_VALUE) minRemaining = 0;
        return minRemaining;
    }

    private int getMinRemaining(Service service, List<String> nights) {
        int minRemaining = Integer.MAX_VALUE;
        for (String d : nights) minRemaining = Math.min(minRemaining, service.getRemainingForDate(d));
        if (minRemaining == Integer.MAX_VALUE) minRemaining = 0;
        return minRemaining;
    }

    private void reloadListsForDates() {
        List<String> nights = getNightsInclusive();
        Log.d("BookingFlow", "Reloading lists for " + nights.size() + " nights: " + nights);
        
        roomsAdapter.setNightsKeys(nights);
        servicesAdapter.setNightsKeys(nights);
        togglePlaceholders(false);

        db.collection("rooms").whereEqualTo("visible", true).get().addOnSuccessListener(snap -> {
            List<Room> list = new ArrayList<>();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Room r = doc.toObject(Room.class);
                if (r == null) continue;
                boolean ok = true;
                for (String d : nights) { if (r.getRemainingForDate(d) <= 0) { ok = false; break; } }
                if (ok) list.add(r);
            }
            Log.d("BookingFlow", "Found " + list.size() + " available rooms for selected dates");
            roomsAdapter.setRooms(list);
        });

        db.collection("services").get().addOnSuccessListener(snap -> {
            List<Service> list = new ArrayList<>();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Service s = doc.toObject(Service.class);
                if (s == null) continue;
                boolean ok = true;
                for (String d : nights) { if (s.getRemainingForDate(d) <= 0) { ok = false; break; } }
                if (ok) list.add(s);
            }
            Log.d("BookingFlow", "Found " + list.size() + " available services for selected dates");
            servicesAdapter.setServices(list);
        });
    }

    private void togglePlaceholders(boolean showPlaceholders) {
        View root = getView();
        if (root == null) {
            Log.w("BookingFlow", "View is null in togglePlaceholders, cannot update visibility");
            return;
        }
        
        TextView phRooms = root.findViewById(R.id.placeholderRooms);
        TextView phServices = root.findViewById(R.id.placeholderServices);
        RecyclerView rvRooms = root.findViewById(R.id.recyclerRooms);
        RecyclerView rvServices = root.findViewById(R.id.recyclerServices);
        
        Log.d("BookingFlow", "Toggling placeholders. Show: " + showPlaceholders);
        Log.d("BookingFlow", "phRooms: " + (phRooms != null) + ", phServices: " + (phServices != null));
        Log.d("BookingFlow", "rvRooms: " + (rvRooms != null) + ", rvServices: " + (rvServices != null));
        
        int phVis = showPlaceholders ? View.VISIBLE : View.GONE;
        int listVis = showPlaceholders ? View.GONE : View.VISIBLE;
        
        if (phRooms != null) {
            phRooms.setVisibility(phVis);
            Log.d("BookingFlow", "Set phRooms visibility to: " + (showPlaceholders ? "VISIBLE" : "GONE"));
        }
        if (phServices != null) {
            phServices.setVisibility(phVis);
            Log.d("BookingFlow", "Set phServices visibility to: " + (showPlaceholders ? "VISIBLE" : "GONE"));
        }
        if (rvRooms != null) {
            rvRooms.setVisibility(listVis);
            Log.d("BookingFlow", "Set rvRooms visibility to: " + (showPlaceholders ? "GONE" : "VISIBLE"));
        }
        if (rvServices != null) {
            rvServices.setVisibility(listVis);
            Log.d("BookingFlow", "Set rvServices visibility to: " + (showPlaceholders ? "GONE" : "VISIBLE"));
        }
    }

    private void showRoomDetailsDialog(Room room, int maxQty) {
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(getContext());
        View content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_room_details, null, false);
        
        // Setup single image
        ImageView ivImage = content.findViewById(R.id.ivRoomImage);
        if (room.getImageUrls() != null && !room.getImageUrls().isEmpty()) {
            ImageUtils.loadImageWithFallback(ivImage, room.getImageUrls().get(0));
        }
        
        // Set room details
        TextView tvName = content.findViewById(R.id.tvRoomName);
        TextView tvType = content.findViewById(R.id.tvRoomType);
        TextView tvDesc = content.findViewById(R.id.tvDescription);
        TextView tvPrice = content.findViewById(R.id.tvPrice);
        TextView tvMaxGuests = content.findViewById(R.id.tvMaxGuests);
        LinearLayout layoutAmenities = content.findViewById(R.id.layoutAmenities);
        
        tvName.setText(room.getName());
        tvType.setText(room.getType());
        tvDesc.setText(room.getDescription());
        tvPrice.setText(String.format(Locale.US, "$%.2f/night", room.getPricePerNight()));
        tvMaxGuests.setText("Max " + room.getMaxGuests() + " guests");
        
        // Display amenities
        displayRoomAmenities(layoutAmenities, room);
        
        // Setup quantity spinner
        Spinner spnQty = content.findViewById(R.id.spnQuantity);
        List<Integer> options = new ArrayList<>();
        for (int i = 1; i <= Math.max(0, maxQty); i++) options.add(i);
        if (options.isEmpty()) options.add(0);
        spnQty.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, options));
        
        // Setup apply button
        content.findViewById(R.id.btnApply).setOnClickListener(v -> {
            int qty = (Integer) spnQty.getSelectedItem();
            if (qty > 0) {
                BookingCart.getInstance().roomSelections.put(room.getRoomId(), new BookingCart.RoomSelection(
                        room.getRoomId(), room.getName(), room.getPricePerNight(), qty
                ));
                
                // Update the adapter to show the selected room
                roomsAdapter.setSelectedRoom(room.getRoomId());
                
                Snackbar.make(requireView(), "Added " + room.getName() + " to booking", Snackbar.LENGTH_SHORT).show();
                updateSelectedItemsSection();
            }
            ((android.app.AlertDialog) v.getTag()).dismiss();
        });
        
        android.app.AlertDialog d = b.setView(content).create();
        content.findViewById(R.id.btnApply).setTag(d);
        d.show();
    }

    private void showServiceDetailsDialog(Service service, int maxQty) {
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(getContext());
        View content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_service_details, null, false);
        
        // Setup single image
        ImageView ivImage = content.findViewById(R.id.ivServiceImage);
        if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
            ImageUtils.loadImageWithFallback(ivImage, service.getImageUrls().get(0));
        }
        
        // Set service details
        TextView tvName = content.findViewById(R.id.tvServiceName);
        TextView tvCategory = content.findViewById(R.id.tvServiceCategory);
        TextView tvDesc = content.findViewById(R.id.tvDescription);
        TextView tvDuration = content.findViewById(R.id.tvDuration);
        TextView tvPrice = content.findViewById(R.id.tvPrice);
        
        tvName.setText(service.getName());
        tvCategory.setText(service.getCategory());
        tvDesc.setText(service.getDescription());
        tvDuration.setText(service.getFormattedDuration());
        tvPrice.setText(String.format(Locale.US, "$%.2f", service.getPrice()));
        
        // Setup quantity and time spinners
        Spinner spnQty = content.findViewById(R.id.spnQuantity);
        Spinner spnTime = content.findViewById(R.id.spnTimeSlot);
        
        List<Integer> qtyOptions = new ArrayList<>();
        for (int i = 1; i <= Math.max(0, maxQty); i++) qtyOptions.add(i);
        if (qtyOptions.isEmpty()) qtyOptions.add(0);
        spnQty.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, qtyOptions));
        
        // Get available time slots for the selected date
        List<String> availableTimeSlots = new ArrayList<>();
        if (checkInDate != null) {
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(checkInDate);
            availableTimeSlots = service.getAvailableTimesForDate(dateKey);
        }
        
        // If no specific time slots available, use default hours
        if (availableTimeSlots.isEmpty()) {
            for (int h = 9; h <= 20; h++) {
                availableTimeSlots.add(String.format(Locale.US, "%02d:00", h));
            }
        }
        
        spnTime.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, availableTimeSlots));
        
        // Setup apply button
        content.findViewById(R.id.btnApply).setOnClickListener(v -> {
            int qty = (Integer) spnQty.getSelectedItem();
            String time = (String) spnTime.getSelectedItem();
            if (qty > 0 && checkInDate != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(checkInDate);
                String[] hh = time.split(":");
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hh[0]));
                c.set(Calendar.MINUTE, 0);
                BookingCart.getInstance().serviceSelections.add(new BookingCart.ServiceSelection(
                        service.getServiceId(), service.getName(), service.getPrice(), qty, new Timestamp(c.getTime())
                ));
                
                // Update the adapter to show the selected service
                servicesAdapter.setSelectedService(service.getServiceId());
                
                Snackbar.make(requireView(), "Added " + service.getName() + " to booking", Snackbar.LENGTH_SHORT).show();
                updateSelectedItemsSection();
            }
            ((android.app.AlertDialog) v.getTag()).dismiss();
        });
        
        android.app.AlertDialog d = b.setView(content).create();
        content.findViewById(R.id.btnApply).setTag(d);
        d.show();
    }

    private void displayRoomAmenities(LinearLayout container, Room room) {
        container.removeAllViews();
        if (room.getAmenities() == null) return;
        
        String[] amenityKeys = {"wifi", "airConditioning", "television", "roomService", "nonSmoking", 
                               "wheelchairAccessible", "balcony", "oceanView", "kingBed", "coffeeMaker", 
                               "miniBar", "safe", "jacuzzi"};
        String[] amenityNames = {"WiFi", "Air Conditioning", "TV", "Room Service", "Non-Smoking", 
                                "Wheelchair Accessible", "Balcony", "Ocean View", "King Bed", "Coffee Maker", 
                                "Mini Bar", "Safe", "Jacuzzi"};
        
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        
        int itemsPerRow = 2;
        int count = 0;
        
        for (int i = 0; i < amenityKeys.length; i++) {
            if (room.hasAmenity(amenityKeys[i])) {
                if (count > 0 && count % itemsPerRow == 0) {
                    container.addView(row);
                    row = new LinearLayout(getContext());
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
                
                TextView amenity = new TextView(getContext());
                amenity.setText("• " + amenityNames[i]);
                amenity.setTextColor(getResources().getColor(R.color.text_primary));
                amenity.setTextSize(12);
                amenity.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                row.addView(amenity);
                count++;
            }
        }
        
        if (count > 0) {
            container.addView(row);
        }
    }

    private void updateSelectedItemsSection() {
        BookingCart cart = BookingCart.getInstance();
        boolean hasRooms = !cart.roomSelections.isEmpty();
        boolean hasServices = !cart.serviceSelections.isEmpty();
        
        if (hasRooms || hasServices) {
            selectedItemsSection.setVisibility(View.VISIBLE);
            
            // Update rooms section
            if (hasRooms) {
                selectedRoomsContainer.setVisibility(View.VISIBLE);
                updateSelectedRoomsList();
            } else {
                selectedRoomsContainer.setVisibility(View.GONE);
            }
            
            // Update services section
            if (hasServices) {
                selectedServicesContainer.setVisibility(View.VISIBLE);
                updateSelectedServicesList();
            } else {
                selectedServicesContainer.setVisibility(View.GONE);
            }
            
            // Update total
            updateEstimatedTotal();
        } else {
            selectedItemsSection.setVisibility(View.GONE);
        }
    }

    private void updateSelectedRoomsList() {
        selectedRoomsList.removeAllViews();
        BookingCart cart = BookingCart.getInstance();
        
        for (BookingCart.RoomSelection roomSel : cart.roomSelections.values()) {
            View roomItem = createRoomItemView(roomSel);
            selectedRoomsList.addView(roomItem);
        }
    }

    private void updateSelectedServicesList() {
        selectedServicesList.removeAllViews();
        BookingCart cart = BookingCart.getInstance();
        
        for (BookingCart.ServiceSelection serviceSel : cart.serviceSelections) {
            View serviceItem = createServiceItemView(serviceSel);
            selectedServicesList.addView(serviceItem);
        }
    }

    private View createRoomItemView(BookingCart.RoomSelection roomSel) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_selected_item, null);
        
        // Set room icon with proper tint
        ImageView ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
        ivItemIcon.setImageResource(R.drawable.ic_rooms);
        ivItemIcon.setColorFilter(getResources().getColor(R.color.dark_blue_primary, null));
        
        // Set item name and details
        TextView tvItemName = itemView.findViewById(R.id.tvItemName);
        TextView tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
        
        int nights = getNumberOfNights();
        double subtotal = roomSel.quantity * roomSel.pricePerNight * nights;
        
        tvItemName.setText(roomSel.name + " × " + roomSel.quantity + " (" + nights + " nights)");
        tvItemPrice.setText(String.format(Locale.US, "$%.2f", subtotal));
        
        // Setup remove button
        Button btnRemove = itemView.findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(v -> {
            BookingCart.getInstance().roomSelections.remove(roomSel.roomId);
            // Clear the room selection in the adapter
            roomsAdapter.clearSelection();
            updateSelectedItemsSection();
            Snackbar.make(requireView(), "Removed " + roomSel.name, Snackbar.LENGTH_SHORT).show();
        });
        
        return itemView;
    }

    private View createServiceItemView(BookingCart.ServiceSelection serviceSel) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_selected_item, null);
        
        // Set service icon with proper tint
        ImageView ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
        ivItemIcon.setImageResource(R.drawable.ic_services);
        ivItemIcon.setColorFilter(getResources().getColor(R.color.dark_blue_primary, null));
        
        // Set item name and details
        TextView tvItemName = itemView.findViewById(R.id.tvItemName);
        TextView tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
        
        double subtotal = serviceSel.quantity * serviceSel.price;
        String timeStr = DateFormat.format("MMM d, h:mm a", serviceSel.scheduledAt.toDate()).toString();
        
        tvItemName.setText(serviceSel.name + " × " + serviceSel.quantity + " @ " + timeStr);
        tvItemPrice.setText(String.format(Locale.US, "$%.2f", subtotal));
        
        // Setup remove button
        Button btnRemove = itemView.findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(v -> {
            BookingCart.getInstance().serviceSelections.remove(serviceSel);
            // Clear the service selection in the adapter
            servicesAdapter.clearSelection();
            updateSelectedItemsSection();
            Snackbar.make(requireView(), "Removed " + serviceSel.name, Snackbar.LENGTH_SHORT).show();
        });
        
        return itemView;
    }

    private void updateEstimatedTotal() {
        BookingCart cart = BookingCart.getInstance();
        double total = 0.0;
        
        // Calculate rooms total
        int nights = getNumberOfNights();
        for (BookingCart.RoomSelection roomSel : cart.roomSelections.values()) {
            total += roomSel.quantity * roomSel.pricePerNight * nights;
        }
        
        // Calculate services total
        for (BookingCart.ServiceSelection serviceSel : cart.serviceSelections) {
            total += serviceSel.quantity * serviceSel.price;
        }
        
        tvEstimatedTotal.setText(String.format(Locale.US, "$%.2f", total));
    }

    private int getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        Calendar c = Calendar.getInstance();
        c.setTime(checkInDate);
        int nights = 0;
        while (c.getTime().before(checkOutDate)) {
            nights++;
            c.add(Calendar.DATE, 1);
        }
        return nights;
    }

    private void goToConfirmation() {
        if (checkInDate == null || checkOutDate == null) {
            Snackbar.make(requireView(), "Select dates first", Snackbar.LENGTH_LONG).show();
            return;
        }
        
        BookingCart cart = BookingCart.getInstance();
        
        // Update cart with current dates if they've changed
        cart.checkIn = new Timestamp(checkInDate);
        cart.checkOut = new Timestamp(checkOutDate);
        
        // Check if we have any selections
        if (cart.roomSelections.isEmpty() && cart.serviceSelections.isEmpty()) {
            Snackbar.make(requireView(), "Please select at least one room or service", Snackbar.LENGTH_LONG).show();
            return;
        }
        
        // Navigate to confirmation
        androidx.navigation.NavController nav = androidx.navigation.Navigation.findNavController(requireView());
        nav.navigate(R.id.action_bookingFlow_to_confirmation);
    }
}


