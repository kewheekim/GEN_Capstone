package com.gen.rally.dto;

import com.gen.rally.enums.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralSignupRequest {
    private String userId;
    private String password;
    private String passwordConfirm;
    private String name;
    private byte[] profileImage;
    private Gender gender;
}
