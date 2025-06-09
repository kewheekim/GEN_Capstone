package com.example.rally.api;
import com.example.rally.dto.CandidateResponseDto;
import com.example.rally.dto.MatchRequestDto;

import java.util.List;

import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.Call;

public interface MatchService {
    @POST("/api/match/request")
    Call<List<CandidateResponseDto>> requestMatch(@Body MatchRequestDto requestDto);
}
