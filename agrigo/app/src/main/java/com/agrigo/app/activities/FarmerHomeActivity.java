package com.agrigo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.agrigo.app.R;
import com.agrigo.app.adapters.BookingAdapter;
import com.agrigo.app.models.Booking;
import com.agrigo.app.network.PredictRequest;
import com.agrigo.app.network.PredictResponse;
import com.agrigo.app.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerHomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Spinner spinnerCropType;
    private TextInputEditText etWeight;
    private MaterialButton btnGetSuggestion, btnProceedBook;
    private ProgressBar progressBar;
    private CardView cardSuggestion;
    private TextView tvVehicleSuggestion, tvSuggestionDesc, tvGreeting;
    private RecyclerView rvBookings;
    private BookingAdapter bookingAdapter;

    private String suggestedVehicle = "";
    private final String[] CROP_TYPES = {
        "Rice", "Wheat", "Maize", "Sugarcane", "Cotton",
        "Tomato", "Potato", "Onion", "Soybean", "Groundnut"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupCropSpinner();
        loadUserGreeting();
        loadRecentBookings();
        setupListeners();
    }

    private void initViews() {
        spinnerCropType = findViewById(R.id.spinnerCropType);
        etWeight = findViewById(R.id.etWeight);
        btnGetSuggestion = findViewById(R.id.btnGetSuggestion);
        btnProceedBook = findViewById(R.id.btnProceedBook);
        progressBar = findViewById(R.id.progressBar);
        cardSuggestion = findViewById(R.id.cardSuggestion);
        tvVehicleSuggestion = findViewById(R.id.tvVehicleSuggestion);
        tvSuggestionDesc = findViewById(R.id.tvSuggestionDesc);
        tvGreeting = findViewById(R.id.tvGreeting);
        rvBookings = findViewById(R.id.rvBookings);

        bookingAdapter = new BookingAdapter(new ArrayList<>(), this::onTrackBooking);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(bookingAdapter);

        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupCropSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CROP_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCropType.setAdapter(adapter);
    }

    private void loadUserGreeting() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            tvGreeting.setText("Hello, " + (name != null ? name : "Farmer") + "!");
                            TextView tvUserName = findViewById(R.id.tvUserName);
                            if (tvUserName != null) tvUserName.setText(name);
                        }
                    });
        }
    }

    private void loadRecentBookings() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("bookings")
                .whereEqualTo("farmerId", user.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Booking b = doc.toObject(Booking.class);
                        b.setBookingId(doc.getId());
                        bookings.add(b);
                    }
                    bookingAdapter.updateData(bookings);
                });
    }

    private void setupListeners() {
        btnGetSuggestion.setOnClickListener(v -> fetchMLPrediction());
        btnProceedBook.setOnClickListener(v -> proceedToBooking());
    }

    private void fetchMLPrediction() {
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        if (TextUtils.isEmpty(weightStr)) {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight value", Toast.LENGTH_SHORT).show();
            return;
        }

        String crop = spinnerCropType.getSelectedItem().toString();
        setLoading(true);
        cardSuggestion.setVisibility(View.GONE);

        PredictRequest request = new PredictRequest(crop, weight);
        RetrofitClient.getInstance().getApiService().predictVehicle(request)
                .enqueue(new Callback<PredictResponse>() {
                    @Override
                    public void onResponse(Call<PredictResponse> call, Response<PredictResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            PredictResponse result = response.body();
                            suggestedVehicle = result.getVehicleType();
                            tvVehicleSuggestion.setText(suggestedVehicle);
                            if (result.getDescription() != null && !result.getDescription().isEmpty()) {
                                tvSuggestionDesc.setText(result.getDescription());
                            }
                            cardSuggestion.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(FarmerHomeActivity.this,
                                    "Could not get suggestion. Check API.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PredictResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(FarmerHomeActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToBooking() {
        String crop = spinnerCropType.getSelectedItem().toString();
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "0";

        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("cropType", crop);
        intent.putExtra("weight", weightStr);
        intent.putExtra("vehicleType", suggestedVehicle);
        startActivity(intent);
    }

    private void onTrackBooking(Booking booking) {
        if ("accepted".equals(booking.getStatus()) && booking.getDriverId() != null) {
            Intent intent = new Intent(this, TrackingActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            intent.putExtra("driverId", booking.getDriverId());
            intent.putExtra("cropType", booking.getCropType());
            intent.putExtra("vehicleType", booking.getVehicleType());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Waiting for driver to accept...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGetSuggestion.setEnabled(!loading);
        btnGetSuggestion.setText(loading ? "Fetching..." : getString(R.string.btn_get_suggestion));
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentBookings();
    }
}
