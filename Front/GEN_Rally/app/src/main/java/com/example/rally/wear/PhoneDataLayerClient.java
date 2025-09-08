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

    private static final String PATH_GAME_SETUP      = "/rally/game_setup";
    private static final String PATH_EVENT_SET_START = "/rally/event/set_start";
    private static final String PATH_EVENT_SCORE     = "/rally/event/score";
    private static final String PATH_EVENT_UNDO      = "/rally/event/undo";
    private static final String PATH_EVENT_PAUSE     = "/rally/event/pause";
    private static final String PATH_EVENT_RESUME    = "/rally/event/resume";
    private static final String PATH_EVENT_SET_FINISH= "/rally/event/set_finish";

    private static volatile @Nullable String watchNodeId = null;
    public static void setWatchNodeId(@Nullable String id) {
        watchNodeId = id;
        Log.d("PhoneDL", "watchNodeId:" + id);
    }
    private static String nodeLabel(Node n) { return n.getDisplayName() + " (" + n.getId() + ")"; }

    public interface SendCallback {
        void onSuccess();
        void onNoNode();
        void onError(Exception e);
    }

    // 노드 찾기
    private static List<String> findTargets(Context ctx) throws Exception {
        List<String> targets = new ArrayList<>();
        try {
            CapabilityInfo info = Tasks.await(
                    Wearable.getCapabilityClient(ctx).getCapability(CAP_WATCH, CapabilityClient.FILTER_REACHABLE));
            Set<Node> nodes = info.getNodes();
            Log.d(TAG, "capability " + CAP_WATCH + " nodes=" + nodes.size());
            for (Node n : nodes) targets.add(n.getId());
        } catch (Exception e) {
            Log.w(TAG, "capability lookup failed", e);
        }
        if (targets.isEmpty()) {
            List<Node> connected = Tasks.await(Wearable.getNodeClient(ctx).getConnectedNodes());
            Log.d(TAG, "fallback connected nodes=" + connected.size());
            for (Node n : connected) targets.add(n.getId());
        }
        return targets;
    }

    // 단일 노드로만 전송
    private static void sendToPreferredOrAll(Context ctx, String path, String json) {
        Log.d("PhoneDL", "FORWARD -> path=" + path + " json=" + json);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        if (watchNodeId != null) {
            Wearable.getMessageClient(ctx).sendMessage(watchNodeId, path, data)
                    .addOnSuccessListener(id -> Log.d("PhoneDL","->watch " + path + " ok=" + id + " to " + watchNodeId))
                    .addOnFailureListener(e -> Log.e("PhoneDL","->watch fail " + path + " to " + watchNodeId, e));
            return;
        }

        // fallback: 연결된 모든 노드 (로그에 노드별로 찍기)
        Wearable.getNodeClient(ctx).getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    for (Node n : nodes) {
                        Wearable.getMessageClient(ctx).sendMessage(n.getId(), path, data)
                                .addOnSuccessListener(id -> Log.d("PhoneDL","->watch " + path + " ok=" + id + " to " + nodeLabel(n)))
                                .addOnFailureListener(e -> Log.e("PhoneDL","->watch fail " + path + " to " + nodeLabel(n), e));
                    }
                })
                .addOnFailureListener(e -> Log.e("PhoneDL","getConnectedNodes fail", e));
    }

    // 공통 데이터
    private static String buildEnvelope(String type, String matchId, JSONObject payload) {
        try {
            return new JSONObject()
                    .put("version", 1)
                    .put("type", type)
                    .put("eventId", java.util.UUID.randomUUID().toString())
                    .put("createdAtUtc", System.currentTimeMillis())
                    .put("matchId", matchId == null ? "" : matchId)
                    .put("payload", payload == null ? new JSONObject() : payload)
                    .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 경기 시작 세팅
    public static void sendGameSetup(Context ctx, String matchId,
                                     String user1Name, String user2Name,
                                     boolean watchIsUser1, @Nullable SendCallback cb) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> targets = findTargets(ctx);
                if (targets.isEmpty()) {
                    Log.w(TAG, "No reachable watch node");
                    if (cb != null) cb.onNoNode();
                    return;
                }
                JSONObject body = new JSONObject()
                        .put("version", 1)
                        .put("type", "GAME_SETUP")
                        .put("eventId", java.util.UUID.randomUUID().toString())
                        .put("createdAtUtc", System.currentTimeMillis())
                        .put("matchId", matchId == null ? "" : matchId)
                        .put("payload", new JSONObject()
                                .put("user1Name", user1Name)
                                .put("user2Name", user2Name)
                                .put("localIsUser1", watchIsUser1)
                                .put("setNumber", 1)
                                .put("firstServer", "USER1")
                        );
                byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);

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

    public static void sendPhoneEventToWatch(Context ctx, String path, String json) {
        sendToPreferredOrAll(ctx, path, json);
    }
}