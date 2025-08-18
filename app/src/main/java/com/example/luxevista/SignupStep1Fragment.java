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
                Step1Listener listener = (Step1Listener) getActivity();
                if (listener != null) {
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString();
                    android.util.Log.d("SignupStep1", "Validation passed, navigating to Step 2 with email: " + email);
                    listener.onStep1Completed(email, password);
                } else {
                    android.util.Log.e("SignupStep1", "Listener is null - cannot navigate to Step 2");
                }
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
        if (password.length() < 6) {
            tilPassword.setError("Minimum 6 characters");
            ok = false;
        }
        if (!password.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            ok = false;
        }
        return ok;
    }
}


