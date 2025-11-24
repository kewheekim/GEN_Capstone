package com.gen.rally.service;

import com.gen.rally.dto.GoalCreateRequest;
import com.gen.rally.dto.GoalItem;
import com.gen.rally.entity.Goal;
import com.gen.rally.entity.User;
import com.gen.rally.enums.GoalType;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.GoalRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createGoal(GoalCreateRequest req, String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Goal goal = new Goal();
        goal.setUser(user);
        goal.setName(req.getName());
        goal.setTheme(req.getTheme());
        goal.setGoalType(req.getType());
        goal.setCalorie(req.getCalorie());
        goal.setTargetWeeksCount(req.getTargetWeeksCount());

        if (req.getType() == GoalType.기간) {
            goal.setEndDate(LocalDate.now().plusWeeks(req.getTargetWeeksCount()));

        } else if (req.getType() == GoalType.횟수) {;
            goal.setEndDate(null);
        }

        Goal saved = goalRepository.save(goal);
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<GoalItem> getActiveGoals(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        // achieved = false 인 Goal 조회
        List<Goal> goals = goalRepository.findByUserAndAchieved(user, false);

        return goals.stream()
                .map(this::toGoalItem)
                .collect(Collectors.toList());
    }
    public void checkGoals( List<Long> goalIds) {
        for (Long goalId : goalIds) {

            Goal goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new CustomException(ErrorCode.GOAL_NOT_FOUND));

            // progressCount +1
            int newProgress = (goal.getProgressCount() == null ? 1 : goal.getProgressCount() + 1);
            goal.setProgressCount(newProgress);

            // 횟수 목표 달성 판정
            if ("횟수".equals(goal.getGoalType()) &&
                    goal.getTargetWeeksCount() != null &&
                    newProgress >= goal.getTargetWeeksCount()) {
                goal.setAchieved(true);
            }
        }
    }
    private GoalItem toGoalItem(Goal goal) {
        return new GoalItem(
                goal.getId(),
                goal.getName(),
                goal.getTheme().name(),
                goal.getGoalType().name(),
                goal.getTargetWeeksCount(),
                goal.getProgressCount()
        );
    }
}

