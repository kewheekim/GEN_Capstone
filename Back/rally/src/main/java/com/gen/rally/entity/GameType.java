package com.gen.rally.entity;

import java.util.Arrays;

public enum GameType {
    단식 (0), 복식 (1);

    private final int code;
    GameType(int code) { this.code = code; }

    public static GameType fromCode(int code) {
        return Arrays.stream(GameType.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow();
    }
    public int getCode() { return code; }
}