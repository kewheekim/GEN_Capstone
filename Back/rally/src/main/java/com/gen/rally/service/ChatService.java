package com.gen.rally.service;

import com.gen.rally.dto.ChatMessageDto;
import com.gen.rally.dto.ChatRoomDto;
import com.gen.rally.entity.ChatMessage;
import com.gen.rally.entity.ChatRoom;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.User;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.ChatMessageRepository;
import com.gen.rally.repository.ChatRoomRepository;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class ChatService {
    private final SimpMessagingTemplate template;
    private final ChatRoomRepository chatRoomRepository;
    private final GameRepository gameRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /* 채팅방 개설
    public ChatRoom createRoom(Long gameId, Long userId) {
        Game game = gameRepository.findByGameId(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        return chatRoomRepository.findByGame_GameId(gameId)
                .orElseGet(()->chatRoomRepository.save(ChatRoom.create(game)));
    }*/

    // 채팅방 입장
    public ResponseEntity<List<ChatRoomDto>> enter(Long roomId){
        ChatRoom room = chatRoomRepository.findById(roomId)
                        .orElseThrow(()-> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
        User user1 = room.getUser1();
        User user2 = room.getUser2();

        ChatRoomDto dto1 = new ChatRoomDto(user1.getId(), user1.getName(), user1.getImageUrl());
        ChatRoomDto dto2 = new ChatRoomDto(user2.getId(), user2.getName(), user2.getImageUrl());

        List<ChatRoomDto> participants = List.of(dto1, dto2);

        return ResponseEntity.ok(participants);
    }

    // 메시지 전송
    public ChatMessage send(Long gameId, Long senderId, String content){
        if (content == null || content.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_MESSAGE);
        }
        ChatRoom room = chatRoomRepository.findByGame_GameId(gameId).orElseGet(()->{
            Game game = gameRepository.findByGameId(gameId).
                    orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));
            return chatRoomRepository.save(ChatRoom.create(game));
        });

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = ChatMessage.create(room, sender, content);

        chatMessageRepository.save(message);

        ChatMessageDto payload = ChatMessageDto.builder()
                .messageId(message.getId())
                .roomId(room.getId())
                .senderId(sender.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
        // 브로커로 전송
        template.convertAndSend("/sub/dm/" + gameId, payload);
        return message;
    }

}
