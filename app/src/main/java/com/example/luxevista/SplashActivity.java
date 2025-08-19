package com.example.luxevista;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 4500; // slower splash: 4.5 seconds
    private static final int WELCOME_TEXT_DELAY = 1200; // slightly slower reveal
    
    private LottieAnimationView lottieAnimation;
    private TextView tvWelcomeText;
    private TextView tvVersion;
    private LinearProgressIndicator progressBar;
    
    private FirebaseAuth mAuth;
    private boolean animationComplete = false;
    private boolean timerComplete = false;
    private boolean hasNavigated = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable welcomeTextRunnable;
    private Runnable versionTextRunnable;
    private Runnable progressBarRunnable;
    private Runnable timerRunnable;

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
        
        // Set up navigation timer
        setupNavigationTimer();
    }
    
    private void initViews() {
        lottieAnimation = findViewById(R.id.lottieAnimation);
        tvWelcomeText = findViewById(R.id.tvWelcomeText);
        tvVersion = findViewById(R.id.tvVersion);
        progressBar = findViewById(R.id.progressBar);
        
        // Initially hide animated elements
        tvWelcomeText.setAlpha(0f);
        tvVersion.setAlpha(0f);
        lottieAnimation.setAlpha(0f);
    }
    
    private void startAnimations() {
        // Lottie animation fade in (slower)
        lottieAnimation.animate()
            .alpha(1f)
            .setDuration(900)
            .setStartDelay(600)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Animation is now visible, mark as complete
                    animationComplete = true;
                    checkAndNavigate();
                }
            })
            .start();
        
    // Welcome text animation (slower)
        welcomeTextRunnable = () -> {
            tvWelcomeText.animate()
                .alpha(1f)
                .translationY(0f)
        .setDuration(900)
                .start();
        };
        mainHandler.postDelayed(welcomeTextRunnable, WELCOME_TEXT_DELAY);
        
    // Version text animation (slower)
        versionTextRunnable = () -> {
            tvVersion.animate()
                .alpha(1f)
        .setDuration(700)
                .start();
        };
        mainHandler.postDelayed(versionTextRunnable, WELCOME_TEXT_DELAY + 300);
        
        // Optional: Show progress bar after a delay
        progressBarRunnable = () -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.animate()
                    .alpha(1f)
            .setDuration(600)
                    .start();
            }
        };
        mainHandler.postDelayed(progressBarRunnable, WELCOME_TEXT_DELAY + 600);
    }
    
    private void setupNavigationTimer() {
        timerRunnable = () -> {
            timerComplete = true;
            checkAndNavigate();
        };
        mainHandler.postDelayed(timerRunnable, SPLASH_DURATION);
    }
    
    private void checkAndNavigate() {
        // Navigate only when both animation is visible and timer is complete
        if (hasNavigated) return;
        if (animationComplete && timerComplete) {
            // Both conditions met; perform fade-out then navigate once
            navigateToNextScreen();
        }
    }
    
    private void navigateToNextScreen() {
        // Fade out all elements before navigation
        fadeOutAndNavigate();
    }
    
    private void fadeOutAndNavigate() {
        // Fade out all visible elements
        lottieAnimation.animate().alpha(0f).setDuration(300);
        tvWelcomeText.animate().alpha(0f).setDuration(300);
        tvVersion.animate().alpha(0f).setDuration(300);
        
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            progressBar.animate().alpha(0f).setDuration(300);
        }
        
        // Navigate after fade out
        mainHandler.postDelayed(this::startNextIfNeeded, 350);
    }

    private void startNextIfNeeded() {
        if (hasNavigated) return;
        hasNavigated = true;
        checkAuthenticationAndNavigate();
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
    protected void onPause() {
        super.onPause();
        // Pause Lottie animation to save resources
        if (lottieAnimation != null) {
            lottieAnimation.pauseAnimation();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Resume Lottie animation
        if (lottieAnimation != null && !lottieAnimation.isAnimating()) {
            lottieAnimation.resumeAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure no pending callbacks can fire after Activity is finished
        if (welcomeTextRunnable != null) mainHandler.removeCallbacks(welcomeTextRunnable);
        if (versionTextRunnable != null) mainHandler.removeCallbacks(versionTextRunnable);
        if (progressBarRunnable != null) mainHandler.removeCallbacks(progressBarRunnable);
        if (timerRunnable != null) mainHandler.removeCallbacks(timerRunnable);
        mainHandler.removeCallbacksAndMessages(null);
    }
    
    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        // Intentionally not calling super.onBackPressed() to prevent user from going back
    }
}