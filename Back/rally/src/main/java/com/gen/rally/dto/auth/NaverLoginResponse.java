package com.gen.rally.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NaverLoginResponse {
    private TokenResponse token;
    @JsonProperty("isNew")
    private boolean isNew;
    private String nickname;
    private String socialId;
}
