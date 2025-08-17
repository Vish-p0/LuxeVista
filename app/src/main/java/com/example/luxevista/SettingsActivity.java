package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private SwitchMaterial switchPasscodeLock;
    private MaterialCardView cardPasscodeSettings;
    private TextView tvPasscodeDescription;

    private SecurityManager securityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        securityManager = new SecurityManager(this);

        initViews();
        setupClickListeners();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI when returning from security setup
        updateUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchPasscodeLock = findViewById(R.id.switchPasscodeLock);
        cardPasscodeSettings = findViewById(R.id.cardPasscodeSettings);
        tvPasscodeDescription = findViewById(R.id.tvPasscodeDescription);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        switchPasscodeLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !securityManager.hasPasscode()) {
                // Need to set up security first
                switchPasscodeLock.setChecked(false);
                Intent intent = new Intent(this, SecuritySetupActivity.class);
                startActivity(intent);
            } else {
                // Toggle security
                securityManager.setSecurityEnabled(isChecked);
                updateUI();
            }
        });

        cardPasscodeSettings.setOnClickListener(v -> {
            if (securityManager.hasPasscode()) {
                Intent intent = new Intent(this, SecuritySetupActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, SecuritySetupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateUI() {
        boolean hasPasscode = securityManager.hasPasscode();
        boolean isEnabled = securityManager.isSecurityEnabled();

        switchPasscodeLock.setChecked(isEnabled);

        if (hasPasscode) {
            tvPasscodeDescription.setText(getString(R.string.manage_passcode_description));
            cardPasscodeSettings.setClickable(true);
            cardPasscodeSettings.setAlpha(1.0f);
        } else {
            tvPasscodeDescription.setText(getString(R.string.set_up_passcode_description));
            cardPasscodeSettings.setClickable(true);
            cardPasscodeSettings.setAlpha(1.0f);
        }
    }
}
