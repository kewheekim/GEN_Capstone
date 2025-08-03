package com.gen.rally.enums;

import java.util.Arrays;

public enum Primary {
    실력(0), 위치(1), 시간(2), 스타일(3);

    private final int code;
    Primary(int code) {this.code = code;}

    public static Primary fromCode(int code) {
        return Arrays.stream(Primary.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Primary code: " + code));
    }
}
