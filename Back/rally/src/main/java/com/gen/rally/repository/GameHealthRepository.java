package com.gen.rally.repository;

import com.gen.rally.entity.GameHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GameHealthRepository extends JpaRepository<GameHealth, Long> {
   GameHealth findByGame_GameIdAndUser_UserId(Long gameId, String userId);

   @Query("SELECT gh FROM GameHealth gh " +
           "JOIN FETCH gh.game " +
           "WHERE gh.user.userId = :userId " +
           "AND gh.game.date BETWEEN :startDate AND :endDate")
   List<GameHealth> findMyHealthRecords(@Param("userId") String userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}
