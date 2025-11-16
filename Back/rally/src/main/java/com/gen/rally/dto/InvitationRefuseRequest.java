package com.gen.rally.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InvitationRefuseRequest {
    Long invitationId;
    String refusal;
}
