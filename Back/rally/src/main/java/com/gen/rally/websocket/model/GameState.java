package com.gen.rally.websocket.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public int lastSeq = 0;
    public int setNumber = 1;
    public int user1Score = 0, user2Score = 0;
    public int user1Sets = 0, user2Sets = 0;
    public String currentServe = "USER1";
    // 경기 시간
    public long startAt = 0L;
    public boolean paused = true;
    public long pauseStartedAt = 0L;
    public long totalPaused = 0L;   // 누적 일시정지 시간(ms)

    public Set<String> appliedMsgIds = ConcurrentHashMap.newKeySet();
    public Deque<String> recentEvents = new ArrayDeque<>();
}