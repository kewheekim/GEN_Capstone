package com.example.rally.api;

import com.example.rally.dto.CheckIdRequest;
import com.example.rally.dto.CheckNicknameResponse;
import com.example.rally.dto.GeneralSignupRequest;
import com.example.rally.dto.SignupResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/users/signup")
    Call<SignupResponse> signup(@Body GeneralSignupRequest request);
    @POST("/api/users/check-id")
    Call<ResponseBody> checkUserId(@Body CheckIdRequest request);

    @GET("/api/users/check-nickname")
    Call<CheckNicknameResponse> checkNickname(@Query("nickname") String nickname);
}
