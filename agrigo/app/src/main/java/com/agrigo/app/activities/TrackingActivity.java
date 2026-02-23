package com.agrigo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.agrigo.app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.Locale;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private ListenerRegistration locationListener;
    private Marker driverMarker;

    private String bookingId, driverId, cropType, vehicleType;

    private TextView tvDriverCoords, tvLocationStatus, tvCropInfo, tvVehicleInfo, tvBookingStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        db = FirebaseFirestore.getInstance();

        bookingId = getIntent().getStringExtra("bookingId");
        driverId = getIntent().getStringExtra("driverId");
        cropType = getIntent().getStringExtra("cropType");
        vehicleType = getIntent().getStringExtra("vehicleType");

        initViews();
        populateBookingInfo();
        setupMap();
    }

    private void initViews() {
        tvDriverCoords = findViewById(R.id.tvDriverCoords);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvCropInfo = findViewById(R.id.tvCropInfo);
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo);
        tvBookingStatus = findViewById(R.id.tvBookingStatus);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void populateBookingInfo() {
        tvCropInfo.setText(cropType != null ? cropType : "—");
        tvVehicleInfo.setText(vehicleType != null ? vehicleType : "—");
        tvBookingStatus.setText("ACCEPTED");
    }

    private void setupMap() {
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Default to India center
        LatLng defaultLatLng = new LatLng(20.5937, 78.9629);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 5f));

        startListeningDriverLocation();
    }

    private void startListeningDriverLocation() {
        if (driverId == null || driverId.isEmpty()) return;

        locationListener = db.collection("driverLocations")
                .document(driverId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) {
                        tvLocationStatus.setText("Offline");
                        return;
                    }

                    Double lat = snapshot.getDouble("lat");
                    Double lng = snapshot.getDouble("lng");

                    if (lat != null && lng != null) {
                        updateDriverMarker(lat, lng);
                        tvDriverCoords.setText(String.format(Locale.US,
                                "%.4f, %.4f", lat, lng));
                        tvLocationStatus.setText("Live");
                    }
                });
    }

    private void updateDriverMarker(double lat, double lng) {
        LatLng position = new LatLng(lat, lng);

        if (driverMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title("Driver")
                    .snippet(vehicleType)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            driverMarker = mMap.addMarker(markerOptions);

            // Zoom to driver on first location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));
        } else {
            driverMarker.setPosition(position);
            // Smooth camera follow
            mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationListener != null) locationListener.remove();
    }
}
