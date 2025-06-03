package com.gen.rally.entity;

import java.util.Arrays;

public enum GameStyle {
    상관없음(0), 편하게(1), 열심히(2);

    private final int code;
    GameStyle(int code) { this.code = code; }

    public static GameStyle fromCode(int code) {
        return Arrays.stream(GameStyle.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid GameStyle code: " + code));
    }

    public int getCode() { return code; }

    // One-hot 인코딩
    public int[] toOneHot() {
        switch (this) {
            case 편하게: return new int[]{1, 0};
            case 열심히: return new int[]{0, 1};
            case 상관없음: return new int[]{1, 1};
            default: throw new IllegalArgumentException("Unknown GameStyle: " + this);
        }
    }
}
