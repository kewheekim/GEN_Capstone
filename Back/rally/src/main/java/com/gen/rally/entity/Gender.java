package com.gen.rally.entity;

import java.util.Arrays;

public enum Gender {
    남성(0), 여성(1), 혼성(2);

    private final int code;
    Gender(int code) { this.code = code; }

    public static Gender fromCode(int code) {
        return Arrays.stream(Gender.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Gender code: " + code));
    }
    public int getCode() { return code; }
}
