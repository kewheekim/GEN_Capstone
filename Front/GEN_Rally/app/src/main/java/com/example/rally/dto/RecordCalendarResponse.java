package com.example.rally.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class RecordCalendarResponse {
    private List<String> markedDates;
    private List<GameReviewDto> games;
}
