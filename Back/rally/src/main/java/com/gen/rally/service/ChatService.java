package com.gen.rally.service;

import com.gen.rally.dto.ChatMessageDto;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final SimpMessagingTemplate template;
    private final ChatRoomRepository chatRoomRepository;
    private final GameRepository gameRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    // 채팅방 개설
    @Transactional
    public ChatRoom createRoom(Long gameId, Long userId) {
        Game game = gameRepository.findByGameId(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));
        // TODO: 경기와 맞는 사용자인지 검증
        return chatRoomRepository.findByGame_GameId(gameId)
                .orElseGet(()->chatRoomRepository.save(ChatRoom.create(game)));
    }


    // 메시지 전송
    @Transactional
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
