package com.gen.rally.repository;

import com.gen.rally.entity.ChatRoom;
import com.gen.rally.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findById(Long id);
    Optional<ChatRoom> findByGame_GameId(Long gameId);
    List<ChatRoom> findAllByUser1_IdOrUser2_Id(Long userId, Long userId2);
}
