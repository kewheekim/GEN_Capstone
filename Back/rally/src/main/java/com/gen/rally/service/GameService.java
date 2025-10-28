package com.gen.rally.service;

import com.gen.rally.dto.MatchFoundItem;
import com.gen.rally.entity.Game;
import com.gen.rally.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M월 d일(E)");

    public List<MatchFoundItem> findFound(String myUserId) {
        List<Game> list = gameRepository.findAllByUser(myUserId);

        return list.stream().map(g -> {
            boolean meIsUser1 = g.getUser1() != null && myUserId.equals(g.getUser1().getUserId());
            var opponent = meIsUser1 ? g.getUser2() : g.getUser1();
            System.out.println("찾은 매칭 개수:" + list.size());
            return MatchFoundItem.builder()
                    .gameId(g.getGameId() != null ? g.getGameId() : null)
                    .opponentId(opponent != null ? opponent.getUserId() : null)
                    .opponentProfile(opponent != null ? opponent.getImageUrl() : null)
                    .opponentName(opponent != null ? opponent.getName() : null)
                    .date(g.getDate() != null ? g.getDate().format(DATE_FMT) : null)
                    .gameType(g.getGameType() != null ? g.getGameType().name() : null)
                    .time(g.getTime())
                    .place(g.getPlace())
                    .state(g.getState() != null ? g.getState().name() : null)
                    .build();
        }).toList();
    }
}
