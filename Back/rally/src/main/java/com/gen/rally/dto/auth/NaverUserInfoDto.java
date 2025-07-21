package com.gen.rally.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NaverUserInfoDto {
    private String resultcode;
    private String message;
    private Response response; // response 필드 안에 진짜 유저 정보 .getResponse().getId() 식으로 접근

    @Getter
    public static class Response {
        private String id;
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
    }
}
