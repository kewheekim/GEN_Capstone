package com.gen.rally.websocket.bus;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class RoomBus implements MessageBus {
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionRoom = new ConcurrentHashMap<>();

    @Override
    public void join(String room, WebSocketSession s) {
        rooms.computeIfAbsent(room, k -> new CopyOnWriteArraySet<>()).add(s);
        sessionRoom.put(s.getId(), room);
    }

    @Override
    public void leave(WebSocketSession s) {
        var room = sessionRoom.remove(s.getId());
        if (room != null) {
            Set<WebSocketSession> set = rooms.get(room);
            if (set != null) set.remove(s);
        }
    }

    @Override
    public void publish(String room, String body) throws Exception {
        var set = rooms.get(room);
        if (set == null) return;
        TextMessage msg = new TextMessage(body);
        for (var s : set) if (s.isOpen()) s.sendMessage(msg);
    }
}

