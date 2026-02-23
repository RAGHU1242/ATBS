package com.agrigo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.agrigo.app.R;
import com.agrigo.app.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String cropType, vehicleType, weightStr;

    private TextView tvCropType, tvWeight, tvVehicleType, tvStatus, tvMessage;
    private TextInputEditText etNotes;
    private MaterialButton btnConfirmBooking;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Retrieve intent extras
        cropType = getIntent().getStringExtra("cropType");
        weightStr = getIntent().getStringExtra("weight");
        vehicleType = getIntent().getStringExtra("vehicleType");

        initViews();
        populateSummary();
        setupListeners();
    }

    private void initViews() {
        tvCropType = findViewById(R.id.tvCropType);
        tvWeight = findViewById(R.id.tvWeight);
        tvVehicleType = findViewById(R.id.tvVehicleType);
        tvStatus = findViewById(R.id.tvStatus);
        tvMessage = findViewById(R.id.tvMessage);
        etNotes = findViewById(R.id.etNotes);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        progressBar = findViewById(R.id.progressBar);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void populateSummary() {
        tvCropType.setText(cropType != null ? cropType : "—");
        tvWeight.setText(weightStr != null ? weightStr + " kg" : "—");
        tvVehicleType.setText(vehicleType != null ? vehicleType : "—");
    }

    private void setupListeners() {
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void confirmBooking() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showMessage("Not authenticated. Please login.", true);
            return;
        }

        double weight = 0;
        try {
            weight = Double.parseDouble(weightStr != null ? weightStr : "0");
        } catch (NumberFormatException e) {
            // ignore, use 0
        }

        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        setLoading(true);

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("farmerId", user.getUid());
        bookingData.put("cropType", cropType);
        bookingData.put("weight", weight);
        bookingData.put("vehicleType", vehicleType);
        bookingData.put("status", "requested");
        bookingData.put("notes", notes);
        bookingData.put("createdAt", Timestamp.now());
        bookingData.put("driverId", null);

        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(docRef -> {
                    setLoading(false);
                    tvStatus.setText("REQUESTED");
                    tvStatus.setTextColor(getColor(R.color.statusRequested));
                    showMessage("Booking confirmed! Drivers are being notified.", false);
                    btnConfirmBooking.setEnabled(false);
                    btnConfirmBooking.setText("Booking Sent ✓");

                    // Listen for status changes
                    listenForBookingUpdates(docRef.getId());
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage("Failed to create booking: " + e.getMessage(), true);
                });
    }

    private void listenForBookingUpdates(String bookingId) {
        db.collection("bookings").document(bookingId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    String status = snapshot.getString("status");
                    if ("accepted".equals(status)) {
                        tvStatus.setText("ACCEPTED");
                        tvStatus.setTextColor(getColor(R.color.statusAccepted));
                        tvStatus.setBackgroundResource(R.drawable.bg_suggestion_card);

                        String driverId = snapshot.getString("driverId");
                        showMessage("Driver accepted your booking! You can now track live.", false);

                        // Show Track button after acceptance
                        btnConfirmBooking.setEnabled(true);
                        btnConfirmBooking.setText("Track Live Location");
                        final String finalDriverId = driverId;
                        final String finalBookingId = bookingId;
                        btnConfirmBooking.setOnClickListener(v -> {
                            Intent intent = new Intent(BookingActivity.this, TrackingActivity.class);
                            intent.putExtra("bookingId", finalBookingId);
                            intent.putExtra("driverId", finalDriverId);
                            intent.putExtra("cropType", cropType);
                            intent.putExtra("vehicleType", vehicleType);
                            startActivity(intent);
                        });
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnConfirmBooking.setEnabled(!loading);
        btnConfirmBooking.setText(loading ? "Confirming..." : "Confirm Booking");
    }

    private void showMessage(String msg, boolean isError) {
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(msg);
        tvMessage.setTextColor(getColor(isError ? R.color.errorRed : R.color.statusAccepted));
    }
}
