package com.gen.rally.repository;

import com.gen.rally.entity.ChatMessage;
import com.gen.rally.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findBySender(User sender);
    Optional<ChatMessage> findByChatRoomId(Long chatRoomId);
    List<ChatMessage> findAllByChatRoomId(Long chatRoomId);
    Optional<ChatMessage> findFirstByChatRoom_IdOrderByCreatedAtDesc(Long chatRoomId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.createdAt > :lastReadAt") // 안 읽은 메시지 조회
    int countUnreadMessages(@Param("roomId") Long roomId, @Param("lastReadAt") LocalDateTime lastReadAt);
}
