package com.gen.rally.websocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gen.rally.websocket.bus.MessageBus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DemoGameEventService {

    private final MessageBus messageBus; // ✅ set_finish에서 game_finish 브로드캐스트에 필요
    private final ObjectMapper mapper = new ObjectMapper();

    // gameId별 demo 상태(메모리)
    private final Map<Long, DemoState> games = new ConcurrentHashMap<>();

    public DemoGameEventService(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public boolean applyIfValid(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);
            String type = text(root, "type");
            long gameId = root.path("gameId").asLong(0L);
            if (gameId <= 0 || type == null) return false;

            DemoState st = games.computeIfAbsent(gameId, k -> DemoState.preset());

            // snapshot_request는 상태 적용 X
            if ("snapshot_request".equals(type)) return false;

            // 이미 게임 끝났으면 game_finish 외 이벤트 무시(선택)
            if (st.gameFinished && !"game_finish".equals(type)) return false;

            switch (type) {

                case "set_start" -> {
                    JsonNode p = root.path("payload");
                    int setNum = p.path("setNumber").asInt(st.setNumber);
                    String firstServer = p.path("firstServer").asText("USER1");
                    long startAt = p.path("startAt").asLong(System.currentTimeMillis());

                    st.setNumber = setNum;
                    st.serve = firstServer;

                    // ✅ 데모에서 “프리셋 점수 유지”하고 싶으면 아래 2줄을 지워.
                    // st.u1Score = 0;
                    // st.u2Score = 0;

                    st.setStartAt = startAt;
                    if (st.startAt == 0L) st.startAt = startAt;

                    st.paused = false;
                    st.pauseStartedAt = 0L;
                    st.totalPaused = 0L;
                    return true;
                }

                case "score_add" -> {
                    String to = root.path("payload").path("scoreTo").asText("");
                    if ("user1".equalsIgnoreCase(to)) st.u1Score = clamp(st.u1Score + 1, 0, 30);
                    else if ("user2".equalsIgnoreCase(to)) st.u2Score = clamp(st.u2Score + 1, 0, 30);
                    else return false;
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }

                case "score_undo" -> {
                    String from = root.path("payload").path("from").asText("user1");
                    if ("user1".equalsIgnoreCase(from)) st.u1Score = clamp(st.u1Score - 1, 0, 30);
                    else if ("user2".equalsIgnoreCase(from)) st.u2Score = clamp(st.u2Score - 1, 0, 30);
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }

                case "set_pause" -> {
                    if (st.paused) return false;
                    long at = root.path("payload").path("pausedAt")
                            .asLong(root.path("payload").path("timeStamp")
                                    .asLong(System.currentTimeMillis()));
                    st.paused = true;
                    st.pauseStartedAt = at;
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }

                case "set_resume" -> {
                    if (!st.paused) return false;
                    long at = root.path("payload").path("resumedAt")
                            .asLong(root.path("payload").path("timeStamp")
                                    .asLong(System.currentTimeMillis()));
                    if (st.pauseStartedAt > 0L) {
                        st.totalPaused += Math.max(0L, at - st.pauseStartedAt);
                    }
                    st.paused = false;
                    st.pauseStartedAt = 0L;
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }

                /**
                 * ✅ 핵심: GameEventService의 set_finish 패턴 그대로 이식
                 * - 세트 요약 저장
                 * - sets 증가
                 * - 게임 종료면 game_finish 브로드캐스트
                 */
                case "set_finish" -> {
                    JsonNode p = root.path("payload");
                    String winner = p.path("winner").asText(""); // "user1"|"user2"
                    long finishedAt = root.path("eventTime").asLong(System.currentTimeMillis());

                    if (st.setStartAt == 0L) st.setStartAt = finishedAt;

                    long elapsedMs = Math.max(0L, finishedAt - st.setStartAt - st.totalPaused);
                    long elapsedSec = elapsedMs / 1000L;

                    // 세트 요약 저장(현재 세트 번호 기준)
                    st.completedSets.add(
                            new DemoSetSummary(st.setNumber, st.u1Score, st.u2Score, st.setStartAt, finishedAt, elapsedSec)
                    );

                    // sets 증가
                    if ("user1".equalsIgnoreCase(winner)) st.u1Sets++;
                    else if ("user2".equalsIgnoreCase(winner)) st.u2Sets++;
                    else {
                        // winner가 비면 점수로 판정(데모 안정장치)
                        if (st.u1Score > st.u2Score) st.u1Sets++;
                        else if (st.u2Score > st.u1Score) st.u2Sets++;
                    }

                    // 다음 세트 준비
                    st.setNumber++;
                    st.u1Score = 0;
                    st.u2Score = 0;

                    st.setStartAt = 0L;
                    st.paused = true;
                    st.pauseStartedAt = 0L;
                    st.totalPaused = 0L;

                    st.lastActivityAt = System.currentTimeMillis();

                    // ✅ 게임 종료 판단 (2선승)
                    if (st.u1Sets >= 2 || st.u2Sets >= 2) {
                        ObjectNode gf = mapper.createObjectNode();
                        gf.put("path", "/rally/event/game_finish");
                        gf.put("type", "game_finish");
                        gf.put("gameId", gameId);

                        ObjectNode payload = mapper.createObjectNode();
                        payload.put("winner", (st.u1Sets > st.u2Sets) ? "user1" : "user2");
                        payload.put("user1Sets", st.u1Sets);
                        payload.put("user2Sets", st.u2Sets);
                        payload.put("setNumberFinished", st.setNumber - 1);

                        // 세트별 점수/시간
                        ArrayNode arr = mapper.createArrayNode();
                        long total = 0L;
                        for (DemoSetSummary ss : st.completedSets) {
                            ObjectNode s = mapper.createObjectNode();
                            s.put("setNumber", ss.setNumber);
                            s.put("user1Score", ss.user1Score);
                            s.put("user2Score", ss.user2Score);
                            s.put("elapsedSec", ss.elapsedSec);
                            arr.add(s);
                            total += ss.elapsedSec;
                        }
                        payload.set("sets", arr);
                        payload.put("totalElapsedSec", total);

                        gf.set("payload", payload);

                        // ✅ 여기서만 브로드캐스트!
                        messageBus.publish("/topic/game." + gameId, mapper.writeValueAsString(gf));

                        st.gameFinished = true;
                        st.totalElapsedSec = total;
                    }

                    return true;
                }

                // (선택) 클라가 game_finish를 보내는 경우도 허용하고 싶다면
                case "game_finish" -> {
                    st.gameFinished = true;
                    st.paused = true;
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }

                default -> {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    /** snapshot은 “요청 시 제공” */
    public String getLatestSnapshot(Long gameId) {
        try {
            if (gameId == null || gameId <= 0) return null;

            DemoState st = games.computeIfAbsent(gameId, k -> DemoState.preset());

            ObjectNode payload = mapper.createObjectNode();

            payload.put("setNumber", st.setNumber);
            payload.set("user1", mapper.createObjectNode().put("score", st.u1Score).put("sets", st.u1Sets));
            payload.set("user2", mapper.createObjectNode().put("score", st.u2Score).put("sets", st.u2Sets));
            payload.put("serve", st.serve);

            // stopWatch
            ObjectNode stopWatch = mapper.createObjectNode();
            stopWatch.put("startAt", st.startAt);
            stopWatch.put("paused", st.paused);
            stopWatch.put("pauseStartedAt", st.pauseStartedAt);
            stopWatch.put("totalPaused", st.totalPaused);
            payload.set("stopWatch", stopWatch);

            // setsSummary: completedSets가 있으면 그걸 내려줌
            ArrayNode setsSummary = mapper.createArrayNode();
            for (DemoSetSummary ss : st.completedSets) {
                setsSummary.add(mapper.createObjectNode()
                        .put("setNumber", ss.setNumber)
                        .put("user1Score", ss.user1Score)
                        .put("user2Score", ss.user2Score)
                        .put("elapsedSec", ss.elapsedSec));
            }
            payload.set("setsSummary", setsSummary);

            payload.put("gameFinished", st.gameFinished);
            payload.put("totalElapsedSec", st.totalElapsedSec);

            ObjectNode root = mapper.createObjectNode();
            root.put("type", "snapshot");
            root.put("gameId", gameId);
            root.set("payload", payload);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return null;
        }
    }

    public void reset(Long gameId) {
        if (gameId == null) return;
        games.remove(gameId);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String text(JsonNode n, String f) {
        return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null;
    }

    // ====== demo state ======
    static class DemoState {
        int setNumber;

        int u1Sets, u2Sets;
        int u1Score, u2Score;

        String serve;

        long startAt;       // 경기 시작(전체)
        long setStartAt;    // 세트 시작
        boolean paused;

        long pauseStartedAt;
        long totalPaused;

        long lastActivityAt;

        boolean gameFinished;
        long totalElapsedSec;

        java.util.List<DemoSetSummary> completedSets = new java.util.ArrayList<>();

        static DemoState preset() {
            DemoState s = new DemoState();

            // ✅ 너가 쓰던 프리셋 그대로
            s.setNumber = 3;
            s.u1Sets = 1;
            s.u2Sets = 1;
            s.u1Score = 18;
            s.u2Score = 19;

            s.serve = "USER1";
            s.startAt = 0L;
            s.setStartAt = 0L;

            s.paused = true;
            s.pauseStartedAt = 0L;
            s.totalPaused = 0L;

            s.lastActivityAt = System.currentTimeMillis();
            s.gameFinished = false;
            s.totalElapsedSec = 0L;

            // 1/2세트 요약도 넣고 싶으면:
            s.completedSets.add(new DemoSetSummary(1, 21, 19, 0L, 0L, 1012));
            s.completedSets.add(new DemoSetSummary(2, 17, 21, 0L, 0L, 906));

            return s;
        }
    }

    static class DemoSetSummary {
        final int setNumber;
        final int user1Score;
        final int user2Score;
        final long startedAt;
        final long finishedAt;
        final long elapsedSec;

        DemoSetSummary(int setNumber, int user1Score, int user2Score,
                       long startedAt, long finishedAt, long elapsedSec) {
            this.setNumber = setNumber;
            this.user1Score = user1Score;
            this.user2Score = user2Score;
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.elapsedSec = elapsedSec;
        }
    }
}
