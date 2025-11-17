package com.gen.rally.repository;

import com.gen.rally.entity.GameScore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameScoreRepository extends JpaRepository<GameScore, Long> {
    GameScore findByGame_GameId(Long gameId);
}
