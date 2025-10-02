package com.example.rally.dto;

import lombok.Getter;

@Getter
public class ChatRoomDto {
    private Long id;
    private String name;
    private String profileUrl;

    public ChatRoomDto(Long id, String name, String profileUrl) {
        this.id = id;
        this.name = name;
        this.profileUrl = profileUrl;
    }
}
