package com.example.rally.dto;

import lombok.Setter;

@Setter
public class GeneralSignupRequest {
    private String userId;
    private String password;
    private String name;
    private String gender;
    private String imageUrl;
    private String primaryThing;
}
