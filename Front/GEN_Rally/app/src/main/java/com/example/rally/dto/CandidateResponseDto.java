package com.example.rally.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Getter;

@Getter
public class CandidateResponseDto  implements Serializable {
    private String userId;
    private String name;
    private String profileImage;
    private int gender;
    private int tier;
    @SerializedName("sameTier")
    private boolean sameTier;

    private double winningRate;      // 최근 5경기 승률 (단식)
    private int skillGap;     // 팀원 간 실력 차이 정도 (복식)

    private String time;
    @SerializedName("sameTime")
    private boolean sameTime;
    private String place;
    @SerializedName("samePlace")
    private boolean samePlace;
    private double distance;

    private int gameStyle;
    private double mannerScore;
}
