package com.gen.rally.websocket.service;

import com.gen.rally.websocket.model.GameState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameEventService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, GameState> matches = new ConcurrentHashMap<>();

    public boolean applyIfValid(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);
            String type = text(root, "type");
            String matchId = text(root, "matchId");
            String clientMsgId = text(root, "clientMsgId");
            int seq = root.has("seq") ? root.get("seq").asInt() : 0;
            if (matchId == null || clientMsgId == null || type == null) return false;

            GameState st = matches.computeIfAbsent(matchId, k -> new GameState());
            if (st.appliedMsgIds.contains(clientMsgId)) return false;       // 멱등
            if (seq != st.lastSeq + 1) return false;                         // 간단 순서 체크

            switch (type) {
                case "score_add" -> {
                    String to = root.path("payload").path("scoreTo").asText();
                    if ("user1".equals(to)) st.user1Score++; else if ("user2".equals(to)) st.user2Score++;
                }
                case "score_undo" -> {
                    String from = root.path("payload").path("from").asText("user1");
                    if ("user1".equals(from) && st.user1Score > 0) st.user1Score--;
                    else if ("user2".equals(from) && st.user2Score > 0) st.user2Score--;
                }
                case "set_finish" -> {
                    String winner = root.path("payload").path("winner").asText();
                    if ("user1".equals(winner)) st.user1Sets++; else st.user2Sets++;
                    st.setNumber++;
                    st.user1Score = st.user2Score = 0;
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

    public String getLatestSnapshot(String matchId) {
        try {
            GameState st = matches.computeIfAbsent(matchId, k -> new GameState());
            ObjectNode p = mapper.createObjectNode();
            ObjectNode u1 = mapper.createObjectNode(); u1.put("score", st.user1Score); u1.put("sets", st.user1Sets);
            ObjectNode u2 = mapper.createObjectNode(); u2.put("score", st.user2Score); u2.put("sets", st.user2Sets);
            p.put("setNumber", st.setNumber);
            p.set("user1", u1); p.set("user2", u2);
            p.put("serve", st.currentServe);
            p.put("lastAppliedSeq", st.lastSeq);

            ObjectNode root = mapper.createObjectNode();
            root.put("type", "snapshot");
            root.put("matchId", matchId);
            root.set("payload", p);
            return mapper.writeValueAsString(root);
        } catch (Exception e) { return null; }
    }

    public String extractMatchId(String rawJson) {
        try { return text(new ObjectMapper().readTree(rawJson), "matchId"); }
        catch (Exception e) { return null; }
    }

    private static String text(JsonNode n, String f){ return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null; }
}