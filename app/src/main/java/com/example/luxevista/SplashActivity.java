package com.example.luxevista;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    
    private ImageView ivLogo;
    private TextView tvAppName, tvTagline;
    private View dot1, dot2, dot3;
    private TextView tvLoading;
    
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize views
        initViews();
        
        // Start animations
        startAnimations();
        
        // Navigate after delay
        navigateAfterDelay();
    }
    
    private void initViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        tvLoading = findViewById(R.id.tvLoading);
    }
    
    private void startAnimations() {
        // Logo animation
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_scale_animation);
        ivLogo.startAnimation(logoAnimation);
        
        // Text animations with staggered delays
        Animation fadeInUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        
        // App name animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvAppName.setVisibility(View.VISIBLE);
            tvAppName.startAnimation(fadeInUp);
        }, 400);
        
        // Tagline animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvTagline.setVisibility(View.VISIBLE);
            Animation taglineAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
            tvTagline.startAnimation(taglineAnim);
        }, 800);
        
        // Loading container animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            findViewById(R.id.loadingContainer).setVisibility(View.VISIBLE);
            Animation loadingAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
            findViewById(R.id.loadingContainer).startAnimation(loadingAnim);
            
            // Start dot animations
            startLoadingDotsAnimation();
        }, 1200);
        
        // Initially hide animated elements
        tvAppName.setVisibility(View.INVISIBLE);
        tvTagline.setVisibility(View.INVISIBLE);
        findViewById(R.id.loadingContainer).setVisibility(View.INVISIBLE);
    }
    
    private void startLoadingDotsAnimation() {
        // Create staggered dot animations
        Animation dotAnim1 = AnimationUtils.loadAnimation(this, R.anim.loading_dot_animation);
        Animation dotAnim2 = AnimationUtils.loadAnimation(this, R.anim.loading_dot_animation);
        Animation dotAnim3 = AnimationUtils.loadAnimation(this, R.anim.loading_dot_animation);
        
        // Start animations with delays
        dot1.startAnimation(dotAnim1);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dot2.startAnimation(dotAnim2);
        }, 200);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dot3.startAnimation(dotAnim3);
        }, 400);
    }
    
    private void navigateAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthenticationAndNavigate();
        }, SPLASH_DURATION);
    }
    
    private void checkAuthenticationAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        Intent intent;
        if (currentUser != null) {
            // User is logged in, check if security is enabled
            SecurityManager securityManager = new SecurityManager(this);
            if (securityManager.isSecurityEnabled() && securityManager.hasPasscode()) {
                // Security is enabled, go to passcode verification
                intent = new Intent(SplashActivity.this, PasscodeVerificationActivity.class);
            } else {
                // No security or security disabled, go to main activity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }
        } else {
            // User is not logged in, go to login activity
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    @Override
    @SuppressWarnings("MissingSuperCall")
    public void onBackPressed() {
        // Disable back button on splash screen
        // Intentionally not calling super.onBackPressed() to prevent user from going back
    }
}
