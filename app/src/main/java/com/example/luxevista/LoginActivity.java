package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister, textViewForgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        // Prefill email if coming from signup redirect
        String prefillEmail = getIntent().getStringExtra("prefillEmail");
        if (prefillEmail != null && !prefillEmail.isEmpty()) {
            editTextEmail.setText(prefillEmail);
        }

        buttonLogin.setOnClickListener(v -> loginUser());

        textViewRegister.setOnClickListener(v -> {
            // Open the new two-step Signup flow
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            // Don't finish LoginActivity so we can return to it
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        textViewForgotPassword.setOnClickListener(v -> {
            // Handle forgot password
            handleForgotPassword();
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    // Navigate to MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    Exception ex = task.getException();
                    boolean showGenericCredsMsg = false;
                    if (ex instanceof FirebaseAuthInvalidCredentialsException || ex instanceof FirebaseAuthInvalidUserException) {
                        showGenericCredsMsg = true;
                    } else if (ex instanceof FirebaseAuthException) {
                        String code = ((FirebaseAuthException) ex).getErrorCode();
                        if ("ERROR_WRONG_PASSWORD".equals(code) || "ERROR_USER_NOT_FOUND".equals(code) || "ERROR_INVALID_EMAIL".equals(code)) {
                            showGenericCredsMsg = true;
                        }
                    }

                    if (showGenericCredsMsg) {
                        String msg = "Your email or password is incorrect";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        editTextPassword.setError(msg);
                        // Also hint on email field without showing error icon persistently
                        editTextEmail.requestFocus();
                    } else {
                        String fallback = ex != null && ex.getMessage() != null ? ex.getMessage() : "Login failed. Please try again.";
                        Toast.makeText(LoginActivity.this, fallback, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void handleForgotPassword() {
        String email = editTextEmail.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Please enter your email first");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}