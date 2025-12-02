package com.gen.rally.dto;

import com.gen.rally.entity.Notification;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class NotificationItem {
    private Long id;
    private String title;
    private String body;
    private String createdAt;

    public static NotificationItem from(Notification n) {
        NotificationItem dto = new NotificationItem();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setBody(n.getBody());
        dto.setCreatedAt(n.getCreatedAt().toString());
        return dto;
    }
}
