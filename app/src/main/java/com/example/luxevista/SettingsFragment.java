package com.example.luxevista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    // General Settings
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchBookingReminders;
    private SwitchMaterial switchPromotions;
    private SwitchMaterial switchUpdates;
    private MaterialAutoCompleteTextView spinnerLanguage;
    private SwitchMaterial switchDarkMode;
    
    // Security
    private MaterialCardView cardSecuritySettings;
    
    // Account Management
    private MaterialCardView cardChangePassword;
    private MaterialCardView cardDeleteAccount;
    
    // App Info
    private MaterialCardView cardAppVersion;
    private MaterialCardView cardTerms;
    private MaterialCardView cardPrivacy;
    private MaterialCardView cardSupport;

    private SharedPreferences preferences;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final String PREFS_NAME = "luxevista_settings";
    private static final String[] LANGUAGES = {"English", "French", "German", "Sinhala", "Tamil"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        initializeServices();
        initViews(view);
        setupLanguageSpinner();
        loadPreferences();
        setupClickListeners();
        
        return view;
    }

    private void initializeServices() {
        preferences = requireContext().getSharedPreferences(PREFS_NAME, 0);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        // General Settings
        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchBookingReminders = view.findViewById(R.id.switchBookingReminders);
        switchPromotions = view.findViewById(R.id.switchPromotions);
        switchUpdates = view.findViewById(R.id.switchUpdates);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        
        // Security
        cardSecuritySettings = view.findViewById(R.id.cardSecuritySettings);
        
        // Account Management
        cardChangePassword = view.findViewById(R.id.cardChangePassword);
        cardDeleteAccount = view.findViewById(R.id.cardDeleteAccount);
        
        // App Info
        cardAppVersion = view.findViewById(R.id.cardAppVersion);
        cardTerms = view.findViewById(R.id.cardTerms);
        cardPrivacy = view.findViewById(R.id.cardPrivacy);
        cardSupport = view.findViewById(R.id.cardSupport);
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            LANGUAGES
        );
        spinnerLanguage.setAdapter(adapter);
    }

    private void loadPreferences() {
        // Load notification preferences
        switchNotifications.setChecked(preferences.getBoolean("notifications_enabled", true));
        switchBookingReminders.setChecked(preferences.getBoolean("booking_reminders", true));
        switchPromotions.setChecked(preferences.getBoolean("promotions", true));
        switchUpdates.setChecked(preferences.getBoolean("updates", true));
        
        // Load language preference
        String savedLanguage = preferences.getString("language", "English");
        spinnerLanguage.setText(savedLanguage, false);
        
        // Load theme preference
        switchDarkMode.setChecked(preferences.getBoolean("dark_mode", false));
        
        // Update notification sub-options visibility
        updateNotificationSubOptions();
    }

    private void setupClickListeners() {
        // Notification switches
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            updateNotificationSubOptions();
        });
        
        switchBookingReminders.setOnCheckedChangeListener((buttonView, isChecked) -> 
            preferences.edit().putBoolean("booking_reminders", isChecked).apply());
            
        switchPromotions.setOnCheckedChangeListener((buttonView, isChecked) -> 
            preferences.edit().putBoolean("promotions", isChecked).apply());
            
        switchUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> 
            preferences.edit().putBoolean("updates", isChecked).apply());

        // Language selection
        spinnerLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLanguage = LANGUAGES[position];
            preferences.edit().putString("language", selectedLanguage).apply();
            Toast.makeText(getContext(), "Language updated to " + selectedLanguage, Toast.LENGTH_SHORT).show();
        });

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("dark_mode", isChecked).apply();
            Toast.makeText(getContext(), "Theme updated. Restart app to apply changes.", Toast.LENGTH_LONG).show();
        });

        // Security settings
        cardSecuritySettings.setOnClickListener(v -> openSecuritySettings());

        // Account management
        cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        cardDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // App info
        cardAppVersion.setOnClickListener(v -> showAppVersionInfo());
        cardTerms.setOnClickListener(v -> openUrl("https://luxevista.com/terms"));
        cardPrivacy.setOnClickListener(v -> openUrl("https://luxevista.com/privacy"));
        cardSupport.setOnClickListener(v -> openSupportEmail());
    }

    private void updateNotificationSubOptions() {
        boolean enabled = switchNotifications.isChecked();
        switchBookingReminders.setEnabled(enabled);
        switchPromotions.setEnabled(enabled);
        switchUpdates.setEnabled(enabled);
        
        // Visual feedback for disabled state
        float alpha = enabled ? 1.0f : 0.5f;
        switchBookingReminders.setAlpha(alpha);
        switchPromotions.setAlpha(alpha);
        switchUpdates.setAlpha(alpha);
    }

    private void openSecuritySettings() {
        Intent intent = new Intent(getContext(), SecuritySetupActivity.class);
        startActivity(intent);
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update Password", null)
                .setNegativeButton("Cancel", null)
                .create();
                
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                handlePasswordChange(dialogView, dialog);
            });
        });
        
        dialog.show();
    }

    private void handlePasswordChange(View dialogView, AlertDialog dialog) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), getString(R.string.not_signed_in), Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values from dialog
        String currentPassword = ((com.google.android.material.textfield.TextInputEditText) 
                dialogView.findViewById(R.id.etCurrentPassword)).getText().toString().trim();
        String newPassword = ((com.google.android.material.textfield.TextInputEditText) 
                dialogView.findViewById(R.id.etNewPassword)).getText().toString().trim();
        String confirmPassword = ((com.google.android.material.textfield.TextInputEditText) 
                dialogView.findViewById(R.id.etConfirmPassword)).getText().toString().trim();

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate user first
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Update password
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> 
                                Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone. All your data will be permanently removed.")
                .setPositiveButton("Delete", (dialog, which) -> showDeleteAccountPasswordDialog())
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void showDeleteAccountPasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_password, null);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Account Deletion")
                .setMessage("Enter your password to confirm account deletion:")
                .setView(dialogView)
                .setPositiveButton("Delete Account", null)
                .setNegativeButton("Cancel", null)
                .create();
                
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                handleAccountDeletion(dialogView, dialog);
            });
        });
        
        dialog.show();
    }

    private void handleAccountDeletion(View dialogView, AlertDialog dialog) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), getString(R.string.not_signed_in), Toast.LENGTH_SHORT).show();
            return;
        }

        String password = ((com.google.android.material.textfield.TextInputEditText) 
                dialogView.findViewById(R.id.etConfirmPassword)).getText().toString().trim();

        if (password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Delete user from Firestore first
                    firestore.collection("users").document(user.getUid())
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                // Delete Firebase Auth account
                                user.delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            // Navigate to login screen
                                            Intent intent = new Intent(getContext(), LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> 
                                            Toast.makeText(getContext(), "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            })
                            .addOnFailureListener(e -> 
                                Toast.makeText(getContext(), "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Password is incorrect", Toast.LENGTH_SHORT).show());
    }

    private void showAppVersionInfo() {
        String version = "1.0.0"; // BuildConfig.VERSION_NAME;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("App Information")
                .setMessage("LuxeVista\nVersion: " + version + "\n\nA luxury hotel booking experience.")
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_app_info)
                .show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSupportEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@luxevista.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "LuxeVista Support Request");
            intent.putExtra(Intent.EXTRA_TEXT, "Hello LuxeVista Support Team,\n\n");
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}
