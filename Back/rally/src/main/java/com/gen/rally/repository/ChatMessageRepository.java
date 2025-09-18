package com.gen.rally.repository;

import com.gen.rally.entity.ChatMessage;
import com.gen.rally.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findBySender(User sender);
}
