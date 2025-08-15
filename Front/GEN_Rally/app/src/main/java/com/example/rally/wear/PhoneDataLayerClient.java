package com.example.rally.wear;

import android.content.Context;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

public class PhoneDataLayerClient {
    private static final String TAG = "PhoneDL";
    private static final String CAP_WATCH = "rally_watch";
    private static final String PATH_MATCH_SETUP = "/rally/match_setup";
    public interface SendCallback {
        void onSuccess();
        void onNoNode();              // 연결된 워치 없음
        void onError(Exception e);
    }

    public static void sendMatchSetup(Context context,
                                      String matchId,
                                      String user1Name,
                                      String user2Name,
                                      boolean isUser1,
                                      @Nullable SendCallback callback
                                      ) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String nodeId = pickWatchNodeOrNull(context);
                if (nodeId == null) {
                    Log.w(TAG, "No reachable watch node");
                    return;
                }
                JSONObject obj = new JSONObject()
                        .put("type", "MATCH_SETUP")
                        .put("eventId", UUID.randomUUID().toString())
                        .put("createdAtUtc", System.currentTimeMillis())
                        .put("matchId", matchId)
                        .put("payload", new JSONObject()
                                .put("user1Name", user1Name)
                                .put("user2Name", user2Name)
                                .put("isUser1", isUser1)   // 워치에서 로컬이 USER1인지
                                .put("firstServer", "USER1")          // 규칙상 1세트는 USER1
                        );

                byte[] data = obj.toString().getBytes(StandardCharsets.UTF_8);
                Tasks.await(Wearable.getMessageClient(context)
                        .sendMessage(nodeId, PATH_MATCH_SETUP, data));
                if(callback != null) callback.onSuccess();
            } catch (Exception e) {
                if(callback != null) callback.onError(e);
                Log.e(TAG, "sendMatchSetup failed", e);
            }
        });
    }

    @Nullable
    private static String pickWatchNodeOrNull(Context ctx) {
        try {
            CapabilityInfo info = Tasks.await(
                    Wearable.getCapabilityClient(ctx)
                            .getCapability(CAP_WATCH, CapabilityClient.FILTER_REACHABLE));
            Set<Node> nodes = info.getNodes();
            for (Node n : nodes) if (n.isNearby()) return n.getId();
            return nodes.isEmpty() ? null : nodes.iterator().next().getId();
        } catch (Exception e) {
            return null;
        }
    }
}