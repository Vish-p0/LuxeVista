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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SecuritySetupActivity extends AppCompatActivity {

    private TextInputEditText etPasscode;
    private AutoCompleteTextView spinnerQuestion;
    private TextInputEditText etAnswer;
    private Button btnSave;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final String[] QUESTIONS = new String[] {
            "What is your mother's maiden name?",
            "What was your first pet's name?",
            "What city were you born in?",
            "What is your favorite teacher's name?"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etPasscode = findViewById(R.id.etPasscode);
        spinnerQuestion = findViewById(R.id.spinnerQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnSave = findViewById(R.id.btnSaveSecurity);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, QUESTIONS);
        spinnerQuestion.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveSecurity());
    }

    private void saveSecurity() {
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String passcode = etPasscode.getText() != null ? etPasscode.getText().toString().trim() : "";
        String question = spinnerQuestion.getText() != null ? spinnerQuestion.getText().toString().trim() : "";
        String answer = etAnswer.getText() != null ? etAnswer.getText().toString().trim() : "";

        if (passcode.length() != 4 || !TextUtils.isDigitsOnly(passcode)) {
            Toast.makeText(this, "Enter a valid 4-digit passcode", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(answer)) {
            Toast.makeText(this, "Select a question and enter an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> security = new HashMap<>();
        security.put("passcode", passcode);
        security.put("securityQuestion", question);
        security.put("securityAnswer", answer);

        DocumentReference ref = firestore.collection("users").document(current.getUid());
        ref.update(security)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Security settings saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}


