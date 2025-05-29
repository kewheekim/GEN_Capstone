package com.gen.rally.dto;

import com.gen.rally.entity.GameStyle;
import com.gen.rally.entity.GameType;
import com.gen.rally.entity.Gender;
import com.gen.rally.entity.Tier;
import lombok.Builder;
import lombok.Getter;

import java.util.Base64;

// 추천 후보 반환 형식
@Getter
@Builder
public class CandidatesResponseDto {
    private String name;
    private String profileImage;
    private Gender gender;
    private Tier tier;

    private double winningRate;      // 최근 5경기 승률 (단식)
    private int skillGap;// 팀원 간 실력 차이 정도 (복식)

    private String time;// 시간대   16~20시
    private boolean isSameTime; // 시간대 동일 여부
    private String place;// 장소 (좌표 말고 장소 이름으로 반환)
    private boolean isSamePlace;// 장소 동일 여부

    private GameStyle gameStyle; // 경기  스타일
    private double mannerScore;
}
