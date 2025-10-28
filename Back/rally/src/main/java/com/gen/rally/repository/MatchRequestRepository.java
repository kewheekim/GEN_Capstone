package com.gen.rally.repository;

import com.gen.rally.entity.MatchRequest;
import com.gen.rally.enums.GameType;
import com.gen.rally.enums.State;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findByGameDateAndGameType(
            LocalDate gameDate,
            GameType gameType
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

    @EntityGraph(attributePaths = {"user"})
    Optional<MatchRequest> findByRequestId(Long requestId);

    @Query("""
    select m from MatchRequest m
     where m.user.userId = :userId
       and m.state in :states
     order by m.createdAt desc
""")
    List<MatchRequest> findByUserAndStates(String userId, java.util.Collection<com.gen.rally.enums.State> states);
}
