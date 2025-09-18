package com.gen.rally.controller;

import com.gen.rally.entity.ChatRoom;
import com.gen.rally.repository.ChatRoomRepository;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;
    private final GameRepository gameRepository;

    // 채팅방 목록 조회
    @GetMapping("/api/rooms")
    @ResponseBody
    public List<ChatRoom> findAllRooms() {
        List<ChatRoom> lists = chatRoomRepository.findAll();
        return lists;
    }

    // 채팅방 개설
    @PostMapping("/api/rooms/{gameId}")
    @ResponseBody
    public ChatRoom createRoom(@PathVariable Long gameId,@RequestParam Long userId ) {
        return chatService.createRoom(gameId, userId);
    }

    // 메시지 발신 및 수신
    @MessageMapping("/dm/{gameId}")
    public void send(@DestinationVariable Long gameId,
                     @Header("senderId") Long senderId,
                     @Payload String content){
        chatService.send(gameId, senderId, content);
    }
}
