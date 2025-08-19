package com.example.rally.wear;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class PhoneDataLayerClient {
    private static final String TAG = "PhoneDL";
    private static final String CAP_WATCH = "rally_watch";
    private static final String PATH_GAME_SETUP = "/rally/game_setup";

    public interface SendCallback {
        void onSuccess();
        void onNoNode();
        void onError(Exception e);
    }

    public static void sendGameSetup(Context ctx,
                                     String matchId,
                                     String user1Name,
                                     String user2Name,
                                     boolean watchIsUser1,
                                     @Nullable SendCallback cb) {

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> targets = new ArrayList<>();

                // 1) capability로 먼저 찾기
                try {
                    CapabilityInfo info = Tasks.await(
                            Wearable.getCapabilityClient(ctx)
                                    .getCapability(CAP_WATCH, CapabilityClient.FILTER_REACHABLE));
                    Set<Node> nodes = info.getNodes();
                    Log.d(TAG, "capability " + CAP_WATCH + " nodes=" + nodes.size());
                    for (Node n : nodes) targets.add(n.getId());
                } catch (Exception e) {
                    Log.w(TAG, "capability lookup failed", e);
                }

                // 2) 폴백: 연결된 모든 노드로 전송
                if (targets.isEmpty()) {
                    List<Node> connected = Tasks.await(Wearable.getNodeClient(ctx).getConnectedNodes());
                    Log.d(TAG, "fallback connected nodes=" + connected.size());
                    for (Node n : connected) targets.add(n.getId());
                }

                if (targets.isEmpty()) {
                    Log.w(TAG, "No reachable watch node");
                    if (cb != null) cb.onNoNode();
                    return;
                }

                // payload
                JSONObject obj = new JSONObject()
                        .put("type", "GAME_SETUP")
                        .put("eventId", java.util.UUID.randomUUID().toString())
                        .put("createdAtUtc", System.currentTimeMillis())
                        .put("matchId", matchId)
                        .put("payload", new JSONObject()
                                .put("user1Name", user1Name)
                                .put("user2Name", user2Name)
                                .put("localIsUser1", watchIsUser1)
                                .put("firstServer", "USER1")
                        );
                byte[] data = obj.toString().getBytes(StandardCharsets.UTF_8);

                MessageClient mc = Wearable.getMessageClient(ctx);
                for (String nodeId : targets) {
                    int res = Tasks.await(mc.sendMessage(nodeId, PATH_GAME_SETUP, data));
                    Log.d(TAG, "GAME_SETUP -> " + nodeId + " result=" + res);
                }

                if (cb != null) cb.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "sendGameSetup failed", e);
                if (cb != null) cb.onError(e);
            }
        });
    }
}