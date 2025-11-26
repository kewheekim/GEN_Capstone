package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class GameResultResponse {
    private GameInfoDto game;
    private List<SetResultDto> sets;
    private GameHealthDto health;
    private String comment;
}
