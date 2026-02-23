package com.agrigo.app.models;

public class DriverLocation {
    private String driverId;
    private double lat;
    private double lng;
    private long updatedAt;

    public DriverLocation() {}

    public DriverLocation(String driverId, double lat, double lng) {
        this.driverId = driverId;
        this.lat = lat;
        this.lng = lng;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
