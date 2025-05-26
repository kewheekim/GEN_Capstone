package com.gen.rally.entity;

import java.util.Arrays;

public enum State {
    요청중(0), 수락(1), 취소(2);

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
