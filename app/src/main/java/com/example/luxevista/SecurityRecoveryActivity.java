package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SecurityRecoveryActivity extends AppCompatActivity {

    private TextView tvSecurityQuestion;
    private TextInputEditText etSecurityAnswer;
    private MaterialButton btnVerifyAnswer;
    private MaterialButton btnBackToPasscode;
    private ImageView btnBack;

    private SecurityManager securityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_recovery);

        securityManager = new SecurityManager(this);

        initViews();
        setupClickListeners();
        loadSecurityQuestion();
    }

    private void initViews() {
        tvSecurityQuestion = findViewById(R.id.tvSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        btnVerifyAnswer = findViewById(R.id.btnVerifyAnswer);
        btnBackToPasscode = findViewById(R.id.btnBackToPasscode);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnBackToPasscode.setOnClickListener(v -> finish());
        btnVerifyAnswer.setOnClickListener(v -> verifyAnswer());
    }

    private void loadSecurityQuestion() {
        String question = securityManager.getSecurityQuestion();
        if (question != null) {
            tvSecurityQuestion.setText(question);
        } else {
            // No security question set, redirect to setup
            Toast.makeText(this, "No security question found. Please set up security first.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SecuritySetupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void verifyAnswer() {
        String answer = etSecurityAnswer.getText() != null ? etSecurityAnswer.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(answer)) {
            Toast.makeText(this, "Please enter your answer", Toast.LENGTH_SHORT).show();
            etSecurityAnswer.requestFocus();
            return;
        }

        if (securityManager.verifySecurityAnswer(answer)) {
            Toast.makeText(this, "Answer verified! You can now set a new passcode.", Toast.LENGTH_SHORT).show();
            
            // Redirect to security setup to set new passcode
            Intent intent = new Intent(this, SecuritySetupActivity.class);
            intent.putExtra("recovery_mode", true);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Incorrect answer. Please try again.", Toast.LENGTH_SHORT).show();
            etSecurityAnswer.setText("");
            etSecurityAnswer.requestFocus();
        }
    }
}
