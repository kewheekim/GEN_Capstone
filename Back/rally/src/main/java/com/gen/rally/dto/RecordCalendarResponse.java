package com.gen.rally.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class RecordCalendarResponse {
    private List<LocalDate> markedDates;
    private List<GameReviewDto> games;
}
