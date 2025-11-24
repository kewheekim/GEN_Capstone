package com.gen.rally.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalItem {
    private Long id;
    private String name;
    private String theme;
    private String type;
    private Integer targetWeeksCount;  // 목표 기간 또는 횟수
    private Integer progressCount = 0;  // 실천 횟수
}
