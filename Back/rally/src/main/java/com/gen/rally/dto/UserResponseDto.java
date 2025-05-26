package com.gen.rally.dto;

import com.gen.rally.entity.GameType;
import com.gen.rally.entity.Gender;
import com.gen.rally.entity.Tier;
import lombok.Builder;
import lombok.Getter;

// 추천 후보 클릭 시 보여지는 사용자 프로필 요청시 응답
@Getter
@Builder
public class UserResponseDto {
    private String name;
    private String profile_image;
    private Gender gender;
    private Tier tier;
    // 최근 5경기 승률 (단식)
    // 팀원 간 실력 차이 정도 (복식)

    // 시간대
    // 시간대 동일 여부
    // 장소 (좌표 말고 장소 이름으로 반환)
    // 장소 동일 여부
    // 경기 유형
    private GameType game_type;
    private double manner_score;
}
