package com.gen.rally.enums;

import java.util.Arrays;

public enum Tier {
    입문자1(0), 입문자2(1),  입문자3(2), 초보자1(3), 초보자2(4), 초보자3(5),
    중급자1(6), 중급자2(7), 중급자3(8), 상급자1(9), 상급자2(10), 상급자3(11);

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