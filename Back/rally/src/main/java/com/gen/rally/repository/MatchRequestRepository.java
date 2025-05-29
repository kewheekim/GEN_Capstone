package com.gen.rally.repository;

import com.gen.rally.entity.GameType;
import com.gen.rally.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    // Spring Data JPA 네이밍 규칙에 맞는 쿼리 메서드 정의
    List<MatchRequest> findByGameDateAndGameTypeAndSameGender(
            LocalDate gameDate,
            GameType gameType,
            boolean sameGender
    );
}
