package com.agrigo.app.models;

import com.google.firebase.Timestamp;

public class Booking {
    private String bookingId;
    private String farmerId;
    private String driverId;
    private String cropType;
    private double weight;
    private String vehicleType;
    private String status; // "requested", "accepted", "completed", "cancelled"
    private String notes;
    private Timestamp createdAt;

    public Booking() {}

    public Booking(String farmerId, String cropType, double weight, String vehicleType) {
        this.farmerId = farmerId;
        this.cropType = cropType;
        this.weight = weight;
        this.vehicleType = vehicleType;
        this.status = "requested";
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
