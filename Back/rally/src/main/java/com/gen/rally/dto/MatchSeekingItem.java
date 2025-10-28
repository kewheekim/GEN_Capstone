package com.gen.rally.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchSeekingItem {
    private Long requestId;
    private String date;
    private String gameType;
    private String time;
    private String place;
    private String state;
    private String createdAt;
}
