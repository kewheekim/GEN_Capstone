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

    private final ObjectMapper mapper = new ObjectMapper();
    private final MessageBus messageBus;

    // gameId별 demo 상태(메모리)
    private final Map<Long, DemoState> games = new ConcurrentHashMap<>();

    public DemoGameEventService(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    /**
     * ✅ 데모 이벤트 적용 + 상대에게 브로드캐스트
     * - true 반환: 상태가 바뀌었고 publish까지 완료
     * - false 반환: 무시(멱등/스냅샷요청/유효성 실패 등)
     */
    public boolean applyIfValid(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);
            String type = text(root, "type");
            long gameId = root.path("gameId").asLong(0L);
            if (gameId <= 0 || type == null) return false;

            DemoState st = games.computeIfAbsent(gameId, k -> DemoState.preset(mapper));

            // 스냅샷 요청은 "상태 갱신"이 아니라서 여기서는 무시(별도 API에서 getLatestSnapshot 호출)
            if ("snapshot_request".equals(type)) return false;

            // 게임이 끝났으면 game_finish 외 이벤트는 무시
            if (st.gameFinished && !"game_finish".equals(type)) return false;

            boolean changed = false;

            switch (type) {
                case "set_start" -> {
                    // ⚠️ 데모에서는 "start 눌렀더니 0:0으로 리셋" 되면 안 되니까
                    // 점수/세트 reset 절대 하지 말고, 타이머/paused만 갱신
                    long startAt = root.path("payload").path("startAt").asLong(System.currentTimeMillis());
                    st.startAt = startAt;
                    st.paused = false;
                    st.lastActivityAt = System.currentTimeMillis();
                    changed = true;

                    // 원하면 set_start 브로드캐스트 payload에 현재 점수도 같이 넣어줄 수도 있음(아래 buildSetStartEvent 참고)
                }

                case "score_add" -> {
                    String to = root.path("payload").path("scoreTo").asText("");
                    if ("user1".equalsIgnoreCase(to)) st.u1Score = clamp(st.u1Score + 1, 0, 30);
                    else if ("user2".equalsIgnoreCase(to)) st.u2Score = clamp(st.u2Score + 1, 0, 30);
                    else return false;

                    st.lastActivityAt = System.currentTimeMillis();
                    changed = true;
                }

                case "score_undo" -> {
                    String from = root.path("payload").path("from").asText("user1");
                    if ("user1".equalsIgnoreCase(from)) st.u1Score = clamp(st.u1Score - 1, 0, 30);
                    else if ("user2".equalsIgnoreCase(from)) st.u2Score = clamp(st.u2Score - 1, 0, 30);

                    st.lastActivityAt = System.currentTimeMillis();
                    changed = true;
                }

                case "set_pause" -> {
                    st.paused = true;
                    st.lastActivityAt = System.currentTimeMillis();
                    changed = true;
                }

                case "set_resume" -> {
                    st.paused = false;
                    st.lastActivityAt = System.currentTimeMillis();
                    changed = true;
                }

                case "game_finish" -> {
                    JsonNode p = root.path("payload");

                    long totalElapsedSec = p.path("totalElapsedSec").asLong(-1L);
                    if (totalElapsedSec >= 0) st.totalElapsedSec = totalElapsedSec;
                    else {
                        if (st.startAt > 0) st.totalElapsedSec = Math.max(0L, (System.currentTimeMillis() - st.startAt) / 1000L);
                        else st.totalElapsedSec = 0L;
                    }

                    JsonNode setsNode = p.get("sets");
                    if (setsNode != null && setsNode.isArray()) {
                        ArrayNode arr = mapper.createArrayNode();
                        for (JsonNode s : setsNode) {
                            ObjectNode one = mapper.createObjectNode();
                            one.put("setNumber", s.path("setNumber").asInt(0));
                            one.put("user1Score", s.path("user1Score").asInt(0));
                            one.put("user2Score", s.path("user2Score").asInt(0));
                            one.put("elapsedSec", s.path("elapsedSec").asLong(0L));
                            arr.add(one);
                        }
                        st.sets = arr;
                    } else {
                        if (st.sets == null) st.sets = mapper.createArrayNode();
                        st.sets.add(mapper.createObjectNode()
                                .put("setNumber", st.setNumber)
                                .put("user1Score", st.u1Score)
                                .put("user2Score", st.u2Score)
                                .put("elapsedSec", 0L));
                    }

                    st.gameFinished = true;
                    st.paused = true;
                    st.lastActivityAt = System.currentTimeMillis();
                    changed = true;
                }

                default -> {
                    return false;
                }
            }

            if (!changed) return false;

            // ✅ 여기서 "상대에게 브로드캐스트"
            // - 실코드처럼 rawJson 그대로 보내도 되지만,
            // - 데모는 watch/phone이 바로 쓰기 쉽게 "score 이벤트는 절대 점수 포함" 형태로 보내는 게 안전함.
            String out = buildBroadcastEvent(type, gameId, st, root);
            messageBus.publish("/topic/game." + gameId, out);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ✅ 데모 broadcast는 "watch/phone이 즉시 반영 가능하게" 절대값을 넣어서 보내는 게 제일 안전
     * - /rally/event/score 처럼 u1/u2 score 포함
     * - set_start도 현재 점수 유지한 채로 보내기 가능
     */
    private String buildBroadcastEvent(String type, long gameId, DemoState st, JsonNode inboundRoot) throws Exception {
        // 네 워치 수신 쪽이 path 기반이면 path도 같이 내려주면 편함
        // (실코드에서 gf.put("path", "/rally/event/game_finish") 한 것처럼)
        String path = switch (type) {
            case "set_start" -> "/rally/event/set_start";
            case "score_add", "score_undo" -> "/rally/event/score";
            case "set_pause" -> "/rally/event/pause";
            case "set_resume" -> "/rally/event/resume";
            case "game_finish" -> "/rally/event/game_finish";
            default -> "/rally/event/unknown";
        };

        ObjectNode out = mapper.createObjectNode();
        out.put("path", path);
        out.put("type", type);
        out.put("gameId", gameId);

        ObjectNode p = mapper.createObjectNode();

        // 공통(현재 상태 절대값)
        p.put("setNumber", st.setNumber);
        p.put("user1Score", st.u1Score);
        p.put("user2Score", st.u2Score);
        p.put("user1Sets", st.u1Sets);
        p.put("user2Sets", st.u2Sets);
        p.put("currentServer", st.serve);

        // set_start 타임스탬프
        if ("set_start".equals(type)) {
            long startAt = inboundRoot.path("payload").path("startAt").asLong(System.currentTimeMillis());
            p.put("startAt", startAt);
            p.put("firstServer", st.serve);
        }

        // pause/resume 타임스탬프
        if ("set_pause".equals(type) || "set_resume".equals(type)) {
            long ts = inboundRoot.path("payload").path("timeStamp").asLong(System.currentTimeMillis());
            p.put("timeStamp", ts);
        }

        // game_finish는 sets/totalElapsed 같이 포함
        if ("game_finish".equals(type)) {
            p.put("totalElapsedSec", st.totalElapsedSec);
            if (st.sets != null) p.set("sets", st.sets);
        }

        out.set("payload", p);
        return mapper.writeValueAsString(out);
    }

    public String getLatestSnapshot(Long gameId) {
        try {
            if (gameId == null || gameId <= 0) return null;

            DemoState st = games.computeIfAbsent(gameId, k -> DemoState.preset(mapper));

            ObjectNode payload = mapper.createObjectNode();

            payload.put("setNumber", st.setNumber);
            payload.set("user1", mapper.createObjectNode().put("score", st.u1Score).put("sets", st.u1Sets));
            payload.set("user2", mapper.createObjectNode().put("score", st.u2Score).put("sets", st.u2Sets));
            payload.put("serve", st.serve);
            payload.put("lastAppliedSeq", 0);

            payload.set("setsSummary", st.sets != null ? st.sets : buildSetsSummary());
            payload.set("vitals", buildVitals(st));

            ObjectNode stopWatch = mapper.createObjectNode();
            stopWatch.put("startAt", st.startAt);
            stopWatch.put("paused", st.paused);
            stopWatch.put("pauseStartedAt", 0L);
            stopWatch.put("totalPaused", 0L);
            payload.set("stopWatch", stopWatch);

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
        arr.add(mapper.createObjectNode().put("setNumber", 1).put("user1Score", 21).put("user2Score", 19).put("elapsedSec", 1012));
        arr.add(mapper.createObjectNode().put("setNumber", 2).put("user1Score", 17).put("user2Score", 21).put("elapsedSec", 906));
        return arr;
    }

    private ObjectNode buildVitals(DemoState st) {
        ObjectNode v = mapper.createObjectNode();
        v.put("calories", 204);
        v.put("steps", 3230);
        v.put("minHr", 83);
        v.put("maxHr", 91);

        ArrayNode series = mapper.createArrayNode();
        long base = System.currentTimeMillis() - 3 * 60_000L;
        int[] bpm = new int[]{83,83,84,84,85,86,88,89,89,89,90,91,90,89,88,88,88,88,88,87,87,88,88,88,87,87,86,88,88,87,89};
        for (int i = 0; i < bpm.length; i++) {
            series.add(mapper.createObjectNode().put("bpm", bpm[i]).put("epochMs", base + i * 1000L));
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

        boolean gameFinished;
        long totalElapsedSec;
        ArrayNode sets;

        static DemoState preset(ObjectMapper mapper) {
            DemoState s = new DemoState();
            s.setNumber = 3;
            s.u1Sets = 1;
            s.u2Sets = 1;
            s.u1Score = 18;
            s.u2Score = 19;
            s.serve = "USER1";
            s.startAt = 0L;
            s.paused = true;
            s.lastActivityAt = System.currentTimeMillis();

            ArrayNode arr = mapper.createArrayNode();
            arr.add(mapper.createObjectNode().put("setNumber", 1).put("user1Score", 21).put("user2Score", 19).put("elapsedSec", 1012));
            arr.add(mapper.createObjectNode().put("setNumber", 2).put("user1Score", 17).put("user2Score", 21).put("elapsedSec", 906));
            s.sets = arr;

            s.gameFinished = false;
            s.totalElapsedSec = 0L;
            return s;
        }
    }
}
