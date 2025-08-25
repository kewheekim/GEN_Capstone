package com.gen.rally.websocket.handler;

import com.gen.rally.websocket.bus.MessageBus;
import com.gen.rally.websocket.service.GameEventService;
import com.gen.rally.websocket.util.QueryString;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class WsHandler extends TextWebSocketHandler {
    private final MessageBus bus;
    private final GameEventService service;
    public WsHandler(MessageBus bus, GameEventService service){ this.bus = bus; this.service = service; }

    private static String room(String matchId){ return "/topic/match." + matchId; }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) throws Exception {
        Map<String,String> q = QueryString.parse(s.getUri());
        String matchId = q.getOrDefault("matchId", "LOCAL-TEST");
        // TODO: JWT 검증(q.get("token"))
        bus.join(room(matchId), s);

        String snap = service.getLatestSnapshot(matchId);
        if (snap != null) s.sendMessage(new TextMessage(snap));
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        String raw = m.getPayload();
        if (raw.contains("\"type\":\"snapshot_request\"")) {
            String matchId = service.extractMatchId(raw);
            String snap = service.getLatestSnapshot(matchId);
            if (snap != null) s.sendMessage(new TextMessage(snap));
            return;
        }
        boolean applied = service.applyIfValid(raw);
        if (applied) {
            String matchId = service.extractMatchId(raw);
            if (matchId != null) bus.publish(room(matchId), raw);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status){ bus.leave(s); }
}