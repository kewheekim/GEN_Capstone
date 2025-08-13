package com.gen.rally.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CheckNicknameResponse {
    private boolean available;
    private String message;
}
