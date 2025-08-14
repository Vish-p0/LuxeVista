package com.example.luxevista;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return;
        }
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Optional: switch selected icon drawable when checked
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.homeFragment) {
                item.setIcon(R.drawable.ic_home_selected);
            } else if (itemId == R.id.roomsFragment) {
                item.setIcon(R.drawable.ic_rooms_selected);
            } else if (itemId == R.id.servicesFragment) {
                item.setIcon(R.drawable.ic_service_selected);
            } else if (itemId == R.id.bookingsFragment) {
                item.setIcon(R.drawable.ic_bookings_selected);
            } else if (itemId == R.id.profileFragment) {
                item.setIcon(R.drawable.ic_profile_selected);
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        bottomNavigationView.setOnItemReselectedListener(item -> {
            // no-op: avoid fragment reloads on reselect
        });

        // Ensure unselected icons reset appropriately when destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
            if (bnv == null) return;
            // Reset all to default icons (unselected)
            bnv.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home);
            bnv.getMenu().findItem(R.id.roomsFragment).setIcon(R.drawable.ic_rooms);
            bnv.getMenu().findItem(R.id.servicesFragment).setIcon(R.drawable.ic_services);
            bnv.getMenu().findItem(R.id.bookingsFragment).setIcon(R.drawable.ic_bookings);
            bnv.getMenu().findItem(R.id.profileFragment).setIcon(R.drawable.ic_profile);

            // Set selected icon
            int destId = destination.getId();
            if (destId == R.id.homeFragment) {
                bnv.getMenu().findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_selected);
            } else if (destId == R.id.roomsFragment) {
                bnv.getMenu().findItem(R.id.roomsFragment).setIcon(R.drawable.ic_rooms_selected);
            } else if (destId == R.id.servicesFragment) {
                bnv.getMenu().findItem(R.id.servicesFragment).setIcon(R.drawable.ic_service_selected);
            } else if (destId == R.id.bookingsFragment) {
                bnv.getMenu().findItem(R.id.bookingsFragment).setIcon(R.drawable.ic_bookings_selected);
            } else if (destId == R.id.profileFragment) {
                bnv.getMenu().findItem(R.id.profileFragment).setIcon(R.drawable.ic_profile_selected);
            }
        });
    }
}