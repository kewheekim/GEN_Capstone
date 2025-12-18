package com.gen.rally.websocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DemoGameEventService {

    private final ObjectMapper mapper = new ObjectMapper();

    // gameId별 demo 상태(메모리)
    private final Map<Long, DemoState> games = new ConcurrentHashMap<>();

    public boolean applyIfValid(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);
            String type = text(root, "type");
            long gameId = root.path("gameId").asLong(0L);
            if (gameId <= 0 || type == null) return false;

            DemoState st = games.computeIfAbsent(gameId, k -> DemoState.preset());

            // 종료, 세트 시작 무시
            if ("set_start".equals(type)
                    || "set_finish".equals(type)
                    || "game_finish".equals(type)
                    || "snapshot_request".equals(type)) {
                return false;
            }

            switch (type) {
                case "score_add" -> {
                    String to = root.path("payload").path("scoreTo").asText("");
                    if ("user1".equalsIgnoreCase(to)) {
                        st.u1Score = clamp(st.u1Score + 1, 0, 30);
                    } else if ("user2".equalsIgnoreCase(to)) {
                        st.u2Score = clamp(st.u2Score + 1, 0, 30);
                    } else {
                        return false;
                    }
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }
                case "score_undo" -> {
                    String from = root.path("payload").path("from").asText("user1");
                    if ("user1".equalsIgnoreCase(from)) {
                        st.u1Score = clamp(st.u1Score - 1, 0, 30);
                    } else if ("user2".equalsIgnoreCase(from)) {
                        st.u2Score = clamp(st.u2Score - 1, 0, 30);
                    }
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }
                case "set_pause" -> {
                    st.paused = true;
                    st.lastActivityAt = System.currentTimeMillis();
                    return true;
                }
                case "set_resume" -> {
                    st.paused = false;
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

    /**
     * 전시용 스냅샷:
     * - 3세트 18:18, sets 1:1
     * - 1,2세트 결과(setsSummary) 포함
     * - vitals(heartSeries 포함) 포함
     */
    public String getLatestSnapshot(Long gameId) {
        try {
            if (gameId == null || gameId <= 0) return null;

            DemoState st = games.computeIfAbsent(gameId, k -> DemoState.preset());

            ObjectNode payload = mapper.createObjectNode();

            // 현재 진행 세트/점수
            payload.put("setNumber", st.setNumber);
            payload.set("user1", mapper.createObjectNode()
                    .put("score", st.u1Score)
                    .put("sets", st.u1Sets));
            payload.set("user2", mapper.createObjectNode()
                    .put("score", st.u2Score)
                    .put("sets", st.u2Sets));
            payload.put("serve", st.serve);
            payload.put("lastAppliedSeq", 0);

            // 1/2세트 결과
            payload.set("setsSummary", buildSetsSummary());

            // 생체데이터
            payload.set("vitals", buildVitals(st));

            // stopWatch (폰/워치 UI에서 필요할 수 있어 포함)
            ObjectNode stopWatch = mapper.createObjectNode();
            stopWatch.put("startAt", st.startAt);
            stopWatch.put("paused", st.paused);
            stopWatch.put("pauseStartedAt", 0L);
            stopWatch.put("totalPaused", 0L);
            payload.set("stopWatch", stopWatch);

            ObjectNode root = mapper.createObjectNode();
            root.put("type", "snapshot");
            root.put("gameId", gameId);
            root.set("payload", payload);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractgameId(String rawJson) {
        try {
            JsonNode node = mapper.readTree(rawJson);
            long id = node.path("gameId").asLong(0L);
            return (id <= 0L) ? null : id;
        } catch (Exception e) {
            return null;
        }
    }


    public void reset(Long gameId) {
        if (gameId == null) return;
        games.remove(gameId);
    }

    private ArrayNode buildSetsSummary() {
        ArrayNode arr = mapper.createArrayNode();

        // @@@점수, 시간 변경
        arr.add(mapper.createObjectNode()
                .put("setNumber", 1)
                .put("user1Score", 21)
                .put("user2Score", 19)
                .put("elapsedSec", 1012));

        arr.add(mapper.createObjectNode()
                .put("setNumber", 2)
                .put("user1Score", 17)
                .put("user2Score", 21)
                .put("elapsedSec", 906));

        return arr;
    }

    private ObjectNode buildVitals(DemoState st) {
        ObjectNode v = mapper.createObjectNode();

        // 헬스데이터
        v.put("calories", 204);
        v.put("steps", 3230);
        v.put("minHr", 83);
        v.put("maxHr", 91);

        // heartSeries
        ArrayNode series = mapper.createArrayNode();
        long base = System.currentTimeMillis() - 3 * 60_000L; // 3분 전부터
        int[] bpm = new int[]{
                83, 83, 84, 84, 85, 86, 88, 89, 89, 89, 90, 91, 90, 89, 88, 88,
                88, 88, 88, 87, 87, 88, 88, 88, 87, 87, 86, 88, 88, 87, 89
        };
        for (int i = 0; i < bpm.length; i++) {
            series.add(mapper.createObjectNode()
                    .put("bpm", bpm[i])
                    .put("epochMs", base + i * 1000L));
        }
        v.set("heartSeries", series);

        return v;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String text(JsonNode n, String f) {
        return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null;
    }

    static class DemoState {
        int setNumber;
        int u1Sets, u2Sets;
        int u1Score, u2Score;

        String serve;

        long startAt;
        boolean paused;

        long lastActivityAt;

        static DemoState preset() {
            DemoState s = new DemoState();

            // 시작 조건
            s.setNumber = 3;
            s.u1Sets = 1;
            s.u2Sets = 1;
            s.u1Score = 18;
            s.u2Score = 18;

            s.serve = "USER1";
            s.startAt = System.currentTimeMillis();
            s.paused = false;
            s.lastActivityAt = System.currentTimeMillis();

            return s;
        }
    }
}
