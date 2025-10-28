package com.gen.rally.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchFoundItem {
    private Long gameId;
    private String opponentId;
    private String opponentProfile;
    private String opponentName;
    private String date;
    private String gameType;
    private String time;
    private String place;
    private String state;
}
