package com.example.luxevista;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.widget.TextView;

public class SignupActivity extends AppCompatActivity implements SignupStep1Fragment.Step1Listener, SignupStep2Fragment.Step2Listener {

    private String email;
    private String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (savedInstanceState == null) {
            replaceFragment(new SignupStep1Fragment(), false);
            updateHeader(1);
        }
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.signup_container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        // Use allowingStateLoss to avoid edge-case crashes that could bounce user back to Splash/Login
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onStep1Completed(String email, String password) {
        android.util.Log.d("SignupActivity", "Step 1 completed, navigating to Step 2");
        this.email = email;
        this.password = password;
        replaceFragment(SignupStep2Fragment.newInstance(email), true);
        updateHeader(2);
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    private void updateHeader(int step) {
        TextView tvCircle = findViewById(R.id.tvStepCircle);
        TextView tvTitle = findViewById(R.id.tvStepTitle);
        if (tvCircle == null || tvTitle == null) return;
        if (step == 1) {
            tvCircle.setText("1");
            tvTitle.setText("Account information");
        } else {
            tvCircle.setText("2");
            tvTitle.setText("Personal information");
        }
    }
}


