package com.example.luxevista;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignupStep2Fragment extends Fragment {

    interface Step2Listener {
        String getEmail();
        String getPassword();
    }

    public static SignupStep2Fragment newInstance(String email) {
        Bundle args = new Bundle();
        args.putString("email", email);
        SignupStep2Fragment fragment = new SignupStep2Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    private TextInputLayout tilFullName, tilPhone, tilBirthday;
    private TextInputEditText etFullName, etPhone, etBirthday;
    private ChipGroup chipGroupRoomType;
    private MaterialSwitch switchNoSmoking;
    private MaterialButton btnBack, btnSignup;

    private Calendar birthdayCal = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step2, container, false);

        tilFullName = view.findViewById(R.id.tilFullName);
        tilPhone = view.findViewById(R.id.tilPhone);
        tilBirthday = view.findViewById(R.id.tilBirthday);
        etFullName = view.findViewById(R.id.etFullName);
        etPhone = view.findViewById(R.id.etPhone);
        etBirthday = view.findViewById(R.id.etBirthday);
        chipGroupRoomType = view.findViewById(R.id.chipGroupRoomType);
        switchNoSmoking = view.findViewById(R.id.switchNoSmoking);
        btnBack = view.findViewById(R.id.btnBack);
        btnSignup = view.findViewById(R.id.btnSignup);

        etBirthday.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnSignup.setOnClickListener(v -> onSubmit());

        return view;
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (DatePicker view, int year, int month, int dayOfMonth) -> {
            birthdayCal.set(year, month, dayOfMonth);
            etBirthday.setText(dateFormat.format(birthdayCal.getTime()));
        }, birthdayCal.get(Calendar.YEAR), birthdayCal.get(Calendar.MONTH), birthdayCal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void onSubmit() {
        if (!validate()) return;

        Step2Listener listener = (Step2Listener) getActivity();
        if (listener == null) return;

        String email = listener.getEmail();
        String password = listener.getPassword();

        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthdayIso = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.getDefault()).format(birthdayCal.getTime());
        String roomType = getSelectedRoomType();
        boolean noSmoking = switchNoSmoking.isChecked();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnSignup.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        btnSignup.setEnabled(true);
                        Toast.makeText(getContext(), "Signup failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("email", email);
                    profile.put("phone", phone);
                    profile.put("birthday", birthdayIso);
                    Map<String, Object> prefs = new HashMap<>();
                    prefs.put("roomType", roomType);
                    prefs.put("noSmoking", noSmoking);
                    profile.put("preferences", prefs);
                    profile.put("createdAt", Timestamp.now());

                    db.collection("users")
                            .document(user.getUid())
                            .set(profile)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                                // Navigate to MainActivity after successful signup
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                requireActivity().finish();
                            })
                            .addOnFailureListener(e -> {
                                btnSignup.setEnabled(true);
                                Toast.makeText(getContext(), "Failed to save profile", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSignup.setEnabled(true);
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getSelectedRoomType() {
        int checkedId = chipGroupRoomType.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip chip = chipGroupRoomType.findViewById(checkedId);
            if (chip != null) return chip.getText().toString();
        }
        return "Standard Room";
    }

    private boolean validate() {
        tilFullName.setError(null);
        tilPhone.setError(null);
        tilBirthday.setError(null);

        boolean ok = true;
        if (TextUtils.isEmpty(etFullName.getText())) {
            tilFullName.setError("Name required");
            ok = false;
        }
        if (TextUtils.isEmpty(etPhone.getText())) {
            tilPhone.setError("Phone required");
            ok = false;
        }
        if (TextUtils.isEmpty(etBirthday.getText())) {
            tilBirthday.setError("Birthday required");
            ok = false;
        }
        return ok;
    }
}


