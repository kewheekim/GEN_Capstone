package com.gen.rally.dto;

import com.gen.rally.entity.Game;
import com.gen.rally.entity.User;
import com.gen.rally.enums.GameStyle;
import com.gen.rally.enums.GameType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameCardInfoDto {
    private String place;
    private String date;
    private String timeRange;
    private GameStyle gameStyle;
    private GameType gameType;
    private String opponentName;
    private String opponentProfileUrl;
    private String myName;
    private Long roomId;
    private Long gameId;
    private boolean isUser1;

    // Game 엔티티를 Dto로 변환
    public GameCardInfoDto(Game game, String myUserId) {
        this.place = game.getPlace();
        this.date = game.getDate() != null ? game.getDate().toString() : null;
        this.timeRange = game.getTime();
        this.gameType = game.getGameType();
        this.gameStyle = game.getGameStyle();
        this.gameId = game.getGameId();
        if (game.getChatRoom() != null) {
            this.roomId = game.getChatRoom().getId();
        }
        User opponent = null;

        if (game.getUser1().getUserId().equals(myUserId)) {
            opponent = game.getUser2();
            myName = game.getUser1().getName();
            isUser1 = true;
        } else {
            opponent = game.getUser1();
            myName = game.getUser2().getName();
            isUser1 = false;
        }

        // 상대방 정보 세팅
        if (opponent != null) {
            this.opponentName = opponent.getName();
            this.opponentProfileUrl = opponent.getImageUrl(); // 프사 getter 확인 필요
        }
    }
}
