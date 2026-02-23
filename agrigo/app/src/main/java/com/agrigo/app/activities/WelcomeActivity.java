package com.agrigo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.agrigo.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            redirectToRoleDashboard(currentUser.getUid());
            return;
        }

        setContentView(R.layout.activity_welcome);

        View btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });
    }

    private void redirectToRoleDashboard(String uid) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        Intent intent;
                        if ("driver".equals(role)) {
                            intent = new Intent(this, DriverHomeActivity.class);
                        } else {
                            intent = new Intent(this, FarmerHomeActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        // User doc missing — go to login
                        setContentView(R.layout.activity_welcome);
                        setupButton();
                    }
                })
                .addOnFailureListener(e -> {
                    setContentView(R.layout.activity_welcome);
                    setupButton();
                });
    }

    private void setupButton() {
        View btn = findViewById(R.id.btnGetStarted);
        if (btn != null) {
            btn.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class)));
        }
    }
}
