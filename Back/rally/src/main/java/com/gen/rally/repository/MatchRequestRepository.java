package com.gen.rally.repository;

import com.gen.rally.entity.GameType;
import com.gen.rally.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findByGameDateAndGameType(
            LocalDate gameDate,
            int gameType
    );

    @Query("""
    SELECT m FROM MatchRequest m
    WHERE m.user.userId = :userId
      AND m.gameDate = :gameDate
      AND (
           (m.startTime < :endTime AND m.endTime > :startTime)
          )
""")
    List<MatchRequest> findOverlappingRequests(String userId, LocalDate gameDate, int startTime, int endTime);
}
