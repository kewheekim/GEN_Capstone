package com.gen.rally.dto;

import com.gen.rally.enums.Tier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TierAssessResponse {
    private double score;
    private Tier tier;
    private String nickname;
}
