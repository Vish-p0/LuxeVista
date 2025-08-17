package com.example.luxevista;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SecuritySetupActivity extends AppCompatActivity {

    private TextInputEditText etPasscode;
    private MaterialAutoCompleteTextView spinnerQuestion;
    private TextInputEditText etAnswer;
    private MaterialButton btnSave;
    private ImageView btnBack;
    private SwitchMaterial switchSecurity;
    private TextInputLayout layoutPasscode, layoutQuestion, layoutAnswer;

    private SecurityManager securityManager;

    private static final String[] QUESTIONS = new String[] {
            "What was the name of your first pet?",
            "What is your mother's maiden name?",
            "What was the name of your first school?",
            "What is your favorite childhood memory?",
            "What was your childhood nickname?",
            "What city were you born in?"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_setup);

        securityManager = new SecurityManager(this);

        initViews();
        setupDropdown();
        setupClickListeners();
        loadExistingData();
        updateSecurityFieldsVisibility();
    }

    private void initViews() {
        etPasscode = findViewById(R.id.etPasscode);
        spinnerQuestion = findViewById(R.id.spinnerQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnSave = findViewById(R.id.btnSaveSecurity);
        btnBack = findViewById(R.id.btnBack);
        switchSecurity = findViewById(R.id.switchSecurity);
        layoutPasscode = findViewById(R.id.layoutPasscode);
        layoutQuestion = findViewById(R.id.layoutQuestion);
        layoutAnswer = findViewById(R.id.layoutAnswer);
    }

    private void setupDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, QUESTIONS);
        spinnerQuestion.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSecurity());
        
        switchSecurity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSecurityFieldsVisibility();
            if (!isChecked) {
                // Clear fields when security is disabled
                etPasscode.setText("");
                etAnswer.setText("");
                spinnerQuestion.setText("", false);
            }
        });
    }

    private void updateSecurityFieldsVisibility() {
        boolean isEnabled = switchSecurity.isChecked();
        int visibility = isEnabled ? View.VISIBLE : View.GONE;
        
        layoutPasscode.setVisibility(visibility);
        layoutQuestion.setVisibility(visibility);
        layoutAnswer.setVisibility(visibility);
        
        // Update button text
        if (isEnabled) {
            btnSave.setText(securityManager.hasPasscode() ? getString(R.string.update_security_settings) : getString(R.string.save_security_settings));
        } else {
            btnSave.setText(getString(R.string.disable_security));
        }
    }

    private void loadExistingData() {
        // Load current security status
        boolean isSecurityEnabled = securityManager.isSecurityEnabled();
        switchSecurity.setChecked(isSecurityEnabled);
        
        // If passcode already exists, show existing data
        if (securityManager.hasPasscode()) {
            // Show existing security question if available
            String existingQuestion = securityManager.getSecurityQuestion();
            if (existingQuestion != null) {
                spinnerQuestion.setText(existingQuestion, false);
            }
        }
    }

    private void saveSecurity() {
        boolean isSecurityEnabled = switchSecurity.isChecked();
        
        if (!isSecurityEnabled) {
            // Disable security
            securityManager.setSecurityEnabled(false);
            Toast.makeText(this, getString(R.string.security_disabled), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String passcode = etPasscode.getText() != null ? etPasscode.getText().toString().trim() : "";
        String question = spinnerQuestion.getText() != null ? spinnerQuestion.getText().toString().trim() : "";
        String answer = etAnswer.getText() != null ? etAnswer.getText().toString().trim() : "";

        // Validation
        if (passcode.length() != 4 || !TextUtils.isDigitsOnly(passcode)) {
            Toast.makeText(this, getString(R.string.valid_passcode_required), Toast.LENGTH_SHORT).show();
            etPasscode.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(question)) {
            Toast.makeText(this, getString(R.string.select_security_question), Toast.LENGTH_SHORT).show();
            spinnerQuestion.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(answer) || answer.length() < 2) {
            Toast.makeText(this, getString(R.string.valid_answer_required), Toast.LENGTH_SHORT).show();
            etAnswer.requestFocus();
            return;
        }

        // Save to local storage
        securityManager.setPasscode(passcode);
        securityManager.setSecurityQuestion(question, answer);
        securityManager.setSecurityEnabled(true);

        Toast.makeText(this, getString(R.string.security_settings_saved), Toast.LENGTH_SHORT).show();
        finish();
    }
}


