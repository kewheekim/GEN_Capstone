package com.example.rally.api.websocket;

public interface RealtimeClient {
    void connect();
    void subscribe(String topic, java.util.function.Consumer<String> onMessage);
    void send(String body);
    void disconnect();
}