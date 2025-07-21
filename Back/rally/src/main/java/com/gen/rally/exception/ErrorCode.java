package com.gen.rally.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "이미 존재하는 아이디입니다."),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND("USER_NOT_FOUND", "해당 유저를 찾을 수 없습니다."),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    INVALID_STATE("INVALID_STATE","유효하지 않은 요청입니다."),
    INVALID_KAKAO_CODE("INVALID_KAKAO_CODE", "카카오 인가 코드가 유효하지 않습니다."),
    INVALID_NAVER_CODE("INVALID_NAVER_CODE","네이버 인가 코드가 유효하지 않습니다."),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
