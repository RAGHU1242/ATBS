package com.agrigo.app.network;

import com.google.gson.annotations.SerializedName;

public class HealthResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("model_loaded")
    private boolean modelLoaded;

    public String getStatus() { return status; }
    public boolean isModelLoaded() { return modelLoaded; }
}
