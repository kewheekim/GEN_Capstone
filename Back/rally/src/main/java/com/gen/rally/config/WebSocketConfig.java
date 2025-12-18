package com.gen.rally.config;

import com.gen.rally.websocket.handler.WsDemoHandler;
import com.gen.rally.websocket.handler.WsHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WsHandler handler;
    private final com.gen.rally.websocket.handler.WsDemoHandler demoHandler;

    public WebSocketConfig(WsHandler handler, com.gen.rally.websocket.handler.WsDemoHandler demoHandler) {
        this.handler = handler;
        this.demoHandler = demoHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws-score")
                .setAllowedOriginPatterns("*");

        registry.addHandler(demoHandler, "/ws-score-demo")
                .setAllowedOriginPatterns("*");
    }
}
