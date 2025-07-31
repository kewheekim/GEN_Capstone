package com.example.rally.api;

import com.example.rally.dto.GeneralSignupRequest;
import com.example.rally.dto.SignupResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/users/signup")
    Call<SignupResponse> signup(@Body GeneralSignupRequest request);

}
