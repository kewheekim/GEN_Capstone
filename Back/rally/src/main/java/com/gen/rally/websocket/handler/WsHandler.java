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

    private static String room(Long gameId){ return "/topic/game." + gameId; }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) throws Exception {
        Map<String,String> q = QueryString.parse(s.getUri());
        String gameIdStr = q.get("gameId");
        long gameId = 0L;
        try {
            gameId = Long.parseLong(gameIdStr);
        } catch (Exception e) {
            s.close();
            return;
        }

        bus.join(room(gameId), s);

        String snap = service.getLatestSnapshot(gameId);  // Long 버전 호출
        if (snap != null) s.sendMessage(new TextMessage(snap));
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        String raw = m.getPayload();
        if (raw.contains("\"type\":\"snapshot_request\"")) {
            Long gameId = service.extractgameId(raw);
            String snap = service.getLatestSnapshot(gameId);
            if (snap != null) s.sendMessage(new TextMessage(snap));
            return;
        }
        boolean applied = service.applyIfValid(raw);
        if (applied) {
            Long gameId = service.extractgameId(raw);
            if (gameId != null) bus.publish(room(gameId), raw);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status){ bus.leave(s); }
}