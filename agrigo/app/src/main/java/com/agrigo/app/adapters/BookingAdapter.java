package com.agrigo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.agrigo.app.R;
import com.agrigo.app.models.Booking;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(Booking booking);
    }

    private List<Booking> items;
    private final OnTrackClickListener listener;

    public BookingAdapter(List<Booking> items, OnTrackClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<Booking> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking b = items.get(position);
        holder.tvBookingId.setText("Booking #" + shortenId(b.getBookingId()));
        holder.tvCropWeight.setText(b.getCropType() + " · " + b.getWeight() + " kg");
        holder.tvVehicleType.setText("🚛 " + (b.getVehicleType() != null ? b.getVehicleType() : "—"));

        String status = b.getStatus() != null ? b.getStatus().toUpperCase() : "UNKNOWN";
        holder.tvBookingStatus.setText(status);

        if ("accepted".equalsIgnoreCase(b.getStatus())) {
            holder.tvBookingStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.statusAccepted));
            holder.tvBookingStatus.setBackgroundResource(R.drawable.bg_suggestion_card);
            holder.tvTrackBooking.setVisibility(View.VISIBLE);
            holder.tvTrackBooking.setOnClickListener(v -> listener.onTrackClick(b));
        } else {
            holder.tvBookingStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.statusRequested));
            holder.tvBookingStatus.setBackgroundResource(R.drawable.bg_request_card);
            holder.tvTrackBooking.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String shortenId(String id) {
        if (id == null) return "—";
        return id.length() > 6 ? id.substring(0, 6).toUpperCase() : id.toUpperCase();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvCropWeight, tvVehicleType, tvBookingStatus, tvTrackBooking;

        ViewHolder(View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvCropWeight = itemView.findViewById(R.id.tvCropWeight);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvTrackBooking = itemView.findViewById(R.id.btnTrackBooking);
        }
    }
}
