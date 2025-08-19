package com.example.luxevista;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class SignupStep1Fragment extends Fragment {

    interface Step1Listener {
        void onStep1Completed(String email, String password);
    }

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnNext = view.findViewById(R.id.btnNext);
        
        TextView tvLoginLink = view.findViewById(R.id.tvLoginLink);
        tvLoginLink.setOnClickListener(v -> {
            // Navigate back to LoginActivity
            requireActivity().finish();
            // Add smooth transition
            requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnNext.setOnClickListener(v -> {
            if (validate()) {
                String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
                checkExistingAndProceed(email, password);
            } else {
                android.util.Log.d("SignupStep1", "Validation failed");
            }
        });

        return view;
    }

    private boolean validate() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean ok = true;
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            ok = false;
        }
        if (!isStrongPassword(password)) {
            tilPassword.setError("Min 8 chars, 1 upper, 1 lower, 1 number, 1 symbol");
            ok = false;
        }
        if (!password.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            ok = false;
        }
        return ok;
    }

    private boolean isStrongPassword(String password) {
        if (password == null) return false;
        // At least 8 chars, 1 upper, 1 lower, 1 digit, 1 symbol
        Pattern strong = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
        return strong.matcher(password).matches();
    }

    private void checkExistingAndProceed(String email, String password) {
        // Disable button to prevent duplicate taps
        btnNext.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    btnNext.setEnabled(true);
                    if (!snap.isEmpty()) {
                        // Account exists; forward to Login
                        android.widget.Toast.makeText(getContext(), "Account already exists. Please log in.", android.widget.Toast.LENGTH_LONG).show();
                        android.content.Intent intent = new android.content.Intent(requireContext(), LoginActivity.class);
                        intent.putExtra("prefillEmail", email);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        // Proceed to step 2
                        Step1Listener listener = (Step1Listener) getActivity();
                        if (listener != null) {
                            android.util.Log.d("SignupStep1", "No existing account, navigating to Step 2 with email: " + email);
                            listener.onStep1Completed(email, password);
                        } else {
                            android.util.Log.e("SignupStep1", "Listener is null - cannot navigate to Step 2");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    android.widget.Toast.makeText(getContext(), "Unable to verify email. Check connection.", android.widget.Toast.LENGTH_SHORT).show();
                });
    }
}


