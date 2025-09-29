package com.gen.rally.websocket.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public int lastSeq = 0;
    public Set<String> appliedMsgIds = ConcurrentHashMap.newKeySet();
    public int setNumber = 1;
    public int user1Score = 0, user2Score = 0;
    public int user1Sets = 0, user2Sets = 0;
    public String currentServe = "USER1";
    // 경기 시간
    public long setStartAt = 0L;
    public long startAt = 0L;
    public boolean paused = true;
    public long pauseStartedAt = 0L;
    public long totalPaused = 0L;   // 세트 별 누적 일시정지 시간(ms)
    public List<SetSummary> completedSets = new ArrayList<>();

    // 세트별 요약(점수/시간) 저장
    public static class SetSummary {
        public int setNumber;
        public int user1Score;
        public int user2Score;
        public long startAt;
        public long endAt;
        public long elapsed;

        public SetSummary(int s, int u1, int u2, long startAt, long endAt, long elapsed){
            this.setNumber = s; this.user1Score = u1; this.user2Score = u2;
            this.startAt = startAt; this.endAt = endAt; this.elapsed = elapsed;
        }
    }
    public Deque<String> recentEvents = new ArrayDeque<>();
    public int targetSetsToWin = 2; // 2선승
}