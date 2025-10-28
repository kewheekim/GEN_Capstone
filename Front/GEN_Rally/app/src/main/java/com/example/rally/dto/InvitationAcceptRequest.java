package com.example.rally.dto;

public class InvitationAcceptRequest {
    private Long invitationId;
    public InvitationAcceptRequest (Long invitationId) {
        this.invitationId = invitationId;
    }
    public Long getInvitationId() { return invitationId; }
}
