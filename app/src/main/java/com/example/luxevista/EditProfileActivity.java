package com.example.luxevista;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
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
    private TextInputEditText etRoomType;
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
        etRoomType = findViewById(R.id.etRoomType);
        switchNoSmoking = findViewById(R.id.switchNoSmoking);
        btnSave = findViewById(R.id.btnSave);

        preload();

        btnSave.setOnClickListener(v -> save());
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
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String roomType = etRoomType.getText() != null ? etRoomType.getText().toString().trim() : "";
        boolean noSmoking = switchNoSmoking.isChecked();

        Map<String, Object> update = new HashMap<>();
        update.put("name", name);
        update.put("phone", phone);
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("roomType", roomType);
        prefs.put("noSmoking", String.valueOf(noSmoking));
        update.put("preferences", prefs);

        DocumentReference ref = firestore.collection("users").document(current.getUid());
        ref.update(update)
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}


