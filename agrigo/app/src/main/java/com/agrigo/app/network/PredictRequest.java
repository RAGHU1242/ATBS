package com.agrigo.app.network;

import com.google.gson.annotations.SerializedName;

public class PredictRequest {
    @SerializedName("crop")
    private String crop;

    @SerializedName("weight")
    private double weight;

    public PredictRequest(String crop, double weight) {
        this.crop = crop;
        this.weight = weight;
    }

    public String getCrop() { return crop; }
    public double getWeight() { return weight; }
}
