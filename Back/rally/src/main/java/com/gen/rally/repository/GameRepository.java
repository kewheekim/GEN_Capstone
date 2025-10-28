package com.gen.rally.repository;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.MatchRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository  extends JpaRepository<Game, Long> {
    @Query("""
    SELECT g FROM Game g
    WHERE (g.user1.userId = :userId OR g.user2.userId = :userId) AND g.state= com.gen.rally.enums.State.경기확정
    ORDER BY g.date DESC
""")
    List<Game> findRecentGamesByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("""
    SELECT g FROM Game g
    WHERE (g.user1.userId = :userId OR g.user2.userId = :userId)
    AND g.state IN (com.gen.rally.enums.State.수락, com.gen.rally.enums.State.경기확정)
    ORDER BY g.date DESC
""")
    List<Game> findRecentByUserAndStates(@Param("userId") String userId);

    @Query("""
SELECT g FROM Game g
WHERE (g.user1.userId = :userId OR g.user2.userId = :userId)
ORDER BY g.date DESC
""")
    List<Game> findAllByUser(@Param("userId") String userId);

    Optional<Game> findByGameId(Long gameId);

    @Query("""
           select g from Game g
            where (g.requestId1 = :r1 and g.requestId2 = :r2)
               or (g.requestId1 = :r2 and g.requestId2 = :r1)
           """)
    Optional<Game> findByRequests(MatchRequest r1, MatchRequest r2);
}
