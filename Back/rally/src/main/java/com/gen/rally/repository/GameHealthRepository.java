package com.gen.rally.repository;

import com.gen.rally.entity.GameHealth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameHealthRepository extends JpaRepository<GameHealth, Long> {
   GameHealth findByGame_GameIdAndUser_UserId(Long  gameId, String userId);
}
