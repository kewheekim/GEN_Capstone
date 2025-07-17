package com.gen.rally.dto;

import com.gen.rally.enums.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialSignupRequest {
    private String socialId;
    private String name;
    private byte[] profileImage;
    private Gender gender;
}
