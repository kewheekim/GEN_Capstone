package com.example.rally.dto;

public class InvitationAcceptResponse {
    private Long gameId;
    private Long roomId;
    private String userProfile;
    private String opponentProfile;
    private String opponentName;

    public Long getGameId() { return gameId; }
    public Long getRoomId() { return roomId; }
    public String getUserProfile() {return userProfile; }
    public String getOpponentProfile() { return  opponentProfile;}
    public String getOpponentName() { return opponentName; }
}
