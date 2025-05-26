package com.gen.rally.entity;

import java.util.Arrays;

public enum Gender {
    MALE(0), FEMALE(1), MIXED(2);

    private final int code;
    Gender(int code) { this.code = code; }

    public static Gender fromCode(int code) {
        return Arrays.stream(Gender.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow();
    }

    public int getCode() { return code; }
}
