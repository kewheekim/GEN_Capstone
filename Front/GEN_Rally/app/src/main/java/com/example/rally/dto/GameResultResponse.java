package com.example.rally.dto;

import java.util.List;

public class GameResultResponse {
    public GameInfoDto game;
    public List<SetResultDto> sets;
    public GameHealthDto health;
    public String comment;
}
