package com.agrigo.app.network;

import com.google.gson.annotations.SerializedName;

public class PredictResponse {
    @SerializedName("vehicle_type")
    private String vehicleType;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("description")
    private String description;

    @SerializedName("error")
    private String error;

    public String getVehicleType() { return vehicleType; }
    public double getConfidence() { return confidence; }
    public String getDescription() { return description; }
    public String getError() { return error; }
}
