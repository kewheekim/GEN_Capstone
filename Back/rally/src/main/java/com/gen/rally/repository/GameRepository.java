package com.gen.rally.repository;
import com.gen.rally.entity.Game;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository  extends JpaRepository<Game, Long> {
    @Query("""
    SELECT g FROM Game g
    WHERE (g.user1.userId = :userId OR g.user2.userId = :userId) AND g.state=1
    ORDER BY g.date DESC
""")
    List<Game> findRecentGamesByUserId(@Param("userId") String userId, Pageable pageable);
    Optional<Game> findByGameId(Long gameId);
}
