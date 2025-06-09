package com.gen.rally.dto;

import com.gen.rally.entity.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

// 추천 후보 반환 형식
@Getter
@Setter
public class CandidatesResponseDto {
    private String userId;
    private String name;
    private String profileImage;
    private int gender;
    private int tier;
    private boolean isSameTier;     // 동일한 티어인지

    private double winningRate;      // 최근 5경기 승률 (단식)
    private int skillGap;// 팀원 간 실력 차이 정도 (복식)

    private String time;// 시간대  (16:00~20:00)
    private boolean isSameTime; // 시간대 동일 여부
    private String place;       // 장소 (좌표X 장소 이름 반환)
    private boolean isSamePlace;    // 동일 장소 여부
    private double distance;      // 거리 얼마나 떨어져있는지 (km)

    private int gameStyle;   // 경기  스타일
    private double mannerScore;

    // MatchRequest 엔티티 -> CandidatesResponseDto로 변환
    public CandidatesResponseDto(MatchRequest request, MatchRequestCreateDto userInput, double distance, double winningRate, int skillGap, boolean isSameTier) {
        this.userId = request.getUser().getUserId();
        this.name = request.getUser().getName();
        this.profileImage = request.getUser().getProfileImageUrl();
        this.gender= request.getGender();
        this.tier=request.getUser().getTier().getCode();
        this.isSameTier=isSameTier;

        if(request.getGameType().getCode() == 0) {
            //단식
            this.winningRate=winningRate;
            this.skillGap=0;
        }
        else{
            // 복식
            this.winningRate=0;
            this.skillGap= skillGap;
        }

        this.time= request.getStartTime() + ":00~" + request.getEndTime() + ":00";
        this.isSameTime=(request.getStartTime() == userInput.getStartTime()) &&
                (request.getEndTime() == userInput.getEndTime());

        this.place = request.getPlace();
        this.isSamePlace=request.getPlace().equals(userInput.getPlace());
        this.distance=distance;
        this.gameStyle = request.getGameStyle().getCode();
        this.mannerScore=request.getUser().getManner();
    }

}



