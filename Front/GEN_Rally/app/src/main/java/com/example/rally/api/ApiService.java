package com.example.rally.api;

import com.example.rally.dto.ChatMessageDto;
import com.example.rally.dto.ChatRoomDto;
import com.example.rally.dto.ChatRoomListDto;
import com.example.rally.dto.CheckIdRequest;
import com.example.rally.dto.CheckNicknameResponse;
import com.example.rally.dto.EvaluationCreateRequest;
import com.example.rally.dto.GeneralLoginRequest;
import com.example.rally.dto.GeneralLoginResponse;
import com.example.rally.dto.GeneralSignupRequest;
import com.example.rally.dto.SignupResponse;
import com.example.rally.dto.TierAssessRequest;
import com.example.rally.dto.TierAssessResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/users/signup")
    Call<SignupResponse> signup(@Body GeneralSignupRequest request);

    @POST("/api/users/login")
    Call<GeneralLoginResponse> login(@Body GeneralLoginRequest request);

    @POST("/api/users/check-id")
    Call<ResponseBody> checkUserId(@Body CheckIdRequest request);

    @GET("/api/users/check-nickname")
    Call<CheckNicknameResponse> checkNickname(@Query("nickname") String nickname);

    @Headers("Requires-Auth: true")
    @POST("/api/users/tier")
    Call<TierAssessResponse> getTier(@Body TierAssessRequest request);
    @POST("/api/evaluation")
    Call<Void> createEvaluation(@Body EvaluationCreateRequest body);

    @Headers("Requires-Auth: true")
    @GET("/api/rooms")
    Call<List<ChatRoomListDto>> getAllChatRooms();

    @Headers("Requires-Auth: true")
    @GET("/api/rooms/{roomId}/participants")
    Call<ChatRoomDto> getChatRoomInfo(@Path("roomId") Long roomId);

    @Headers("Requires-Auth: true")
    @GET("/api/rooms/{roomId}/messages")
    Call<List<ChatMessageDto>> loadMessages(@Path("roomId") Long roomId);

    @Headers("Requires-Auth: true")
    @POST("/api/rooms/{roomId}/read")
    Call<ResponseBody> markChatRoomAsRead(@Path("roomId") Long roomId);
}
