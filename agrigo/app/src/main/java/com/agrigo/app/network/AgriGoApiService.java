package com.agrigo.app.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AgriGoApiService {

    @POST("/predict")
    Call<PredictResponse> predictVehicle(@Body PredictRequest request);

    @GET("/health")
    Call<HealthResponse> healthCheck();
}
