package com.example.rally.api.websocket;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.*;
import okio.ByteString;

public class WsRealtimeClient implements RealtimeClient {
    private final OkHttpClient client;
    private final String url;
    private WebSocket socket;
    private Consumer<String> listener;
    private int backoffMs = 1000;

    public WsRealtimeClient(String url) {
        this.url = url;
        this.client = new OkHttpClient.Builder()
                .pingInterval(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Override public void connect() {
        Request req = new Request.Builder().url(url).build();
        socket = client.newWebSocket(req, new WebSocketListener() {
            @Override public void onOpen(WebSocket ws, Response response) {
                backoffMs = 1000; // reset
                // 필요시 snapshot_request 전송 가능
            }
            @Override public void onMessage(WebSocket ws, String text) {
                if (listener != null) new Handler(Looper.getMainLooper()).post(() -> listener.accept(text));
            }
            @Override public void onMessage(WebSocket ws, ByteString bytes) { /* not used */ }
            @Override public void onFailure(WebSocket ws, Throwable t, Response r) {
                scheduleReconnect();
            }
            @Override public void onClosed(WebSocket ws, int code, String reason) {
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        int delay = Math.min(backoffMs, 30000);
        new Handler(Looper.getMainLooper()).postDelayed(this::connect, delay);
        backoffMs = Math.min(backoffMs * 2, 30000);
    }

    @Override public void subscribe(String topic, Consumer<String> onMessage) { this.listener = onMessage; }
    @Override public void send(String body) { if (socket != null) socket.send(body); }
    @Override public void disconnect() { if (socket != null) socket.close(1000, "bye"); }
}