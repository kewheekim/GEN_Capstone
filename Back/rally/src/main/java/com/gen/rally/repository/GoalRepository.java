package com.gen.rally.repository;

import com.gen.rally.entity.Goal;
import com.gen.rally.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    // 달성 여부로 조회
    List<Goal> findByUserAndAchieved(User user, boolean achieved);
}
