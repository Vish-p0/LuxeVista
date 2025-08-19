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
import android.widget.Spinner;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private Spinner spinnerRoomType;
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
        spinnerRoomType = view.findViewById(R.id.spinnerRoomType);
        
        // Set up room type spinner with data from database
        loadRoomTypesFromDatabase();
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
    // Require users to be at least 12 years old: prevent selecting dates after (today - 12 years)
    Calendar maxBirthCal = Calendar.getInstance();
    maxBirthCal.add(Calendar.YEAR, -12);
    dialog.getDatePicker().setMaxDate(maxBirthCal.getTimeInMillis());
        dialog.show();
    }

    private void onSubmit() {
        if (!validate()) return;

        Step2Listener listener = (Step2Listener) getActivity();
        if (listener == null) return;

        String email = listener.getEmail();
        String password = listener.getPassword();

    String name = etFullName.getText() == null ? "" : etFullName.getText().toString().trim();
    String phoneRaw = etPhone.getText() == null ? "" : etPhone.getText().toString();
    String phone = phoneRaw.replaceAll("\\D", "");
    String birthdayIso = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.getDefault()).format(birthdayCal.getTime());
    Timestamp birthDayTs = new Timestamp(birthdayCal.getTime());
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
                    profile.put("birthDay", birthDayTs);
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
        return spinnerRoomType.getSelectedItem().toString();
    }

    private void loadRoomTypesFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("rooms")
            .whereEqualTo("visible", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> roomNames = new ArrayList<>();
                
                for (var document : queryDocumentSnapshots) {
                    String roomName = document.getString("name");
                    if (roomName != null && !roomName.isEmpty()) {
                        roomNames.add(roomName);
                    }
                }
                
                // Sort room names alphabetically
                Collections.sort(roomNames);
                
                // Create and set adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), 
                    android.R.layout.simple_spinner_item, 
                    roomNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoomType.setAdapter(adapter);
                
                // Set default selection if available
                if (!roomNames.isEmpty()) {
                    spinnerRoomType.setSelection(0);
                }
            })
            .addOnFailureListener(e -> {
                // Fallback to default room types if database fails
                String[] defaultRoomTypes = {"Standard Room", "Deluxe Room", "Suite"};
                ArrayAdapter<String> fallbackAdapter = new ArrayAdapter<>(
                    requireContext(), 
                    android.R.layout.simple_spinner_item, 
                    defaultRoomTypes
                );
                fallbackAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoomType.setAdapter(fallbackAdapter);
                
                Toast.makeText(requireContext(), "Failed to load room types", Toast.LENGTH_SHORT).show();
            });
    }

    private boolean validate() {
        tilFullName.setError(null);
        tilPhone.setError(null);
        tilBirthday.setError(null);

        boolean ok = true;
        String name = etFullName.getText() == null ? "" : etFullName.getText().toString().trim();
        String phoneRaw = etPhone.getText() == null ? "" : etPhone.getText().toString();
        String phoneDigits = phoneRaw.replaceAll("\\D", "");
        String birthdayText = etBirthday.getText() == null ? "" : etBirthday.getText().toString().trim();

        if (name.isEmpty()) {
            tilFullName.setError("Enter your full name");
            ok = false;
        }
        if (phoneDigits.length() != 10) {
            tilPhone.setError("Phone must be exactly 10 digits");
            ok = false;
        }
        if (TextUtils.isEmpty(birthdayText)) {
            tilBirthday.setError("Birthday required");
            ok = false;
        } else {
            Calendar minAllowed = Calendar.getInstance();
            minAllowed.add(Calendar.YEAR, -12);
            if (birthdayCal.after(minAllowed)) {
                tilBirthday.setError("You must be at least 12 years old");
                ok = false;
            }
        }
        if (!phoneRaw.equals(phoneDigits)) {
            etPhone.setText(phoneDigits);
        }
        return ok;
    }
}


