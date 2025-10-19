package com.gen.rally.entity;

import com.gen.rally.enums.MessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sender_id")
    private User sender;

    private LocalDateTime createdAt = LocalDateTime.now();

    public static ChatMessage create(ChatRoom chatRoom, User sender, String content) {
        ChatMessage message = new ChatMessage();
        message.chatRoom = chatRoom;
        message.sender = sender;
        message.content = content;
        return message;
    }
}
