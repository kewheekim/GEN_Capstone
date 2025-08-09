package com.example.rally.dto;

import lombok.Getter;

@Getter
public class CheckIdRequest {
    private String userId;
    public CheckIdRequest(String userId) { this.userId = userId; }
}
