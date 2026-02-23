package com.agrigo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.agrigo.app.R;
import com.agrigo.app.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isLoginMode = true;
    private String selectedRole = "farmer";

    private TextView tabLogin, tabRegister;
    private TextInputLayout tilName, tilPhone;
    private TextInputEditText etEmail, etPassword, etName, etPhone;
    private LinearLayout layoutRoleSelector, btnRoleFarmer, btnRoleDriver;
    private MaterialButton btnAction;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        tabLogin = findViewById(R.id.tabLogin);
        tabRegister = findViewById(R.id.tabRegister);
        tilName = findViewById(R.id.tilName);
        tilPhone = findViewById(R.id.tilPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        layoutRoleSelector = findViewById(R.id.layoutRoleSelector);
        btnRoleFarmer = findViewById(R.id.btnRoleFarmer);
        btnRoleDriver = findViewById(R.id.btnRoleDriver);
        btnAction = findViewById(R.id.btnAction);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
    }

    private void setupListeners() {
        tabLogin.setOnClickListener(v -> switchToLogin());
        tabRegister.setOnClickListener(v -> switchToRegister());

        btnRoleFarmer.setOnClickListener(v -> {
            selectedRole = "farmer";
            btnRoleFarmer.setBackgroundResource(R.drawable.bg_button_primary);
            btnRoleDriver.setBackgroundResource(R.drawable.bg_input_field);
            ((TextView) btnRoleFarmer.getChildAt(1)).setTextColor(getColor(R.color.white));
            ((TextView) btnRoleDriver.getChildAt(1)).setTextColor(getColor(R.color.textPrimary));
        });

        btnRoleDriver.setOnClickListener(v -> {
            selectedRole = "driver";
            btnRoleDriver.setBackgroundResource(R.drawable.bg_button_primary);
            btnRoleFarmer.setBackgroundResource(R.drawable.bg_input_field);
            ((TextView) btnRoleDriver.getChildAt(1)).setTextColor(getColor(R.color.white));
            ((TextView) btnRoleFarmer.getChildAt(1)).setTextColor(getColor(R.color.textPrimary));
        });

        btnAction.setOnClickListener(v -> {
            if (isLoginMode) performLogin();
            else performRegister();
        });
    }

    private void switchToLogin() {
        isLoginMode = true;
        tabLogin.setBackgroundColor(getColor(R.color.colorPrimary));
        tabLogin.setTextColor(getColor(R.color.white));
        tabRegister.setBackgroundColor(getColor(R.color.background));
        tabRegister.setTextColor(getColor(R.color.textSecondary));
        tilName.setVisibility(View.GONE);
        tilPhone.setVisibility(View.GONE);
        layoutRoleSelector.setVisibility(View.GONE);
        btnAction.setText(R.string.btn_login);
        clearError();
    }

    private void switchToRegister() {
        isLoginMode = false;
        tabRegister.setBackgroundColor(getColor(R.color.colorPrimary));
        tabRegister.setTextColor(getColor(R.color.white));
        tabLogin.setBackgroundColor(getColor(R.color.background));
        tabLogin.setTextColor(getColor(R.color.textSecondary));
        tilName.setVisibility(View.VISIBLE);
        tilPhone.setVisibility(View.VISIBLE);
        layoutRoleSelector.setVisibility(View.VISIBLE);
        btnAction.setText(R.string.btn_register);
        clearError();
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please fill in all fields");
            return;
        }

        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) navigateBasedOnRole(user.getUid());
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(e.getMessage());
                });
    }

    private void performRegister() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            showError("Please fill in all fields");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        setLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser != null) {
                        saveUserToFirestore(firebaseUser.getUid(), name, phone, email);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(e.getMessage());
                });
    }

    private void saveUserToFirestore(String uid, String name, String phone, String email) {
        User user = new User(uid, name, phone, email, selectedRole);
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> navigateBasedOnRole(uid))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Failed to save user data: " + e.getMessage());
                });
    }

    private void navigateBasedOnRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    setLoading(false);
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        Intent intent = "driver".equals(role)
                                ? new Intent(this, DriverHomeActivity.class)
                                : new Intent(this, FarmerHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Error fetching user role.");
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAction.setEnabled(!loading);
        btnAction.setText(loading ? "Please wait..." : (isLoginMode ? "Login" : "Register"));
    }

    private void showError(String message) {
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(message);
    }

    private void clearError() {
        tvError.setVisibility(View.GONE);
        tvError.setText("");
    }
}
