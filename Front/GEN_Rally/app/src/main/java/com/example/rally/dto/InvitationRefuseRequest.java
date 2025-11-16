package com.example.rally.dto;

public class InvitationRefuseRequest {
    private Long invitationId;
    private String refusal;

    public InvitationRefuseRequest(Long invitationId, String refusal) {
        this.invitationId = invitationId;
        this.refusal = refusal;
    }
}
