package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PasscodeVerificationActivity extends AppCompatActivity {

    private TextInputEditText etPasscode;
    private MaterialButton btnVerify;
    private MaterialButton btnForgotPasscode;
    private TextView tvAppName;

    private SecurityManager securityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode_verification);

        securityManager = new SecurityManager(this);

        // Check if security is actually enabled
        if (!securityManager.isSecurityEnabled() || !securityManager.hasPasscode()) {
            // Skip verification and go to main activity
            navigateToMainActivity();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etPasscode = findViewById(R.id.etPasscode);
        btnVerify = findViewById(R.id.btnVerify);
        btnForgotPasscode = findViewById(R.id.btnForgotPasscode);
        tvAppName = findViewById(R.id.tvAppName);

        // Show forgot passcode button only if security question is set
        if (!securityManager.hasSecurityQuestion()) {
            btnForgotPasscode.setVisibility(android.view.View.GONE);
        }
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(v -> verifyPasscode());
        btnForgotPasscode.setOnClickListener(v -> openRecovery());
        
        // Allow verification on enter key
        etPasscode.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            verifyPasscode();
            return true;
        });
    }

    private void verifyPasscode() {
        String passcode = etPasscode.getText() != null ? etPasscode.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(passcode)) {
            Toast.makeText(this, "Please enter your passcode", Toast.LENGTH_SHORT).show();
            etPasscode.requestFocus();
            return;
        }

        if (passcode.length() != 4) {
            Toast.makeText(this, "Passcode must be 4 digits", Toast.LENGTH_SHORT).show();
            etPasscode.setText("");
            etPasscode.requestFocus();
            return;
        }

        if (securityManager.verifyPasscode(passcode)) {
            // Passcode correct, navigate to main activity
            navigateToMainActivity();
        } else {
            Toast.makeText(this, "Incorrect passcode. Please try again.", Toast.LENGTH_SHORT).show();
            etPasscode.setText("");
            etPasscode.requestFocus();
        }
    }

    private void openRecovery() {
        Intent intent = new Intent(this, SecurityRecoveryActivity.class);
        startActivity(intent);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Ensure superclass behavior is invoked as required by lint
        super.onBackPressed();
        // Prevent back button from bypassing security by moving app to background
        moveTaskToBack(true);
    }
}
