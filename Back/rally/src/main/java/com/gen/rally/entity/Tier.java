package com.gen.rally.entity;

import java.util.Arrays;

public enum Tier {
    입문자(0), 초급자(1), 중급자 (2), 상급자(3);

    private final int code;
    Tier(int code) {
        this.code = code;
    }
    public static Tier fromCode(int code) {
        return Arrays.stream(Tier.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Tier code: " + code));
    }
    public int getCode() {
        return code;
    }
}