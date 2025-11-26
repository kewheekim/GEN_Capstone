package com.gen.rally.repository;

import com.gen.rally.entity.GameScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameScoreRepository extends JpaRepository<GameScore, Long> {
    Optional<GameScore> findByGame_GameId(Long gameId);
}
