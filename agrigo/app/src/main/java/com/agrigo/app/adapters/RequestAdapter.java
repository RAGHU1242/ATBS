package com.agrigo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.agrigo.app.R;
import com.agrigo.app.models.Booking;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface OnActionListener {
        void onAction(Booking booking);
    }

    private List<Booking> items;
    private final OnActionListener acceptListener;
    private final OnActionListener declineListener;

    public RequestAdapter(List<Booking> items,
                          OnActionListener acceptListener,
                          OnActionListener declineListener) {
        this.items = items != null ? items : new ArrayList<>();
        this.acceptListener = acceptListener;
        this.declineListener = declineListener;
    }

    public void updateData(List<Booking> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Booking> getItems() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking b = items.get(position);

        holder.tvRequestId.setText("Request #" + shortenId(b.getBookingId()));
        holder.tvCropType.setText(b.getCropType() != null ? b.getCropType() : "—");
        holder.tvWeight.setText(b.getWeight() + " kg");
        holder.tvVehicleType.setText(b.getVehicleType() != null ? b.getVehicleType() : "—");

        holder.btnAccept.setOnClickListener(v -> {
            if (acceptListener != null) acceptListener.onAction(b);
        });

        holder.btnDecline.setOnClickListener(v -> {
            if (declineListener != null) declineListener.onAction(b);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String shortenId(String id) {
        if (id == null) return "—";
        return id.length() > 6 ? id.substring(0, 6).toUpperCase() : id.toUpperCase();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequestId, tvRequestTime, tvCropType, tvWeight, tvVehicleType;
        MaterialButton btnAccept, btnDecline;

        ViewHolder(View itemView) {
            super(itemView);
            tvRequestId = itemView.findViewById(R.id.tvRequestId);
            tvRequestTime = itemView.findViewById(R.id.tvRequestTime);
            tvCropType = itemView.findViewById(R.id.tvCropType);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}
