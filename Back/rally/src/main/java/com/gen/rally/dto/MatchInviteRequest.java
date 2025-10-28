package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MatchInviteRequest {
    private Long senderRequestId;
    private Long receiverRequestId;
    private String receiverId;
}