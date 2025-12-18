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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WsHandler extends TextWebSocketHandler {
    private final MessageBus bus;
    private final GameEventService service;
    public WsHandler(MessageBus bus, GameEventService service){ this.bus = bus; this.service = service; }

    private static String room(Long gameId){ return "/topic/game." + gameId; }

    // 세션이 어떤 gameId로 붙었는지 추적
    private final ConcurrentMap<String, Long> sessionToGame = new ConcurrentHashMap<>();
    // gameId별 현재 접속 세션 수
    private final ConcurrentMap<Long, AtomicInteger> gameConnCount = new ConcurrentHashMap<>();

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
        sessionToGame.put(s.getId(), gameId);
        gameConnCount.computeIfAbsent(gameId, k -> new AtomicInteger(0)).incrementAndGet();

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
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
        bus.leave(s);
        Long gameId = sessionToGame.remove(s.getId());
        if (gameId == null) return;

        AtomicInteger cnt = gameConnCount.get(gameId);
        if (cnt == null) return;

        int left = cnt.decrementAndGet();
        if (left <= 0) {
            gameConnCount.remove(gameId);
            service.reset(gameId); // 두 기기 다 끊기면 초기 상태로
        }
    }
}