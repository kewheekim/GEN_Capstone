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
    public Set<String> appliedMsgIds = ConcurrentHashMap.newKeySet();
    public Deque<String> recentEvents = new ArrayDeque<>();
}