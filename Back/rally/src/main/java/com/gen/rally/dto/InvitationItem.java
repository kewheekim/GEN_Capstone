package com.gen.rally.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvitationItem {
    private Long invitationId;
    private Long myRequestId;
    private Long opponentRequestId;
    private String date;
    private String gameType;
    private String opponentId;
    private String opponentName;
    private String opponentProfileImage;
    private String state;
    private String refusal;

    public Long getInvitationId() { return invitationId; }
    public Long getMyRequestId() { return myRequestId; }
    public Long getOpponentRequestId() { return opponentRequestId; }
    public String getDate() { return date; }
    public String getGameType() { return gameType; }
    public String getOpponentId() { return opponentId; }
    public String getOpponentProfileImage() { return opponentProfileImage; }
    public String getOpponentName() { return opponentName; }
    public String getState() { return state; }
    public String getRefusal() { return refusal; }
}