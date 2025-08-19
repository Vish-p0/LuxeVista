package com.example.luxevista;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.ui.AppBarConfiguration;

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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Avoid reloading the current fragment on reselect
            bottomNav.setOnItemReselectedListener(item -> { /* no-op */ });

            // Ensure no active indicator or background shape is shown
            try {
                bottomNav.setItemActiveIndicatorEnabled(false);
            } catch (Throwable ignored) { }
            bottomNav.setItemRippleColor(null);
            bottomNav.setItemBackground(new ColorDrawable(Color.TRANSPARENT));
        }

        // Configure top-level destinations to avoid showing Up button
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.roomsFragment,
                R.id.servicesFragment,
                R.id.bookingsFragment,
                R.id.profileFragment
        ).build();
        
        // Note: ActionBar setup removed as this app doesn't use an ActionBar
        // The navigation will still work properly with the bottom navigation
        
        // Handle fragment navigation from extras (e.g., from detail activities)
        handleFragmentNavigation(navController);
    }
    
    private void handleFragmentNavigation(NavController navController) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String fragment = extras.getString("fragment");
            if (fragment != null) {
                switch (fragment) {
                    case "rooms":
                        navController.navigate(R.id.roomsFragment);
                        break;
                    case "services":
                        navController.navigate(R.id.servicesFragment);
                        break;
                    case "bookings":
                        navController.navigate(R.id.bookingsFragment);
                        break;
                    case "profile":
                        navController.navigate(R.id.profileFragment);
                        break;
                    case "booking":
                        navController.navigate(R.id.bookingFlowFragment);
                        break;
                }
            }
        }
    }
}