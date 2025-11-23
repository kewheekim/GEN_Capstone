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

        if (req.getType() == GoalType.기간) {
            if (req.getTargetWeeks() == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            goal.setTargetWeeks(req.getTargetWeeks());
            goal.setTargetCount(null);
            goal.setEndDate(LocalDate.now().plusWeeks(req.getTargetWeeks()));

        } else if (req.getType() == GoalType.횟수) {
            if (req.getTargetCount() == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            goal.setTargetCount(req.getTargetCount());
            goal.setTargetWeeks(null);
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

    private GoalItem toGoalItem(Goal goal) {
        String theme = goal.getTheme() != null ? goal.getTheme().name() : null;
        return new GoalItem(
                goal.getId(),
                goal.getName(),
                theme,
                goal.getTargetWeeks(),
                goal.getTargetCount(),
                goal.getProgressCount()
        );
    }
}

