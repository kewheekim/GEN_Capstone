package com.example.rally.dto;

import java.time.LocalDate;

public class MatchSeekingItem {
    public final Long requestId;
    public final String state;
    public final String date;
    public final String gameType;
    public final String time;
    public final String place;
    public final String createdAt;

    public MatchSeekingItem(Long requestId, String date, String gameType,
                            String time, String place, String state, String createdAt) {
        this.requestId = requestId;
        this.date = date;
        this.gameType = gameType;
        this.time = time;
        this.place = place;
        this.state = state;
        this.createdAt = createdAt;
    }
    public Long getRequestId() { return requestId; }
    public String getDate() { return date; }
    public String getGameType() { return gameType; }
    public String getTime() { return time; }
    public String getPlace() { return place; }
    public String getState() { return state; }
    public String getCreatedAt() { return createdAt; }
}