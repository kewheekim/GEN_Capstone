package com.example.rally.dto;

public class MatchFoundItem {
    public final Long gameId;
    public final Long roomId;
    public final Long userId;
    public final String state;
    public final String date;
    public final String gameType;
    public final String time;
    public final String place;
    public final String opponentProfile;
    public final String opponentName;

    public MatchFoundItem( Long gameId, Long roomId, Long userId, String state, String date, String gameType, String time, String place,
                           String opponentProfile, String opponentName) {
        this.gameId = gameId;
        this.roomId = roomId;
        this.userId = userId;
        this.state = state;
        this.date = date;
        this.gameType = gameType;
        this.time = time;
        this.place = place;
        this.opponentProfile = opponentProfile;
        this.opponentName = opponentName;
    }

    public Long getGameId() { return gameId; }
    public Long getRoomId() {return roomId;}
    public Long getUserId() {return userId;}
    public String getState() { return state; }
    public String getDate() {return date; }

    public String getGameType()  { return gameType; }
    public String getTime()      { return time; }
    public String getPlace()     { return place; }
    public String getOpponentProfile() { return opponentProfile; }
    public String getOpponentName()  { return opponentName; }
}
