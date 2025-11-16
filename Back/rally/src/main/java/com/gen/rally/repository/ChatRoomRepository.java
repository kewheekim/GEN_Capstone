package com.gen.rally.repository;

import com.gen.rally.entity.ChatRoom;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findById(Long id);
    Optional<ChatRoom> findByGame(Game game);
    Optional<ChatRoom> findByGame_GameId(Long gameId);
    List<ChatRoom> findByGame_GameIdIn(Collection<Long> gameIds);
    List<ChatRoom> findAllByUser1_IdOrUser2_Id(Long userId, Long userId2);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ChatRoom cr where cr.game.gameId = :gameId")
    void deleteByGame_GameId(Long gameId);
}
