package com.gen.rally.websocket.bus;

import org.springframework.web.socket.WebSocketSession;

public interface MessageBus {
    void join(String room, WebSocketSession session);
    void leave(WebSocketSession session);
    void publish(String room, String body) throws Exception;
}
