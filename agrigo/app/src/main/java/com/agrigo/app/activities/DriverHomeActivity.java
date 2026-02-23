package com.agrigo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.agrigo.app.R;
import com.agrigo.app.adapters.RequestAdapter;
import com.agrigo.app.models.Booking;
import com.agrigo.app.services.LocationTrackingService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DriverHomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isOnline = false;
    private ListenerRegistration bookingsListener;

    private MaterialButton btnToggleStatus;
    private TextView tvStatus, tvDriverName, tvRequestCount;
    private TextView tvTotalRides, tvPendingRides, tvAcceptedRides;
    private RecyclerView rvRequests;
    private LinearLayout layoutEmpty;
    private RequestAdapter requestAdapter;
    private View statusDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadDriverInfo();
        setupListeners();
    }

    private void initViews() {
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        tvStatus = findViewById(R.id.tvStatus);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvRequestCount = findViewById(R.id.tvRequestCount);
        tvTotalRides = findViewById(R.id.tvTotalRides);
        tvPendingRides = findViewById(R.id.tvPendingRides);
        tvAcceptedRides = findViewById(R.id.tvAcceptedRides);
        rvRequests = findViewById(R.id.rvRequests);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        statusDot = findViewById(R.id.statusDot);

        requestAdapter = new RequestAdapter(new ArrayList<>(),
                this::onAcceptRequest, this::onDeclineRequest);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);

        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadDriverInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvDriverName.setText(name);
                    }
                });

        loadRideStats();
    }

    private void loadRideStats() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("bookings")
                .whereEqualTo("driverId", user.getUid())
                .get()
                .addOnSuccessListener(snap -> {
                    int total = 0, pending = 0, accepted = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        total++;
                        String status = doc.getString("status");
                        if ("requested".equals(status)) pending++;
                        else if ("accepted".equals(status)) accepted++;
                    }
                    tvTotalRides.setText(String.valueOf(total));
                    tvPendingRides.setText(String.valueOf(pending));
                    tvAcceptedRides.setText(String.valueOf(accepted));
                });
    }

    private void setupListeners() {
        btnToggleStatus.setOnClickListener(v -> {
            if (isOnline) goOffline();
            else goOnline();
        });
    }

    private void goOnline() {
        isOnline = true;
        tvStatus.setText("Online");
        tvStatus.setTextColor(getColor(R.color.statusOnline));
        statusDot.setBackgroundResource(R.drawable.bg_circle_green);
        btnToggleStatus.setText("Go Offline");
        btnToggleStatus.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(R.color.errorRed)));

        // Start location tracking service
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        startForegroundService(serviceIntent);

        // Start listening for booking requests
        listenForRequests();
    }

    private void goOffline() {
        isOnline = false;
        tvStatus.setText("Offline");
        tvStatus.setTextColor(getColor(R.color.statusOffline));
        btnToggleStatus.setText("Go Online");
        btnToggleStatus.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(R.color.colorPrimary)));

        // Stop location tracking
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        stopService(serviceIntent);

        // Stop listening
        if (bookingsListener != null) {
            bookingsListener.remove();
        }

        requestAdapter.updateData(new ArrayList<>());
        layoutEmpty.setVisibility(View.VISIBLE);
        tvRequestCount.setText("0");
    }

    private void listenForRequests() {
        if (bookingsListener != null) bookingsListener.remove();

        bookingsListener = db.collection("bookings")
                .whereEqualTo("status", "requested")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null || querySnapshot == null) return;

                    List<Booking> requests = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Booking b = doc.toObject(Booking.class);
                        b.setBookingId(doc.getId());
                        requests.add(b);
                    }

                    requestAdapter.updateData(requests);
                    tvRequestCount.setText(String.valueOf(requests.size()));
                    layoutEmpty.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void onAcceptRequest(Booking booking) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("bookings").document(booking.getBookingId())
                .update("status", "accepted", "driverId", user.getUid())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking accepted!", Toast.LENGTH_SHORT).show();
                    loadRideStats();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void onDeclineRequest(Booking booking) {
        // Just remove from local list for this driver; don't delete from DB
        List<Booking> current = requestAdapter.getItems();
        current.remove(booking);
        requestAdapter.updateData(current);
        tvRequestCount.setText(String.valueOf(current.size()));
        if (current.isEmpty()) layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void logout() {
        goOffline();
        mAuth.signOut();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingsListener != null) bookingsListener.remove();
    }
}
