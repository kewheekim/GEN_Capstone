package com.gen.rally.enums;

import java.util.Arrays;

public enum MessageType {
    TEXT(0), MATCH_CARD(1);

    private final int code;
    MessageType(int code) { this.code = code; }

    public static MessageType fromCode(int code) {
        return Arrays.stream(MessageType.values())
                .filter(g -> g.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid MessageType code: " + code));
    }
    public int getCode() { return code; }
}
