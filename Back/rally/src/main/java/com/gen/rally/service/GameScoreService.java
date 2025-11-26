package com.gen.rally.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.GameScore;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.repository.GameScoreRepository;
import com.gen.rally.websocket.model.GameState;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameScoreService {

    private final GameRepository gameRepository;
    private final GameScoreRepository gameScoreRepository;
    private final ObjectMapper mapper;

    @Transactional
    public void saveFromGameState(Long gameId, GameState st, ArrayNode setsArray, long totalElapsedSec) throws Exception {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        GameScore score = new GameScore();
        score.setGame(game);
        score.setUser1Sets(st.user1Sets);
        score.setUser2Sets(st.user2Sets);
        score.setTotalElapsedSec((int) totalElapsedSec);

        // 세트별 점수, 시간
        String setsJsonStr = mapper.writeValueAsString(setsArray);
        score.setSetsJson(setsJsonStr);

        gameScoreRepository.save(score);

        // Game의 winner 저장 -> 삭제할지 고민
        if (st.user1Sets > st.user2Sets) {
            game.setWinner(game.getUser1());
        } else if (st.user2Sets > st.user1Sets) {
            game.setWinner(game.getUser2());
        }
    }
}
