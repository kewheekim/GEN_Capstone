package com.example.rally.dto;

public class MatchInvite{
    public Long senderRequestId;
    public Long receiverRequestId;
    public String receiverId;

    public MatchInvite (Long senderRequestId, Long receiverRequestId, String receiverId) {
        this.senderRequestId = senderRequestId;
        this.receiverRequestId = receiverRequestId;
        this.receiverId = receiverId;
    }
}