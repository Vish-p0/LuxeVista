package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private EditText editTextName, editTextPhone, editTextBirthday, editTextRoomType;
    private SwitchMaterial switchNoSmoking;
    private Button buttonRegister;
    private TextView textViewLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextRoomType = findViewById(R.id.editTextRoomType);
        switchNoSmoking = findViewById(R.id.switchNoSmokingRegister);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister.setOnClickListener(v -> registerUser());
        
        textViewLogin.setOnClickListener(v -> {
            // Navigate back to login
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        setupBirthdayPicker();
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String birthdayStr = editTextBirthday.getText().toString().trim();
        String roomType = editTextRoomType.getText().toString().trim();
        boolean noSmoking = switchNoSmoking.isChecked();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserDocument(user.getUid(), name, email, phone, birthdayStr, roomType, noSmoking);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void setupBirthdayPicker() {
        editTextBirthday.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
            android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                    RegisterActivity.this,
                    (view, y, m, d) -> {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
                        java.util.Calendar selected = java.util.Calendar.getInstance();
                        selected.set(y, m, d, 0, 0, 0);
                        editTextBirthday.setText(sdf.format(selected.getTime()));
                        editTextBirthday.setTag(selected.getTime());
                    }, year, month, day);
            dialog.show();
        });
    }

    private void saveUserDocument(String uid, String name, String email, String phone, String birthdayStr, String roomType, boolean noSmoking) {
        java.util.Date birthDate = null;
        Object tag = editTextBirthday.getTag();
        if (tag instanceof java.util.Date) {
            birthDate = (java.util.Date) tag;
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        if (birthDate != null) data.put("birthDay", new Timestamp(birthDate));
        data.put("createdAt", Timestamp.now());
        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("roomType", roomType);
        prefs.put("noSmoking", String.valueOf(noSmoking));
        data.put("preferences", prefs);

        firestore.collection("users").document(uid).set(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful! Welcome to LuxeVista", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "User created but failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
    }
}