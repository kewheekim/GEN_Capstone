package com.gen.rally.entity;

import java.util.Arrays;

public enum GameStyle {
    상관없음 (0), 편하게(1), 열심히(2);

    private final int code;
    GameStyle(int code) { this.code = code; }

    public static GameStyle fromCode(int code) {
        return Arrays.stream(GameStyle.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow();
    }
    public int getCode() { return code; }
}