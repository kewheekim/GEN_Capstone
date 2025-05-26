package com.gen.rally.entity;

public enum Tier {
    입문자(0), 초급자(1), 중급자 (2), 상급자(3);

    private final int code;

    Tier(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Tier fromCode(int code) {
        for (Tier t : Tier.values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Invalid Tier code: " + code);
    }
}
