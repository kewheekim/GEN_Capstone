package com.gen.rally.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RecordCalendarResponse {
    private Long gameId;
    private LocalDate date;
}
