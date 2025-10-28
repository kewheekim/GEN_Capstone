package com.gen.rally.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor @NoArgsConstructor
public class InvitationAcceptResponse {
    Long gameId;
    Long roomId;
    String userProfile;
    String opponentProfile;
    String opponentName;
}
