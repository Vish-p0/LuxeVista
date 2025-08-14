package com.example.luxevista;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    private TextInputEditText etSubject;
    private TextInputEditText etMessage;
    private AutoCompleteTextView dropdownCategory;
    private Button btnSubmitHelp;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final String[] CATEGORIES = new String[] {
            "App Issue", "Booking Issue", "Payment Query", "Other"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etSubject = findViewById(R.id.etSubject);
        etMessage = findViewById(R.id.etMessage);
        dropdownCategory = findViewById(R.id.dropdownCategory);
        btnSubmitHelp = findViewById(R.id.btnSubmitHelp);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CATEGORIES);
        dropdownCategory.setAdapter(adapter);

        btnSubmitHelp.setOnClickListener(v -> submit());
    }

    private void submit() {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = etSubject.getText() != null ? etSubject.getText().toString().trim() : "";
        String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        String category = dropdownCategory.getText() != null ? dropdownCategory.getText().toString().trim() : "";

        if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(message) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", current.getUid());
        data.put("subject", subject);
        data.put("message", message);
        data.put("category", category);
        data.put("createdAt", Timestamp.now());

        firestore.collection("helpRequests").add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}


