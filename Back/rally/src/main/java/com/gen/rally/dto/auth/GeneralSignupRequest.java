package com.gen.rally.dto.auth;

import com.gen.rally.enums.Gender;
import com.gen.rally.enums.Primary;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralSignupRequest {
    private String userId;
    private String password;
    private String name;
    private String imageUrl;
    private Gender gender;
    private Primary primaryThing;
}
