package com.gen.rally.service;

import com.gen.rally.dto.GameHealthRequest;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.GameHealth;
import com.gen.rally.entity.User;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.repository.GameHealthRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameHealthService {
    private final GameHealthRepository gameHealthRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveHealth(Long userId, GameHealthRequest req) {
        Game game = gameRepository.findById(req.getGameId())
                .orElseThrow(()-> new CustomException(ErrorCode.GAME_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        GameHealth e = new GameHealth();
        e.setGame(game);
        e.setUser(user);
        e.setSteps(req.getSteps());
        e.setMaxHr(req.getMaxHr());
        e.setMinHr(req.getMinHr());
        e.setCalories(req.getCalories());
        e.setSeriesHr(req.getSeriesHr());

        gameHealthRepository.save(e);
    }
}
