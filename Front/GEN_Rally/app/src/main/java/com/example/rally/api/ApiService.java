package com.example.rally.api;

import com.example.rally.dto.CandidateResponseDto;
import com.example.rally.dto.ChatMessageDto;
import com.example.rally.dto.ChatRoomDto;
import com.example.rally.dto.ChatRoomListDto;
import com.example.rally.dto.CheckIdRequest;
import com.example.rally.dto.CheckNicknameResponse;
import com.example.rally.dto.EvaluationCreateRequest;
import com.example.rally.dto.GeneralLoginRequest;
import com.example.rally.dto.GeneralLoginResponse;
import com.example.rally.dto.GeneralSignupRequest;
import com.example.rally.dto.GoalActiveItem;
import com.example.rally.dto.GoalCreateRequest;
import com.example.rally.dto.InvitationAcceptRequest;
import com.example.rally.dto.InvitationAcceptResponse;
import com.example.rally.dto.InvitationItem;
import com.example.rally.dto.MatchConfirmDto;
import com.example.rally.dto.InvitationRefuseRequest;
import com.example.rally.dto.MatchFoundItem;
import com.example.rally.dto.MatchInfoDto;
import com.example.rally.dto.MatchInvite;
import com.example.rally.dto.MatchInviteResponse;
import com.example.rally.dto.MatchRequestDetails;
import com.example.rally.dto.MatchRequestDto;
import com.example.rally.dto.MatchRequestInfoDto;
import com.example.rally.dto.MatchSeekingItem;
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
import retrofit2.http.PUT;
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

    @Headers("Requires-Auth: true")
    @GET("/api/home")
    Call<List<MatchInfoDto>> getHome();

    @Headers("Requires-Auth: true")
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

    @Headers("Requires-Auth: true")
    @PUT("/api/rooms/match-confirm")
    Call<ResponseBody> confirmMatch(@Body MatchConfirmDto dto);

    @Headers("Requires-Auth: true")
    @POST("/api/invitation/invite")
    Call<MatchInviteResponse> sendInvitation( @Body MatchInvite body);

    @Headers("Requires-Auth: true")
    @GET("/api/invitation/received")
    Call<List<InvitationItem>> getReceivedInvitations();

    @Headers("Requires-Auth: true")
    @GET("/api/invitation/sent")
    Call<List<InvitationItem>> getSentInvitations();

    @Headers("Requires-Auth: true")
    @GET("/api/match/found")
    Call<List<MatchFoundItem>> getFoundMatches();
    @Headers("Requires-Auth: true")
    @GET("/api/match/seeking")
    Call<List<MatchSeekingItem>> getSeekingMatches ();

    @Headers("Requires-Auth: true")
    @GET("/api/match/details")
    Call<MatchRequestDetails> getMatchRequestDetails(@Query("myRequestId") Long myRequestId, @Query("opponentRequestId") Long opponentRequestId );

    @Headers("Requires-Auth: true")
    @POST("/api/match/request")
    Call<Long> requestMatch(@Body MatchRequestDto requestDto);

    @Headers("Requires-Auth: true")
    @POST("/api/match/cancel")
    Call<ResponseBody> cancelMatchRequest(@Body Long requestId);

    @Headers("Requires-Auth: true")
    @POST("/api/match/candidates")
    Call<List<CandidateResponseDto>> getCandidates(@Body Long requestId);

    @Headers("Requires-Auth: true")
    @POST("/api/invitation/accept")
    Call<InvitationAcceptResponse> acceptInvitation(@Body InvitationAcceptRequest body);

    @Headers("Requires-Auth: true")
    @POST("/api/invitation/refuse")
    Call<ResponseBody> refuseInvitation(@Body InvitationRefuseRequest body);

    @Headers("Requires-Auth: true")
    @POST("/api/game/cancel")
    Call<ResponseBody> cancelGame(@Body Long gameId);

    @Headers("Requires-Auth:true")
    @POST("/api/goal/create")
    Call<ResponseBody> createGoal(@Body GoalCreateRequest body);

    @Headers("Requires-Auth:true")
    @POST("/api/goal/active")
    Call<List<GoalActiveItem>> getActiveGoals();

    @Headers("Requires-Auth:true")
    @POST("/api/goal/check")
    Call<Void> checkGoals(@Body List<Long> goalIds);
}
