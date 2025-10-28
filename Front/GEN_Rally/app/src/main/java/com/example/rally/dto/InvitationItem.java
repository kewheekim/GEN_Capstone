package com.example.rally.dto;

public class InvitationItem {
    private Long invitationId;
    private Long myRequestId;
    private Long opponentRequestId;
    private String date;
    private String gameType;

    private String opponentId;
    private String opponentProfileImage;
    private String opponentName;
    private String state;
    private String refusal;

    public InvitationItem(Long invitationId, Long myRequestId, Long opponentRequestId, String date, String gameType,
                          String opponentId, String opponentName, String opponentProfileImage, String state, String refusal) {
        this.invitationId = invitationId;
        this.state = state;
        this.myRequestId = myRequestId;
        this.opponentRequestId = opponentRequestId;
        this.date = date;
        this.gameType = gameType;
        this.opponentId = opponentId;
        this.opponentName = opponentName;
        this.opponentProfileImage = opponentProfileImage;
        this.refusal = refusal;
    }
    public Long getInvitationId() {return invitationId; }
    public Long getMyRequestId() { return myRequestId; }
    public Long getOpponentRequestId() { return opponentRequestId; }
    public String getDate() { return date;}
    public String getGameType() { return gameType; }
    public String getOpponentId() {return opponentId;}
    public String getOpponentProfileImage() {return opponentProfileImage;}
    public String getOpponentName() {return opponentName;}
    public String getState() { return state; }
    public String getRefusal() { return refusal; }
}
