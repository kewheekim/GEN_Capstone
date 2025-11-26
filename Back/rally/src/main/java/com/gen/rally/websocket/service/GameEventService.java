package com.gen.rally.websocket.service;

import com.gen.rally.service.GameScoreService;
import com.gen.rally.websocket.bus.MessageBus;
import com.gen.rally.websocket.model.GameState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameEventService {
    private final MessageBus messageBus;
    private final GameScoreService gameScoreService;
    public GameEventService(MessageBus messageBus, GameScoreService gameScoreService) {
        this.messageBus = messageBus;
        this.gameScoreService = gameScoreService;
    }
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Long, GameState> games = new ConcurrentHashMap<>();

    public boolean applyIfValid(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);
            String type = text(root, "type");
            Long gameId = root.get("gameId").asLong(-1L);
            String clientMsgId = text(root, "clientMsgId");
            int seq = root.has("seq") ? root.get("seq").asInt() : 0;
            if (gameId == null || clientMsgId == null || type == null) return false;

            GameState st = games.computeIfAbsent(gameId, k -> new GameState());
            if (st.appliedMsgIds.contains(clientMsgId)) return false; // 멱등
            if (seq != 0 && seq != st.lastSeq + 1) return false;   // 순서체크

            switch (type) {
                case "set_start" -> {
                    JsonNode p = root.path("payload");
                    int setNum = p.path("setNumber").asInt(st.setNumber);
                    String firstServer = p.path("firstServer").asText("USER1");
                    long startAt = p.path("startAt").asLong(System.currentTimeMillis());

                    st.setNumber = setNum;
                    st.currentServe = firstServer; // 다음 득점 전까지의 선서브
                    st.user1Score = 0;
                    st.user2Score = 0;
                    st.setStartAt = startAt;
                    if (st.startAt == 0L) { st.startAt = startAt; }
                    st.paused = false;
                    st.pauseStartedAt = 0L;
                    st.totalPaused = 0L;
                }
                case "score_add" -> {
                    String to = root.path("payload").path("scoreTo").asText();
                    if ("user1".equals(to)) st.user1Score++; else if ("user2".equals(to)) st.user2Score++;
                }
                case "score_undo" -> {
                    String from = root.path("payload").path("from").asText("user1");
                    if ("user1".equals(from) && st.user1Score > 0) st.user1Score--;
                    else if ("user2".equals(from) && st.user2Score > 0) st.user2Score--;
                }
                case "set_pause" -> {
                    if (st.paused) break; // 이미 정지면 무시
                    long at = root.path("payload").path("pausedAt")
                            .asLong(root.path("payload").path("timeStamp")
                            .asLong(System.currentTimeMillis()));
                    st.paused = true;
                    st.pauseStartedAt = at;
                }
                case "set_resume" -> {
                    if (!st.paused) break; // 이미 재개면 무시
                    long at = root.path("payload").path("resumedAt")
                            .asLong(root.path("payload").path("timeStamp")
                            .asLong(System.currentTimeMillis()));
                    if (st.pauseStartedAt > 0L) {
                        st.totalPaused += Math.max(0L, at - st.pauseStartedAt);
                    }
                    st.paused = false;
                    st.pauseStartedAt = 0L;
                }
                case "set_finish" -> {
                    String winner = root.path("payload").path("winner").asText();
                    long finishedAt = root.path("eventTime").asLong(System.currentTimeMillis());

                    if(st.setStartAt == 0L) st.setStartAt = finishedAt;
                    long elapsedMs = Math.max(0L, finishedAt - st.setStartAt - st.totalPaused);
                    long elapsedSec = elapsedMs / 1000L;
                    // 세트 요약 저장
                    st.completedSets.add(
                            new GameState.SetSummary(st.setNumber, st.user1Score, st.user2Score, st.setStartAt, finishedAt, elapsedSec)
                    );

                    if ("user1".equals(winner)) st.user1Sets++; else st.user2Sets++;
                    st.setNumber++;
                    st.user1Score = st.user2Score = 0;

                    st.setStartAt = 0L;
                    st.paused = true;
                    st.pauseStartedAt = 0L;
                    st.totalPaused = 0L;

                    // 게임 종료 판단
                    if(st.user1Sets >=2 || st.user2Sets >=2) {
                        ObjectNode gf = mapper.createObjectNode();
                        gf.put("path", "/rally/event/game_finish");
                        gf.put("type", "game_finish");
                        gf.put("gameId", gameId);

                        ObjectNode p = mapper.createObjectNode();
                        p.put("winner", (st.user1Sets > st.user2Sets) ? "user1" : "user2");
                        p.put("user1Sets", st.user1Sets);
                        p.put("user2Sets", st.user2Sets);
                        p.put("setNumberFinished", st.setNumber - 1);

                        //  세트별 점수, 시간
                        var arr = mapper.createArrayNode();
                        long total = 0L;
                        for (var ss : st.completedSets) {
                            var s = mapper.createObjectNode();
                            s.put("setNumber", ss.setNumber);
                            s.put("user1Score", ss.user1Score);
                            s.put("user2Score", ss.user2Score);
                            s.put("elapsedSec", ss.elapsed);
                            arr.add(s);
                            total += ss.elapsed;
                        }
                        p.set("sets", arr);
                        p.put("totalElapsedSec", total);

                        gf.set("payload", p);
                        messageBus.publish("/topic/game."+ gameId, mapper.writeValueAsString(gf));

                        try {
                            gameScoreService.saveFromGameState(gameId, st, arr, total);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                case "snapshot_request" -> { return false; }
                default -> { return false; }
            }

            st.lastSeq = seq;
            st.appliedMsgIds.add(clientMsgId);
            if (st.recentEvents.size() >= 50) st.recentEvents.removeFirst();
            st.recentEvents.addLast(rawJson);
            return true;
        } catch (Exception e) { return false; }
    }

    public String getLatestSnapshot(Long gameId) {
        try {
            GameState st = games.computeIfAbsent(gameId, k -> new GameState());
            ObjectNode p = mapper.createObjectNode();
            ObjectNode u1 = mapper.createObjectNode(); u1.put("score", st.user1Score); u1.put("sets", st.user1Sets);
            ObjectNode u2 = mapper.createObjectNode(); u2.put("score", st.user2Score); u2.put("sets", st.user2Sets);
            p.put("setNumber", st.setNumber);
            p.set("user1", u1); p.set("user2", u2);
            p.put("serve", st.currentServe);
            p.put("lastAppliedSeq", st.lastSeq);

            // stopWatch
            ObjectNode stopWatch = mapper.createObjectNode();
            stopWatch.put("startAt", st.startAt);
            stopWatch.put("paused", st.paused);
            stopWatch.put("pauseStartedAt", st.pauseStartedAt);
            stopWatch.put("totalPaused", st.totalPaused);
            p.set("stopWatch", stopWatch);

            ObjectNode root = mapper.createObjectNode();
            root.put("type", "snapshot");
            root.put("gameId", gameId);
            root.set("payload", p);
            return mapper.writeValueAsString(root);
        } catch (Exception e) { return null; }
    }

    public Long extractgameId(String rawJson) {
        try {
            JsonNode node = new ObjectMapper().readTree(rawJson);
            long id = node.path("gameId").asLong(0L);
            return (id <= 0L) ? null : id;
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode n, String f){ return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null; }
}