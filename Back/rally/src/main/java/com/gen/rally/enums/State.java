package com.gen.rally.enums;

import java.util.Arrays;

public enum State {
    대기(0), 요청중(1), 수락(2), 취소(3), 거절(4), 경기확정(5);

    private final int code;
    State(int code) { this.code = code; }

    public static State fromCode(int code) {
        return Arrays.stream(State.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid State code: " + code));
    }

    public int getCode() { return code; }
}
