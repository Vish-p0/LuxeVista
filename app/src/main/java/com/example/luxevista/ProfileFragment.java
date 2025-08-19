package com.example.luxevista;

import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView ivProfile;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserPhone;
    private TextView tvUserBirthday;
    private View btnEditProfile;
    private View btnSecurity;
    private View btnSettings;
    private View btnHelp;
    private View btnLogout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new Fade());
        setExitTransition(new Fade());
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfile = root.findViewById(R.id.ivProfile);
        tvUserName = root.findViewById(R.id.tvUserName);
        tvUserEmail = root.findViewById(R.id.tvUserEmail);
        tvUserPhone = root.findViewById(R.id.tvUserPhone);
        tvUserBirthday = root.findViewById(R.id.tvUserBirthday);

        btnEditProfile = root.findViewById(R.id.btnEditProfile);
        btnSecurity = root.findViewById(R.id.btnSecurity);
        btnSettings = root.findViewById(R.id.btnSettings);
        btnHelp = root.findViewById(R.id.btnHelp);
        btnLogout = root.findViewById(R.id.btnLogout);

        setupClicks();
        loadUser();
        
        // Load profile image with fallback
        ImageUtils.loadProfileImageWithFallback(ivProfile, null);

        return root;
    }

    private void setupClicks() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        btnSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SecuritySetupActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        btnSettings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.settingsFragment);
        });
        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HelpActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void loadUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            return;
        }

        String uid = currentUser.getUid();
        DocumentReference docRef = firestore.collection("users").document(uid);
        docRef.get().addOnSuccessListener(this::bindUser).addOnFailureListener(e -> {
            // Fallback to auth profile if Firestore fails
            tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "");
            tvUserEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            tvUserPhone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
            tvUserBirthday.setText("");
        });
    }

    private void bindUser(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "");
                tvUserEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
                tvUserPhone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
                tvUserBirthday.setText(getString(R.string.not_set));
            }
            return;
        }

        String name = snapshot.getString("name");
        String email = snapshot.getString("email");
        String phone = snapshot.getString("phone");
    Timestamp birthDay = snapshot.getTimestamp("birthDay");
    String birthdayIso = snapshot.getString("birthday");

        tvUserName.setText(name != null ? name : "");
        tvUserEmail.setText(email != null ? email : "");
        tvUserPhone.setText(phone != null ? phone : "");
        String birthdayText = "";
        if (birthDay != null) {
            birthdayText = formatDate(birthDay.toDate());
        } else if (birthdayIso != null && !birthdayIso.isEmpty()) {
            try {
                // try multiple formats
                java.util.Date parsed;
                try {
                    parsed = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).parse(birthdayIso);
                } catch (Exception e1) {
                    try {
                        parsed = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(birthdayIso);
                    } catch (Exception e2) {
                        parsed = null;
                    }
                }
                if (parsed != null) birthdayText = formatDate(parsed);
            } catch (Exception ignored) {}
        }
        tvUserBirthday.setText(!birthdayText.isEmpty() ? birthdayText : getString(R.string.not_set));
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}



