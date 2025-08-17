package com.example.luxevista;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private com.google.android.material.textfield.TextInputEditText etBirthday;
    private com.google.android.material.textfield.MaterialAutoCompleteTextView etRoomType;
    private SwitchMaterial switchNoSmoking;
    private Button btnSave;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etBirthday = findViewById(R.id.etBirthday);
        etRoomType = findViewById(R.id.etRoomType);
        switchNoSmoking = findViewById(R.id.switchNoSmoking);
        btnSave = findViewById(R.id.btnSave);

        // Set up back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        preload();

        btnSave.setOnClickListener(v -> save());
        etBirthday.setOnClickListener(v -> showDatePicker());
        etBirthday.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) showDatePicker(); });
        loadRoomTypes();
    }

    private void preload() {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current == null) {
            finish();
            return;
        }
        firestore.collection("users").document(current.getUid()).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    String phone = doc.getString("phone");
                    if (!TextUtils.isEmpty(name)) etName.setText(name);
                    if (!TextUtils.isEmpty(phone)) etPhone.setText(phone);
                    if (doc.contains("birthDay")) {
                        com.google.firebase.Timestamp ts = doc.getTimestamp("birthDay");
                        if (ts != null) etBirthday.setText(new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(ts.toDate()));
                    }
                    Map<String, Object> prefs = (Map<String, Object>) doc.get("preferences");
                    if (prefs != null) {
                        Object roomType = prefs.get("roomType");
                        Object noSmoking = prefs.get("noSmoking");
                        if (roomType != null) etRoomType.setText(roomType.toString());
                        if (noSmoking != null) switchNoSmoking.setChecked(Boolean.parseBoolean(noSmoking.toString()));
                    }
                }
            });
    }

    private void save() {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current == null) {
            Toast.makeText(this, getString(R.string.not_signed_in), Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String roomType = etRoomType.getText() != null ? etRoomType.getText().toString().trim() : "";
        String birthdayStr = etBirthday.getText() != null ? etBirthday.getText().toString().trim() : "";
        boolean noSmoking = switchNoSmoking.isChecked();

        Map<String, Object> update = new HashMap<>();
        update.put("name", name);
        update.put("phone", phone);
        if (!TextUtils.isEmpty(birthdayStr)) {
            try {
                java.util.Date date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(birthdayStr);
                if (date != null) update.put("birthDay", new com.google.firebase.Timestamp(date));
            } catch (Exception ignored) {}
        }
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("roomType", roomType);
        prefs.put("noSmoking", String.valueOf(noSmoking));
        update.put("preferences", prefs);

        DocumentReference ref = firestore.collection("users").document(current.getUid());
        ref.update(update)
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showDatePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        com.google.android.material.datepicker.MaterialDatePicker<Long> picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_birthday))
                .setSelection(cal.getTimeInMillis())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            etBirthday.setText(sdf.format(new java.util.Date(selection)));
        });
        picker.show(getSupportFragmentManager(), "birthday_picker");
    }

    private void loadRoomTypes() {
        firestore.collection("rooms").get().addOnSuccessListener(snap -> {
            java.util.List<String> names = new java.util.ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot d : snap) {
                String name = d.getString("name");
                if (!TextUtils.isEmpty(name)) names.add(name);
            }
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            etRoomType.setAdapter(adapter);
        });
    }
}


