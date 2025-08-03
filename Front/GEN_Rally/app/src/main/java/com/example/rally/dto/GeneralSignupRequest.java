package com.example.rally.dto;

import lombok.Setter;

@Setter
public class GeneralSignupRequest {
    private String userId;
    private String password;
    private String nickname;
    private String gender;
    private byte[] profileImage;
    private String primaryThing;
}
