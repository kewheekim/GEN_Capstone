package com.example.rally.wear;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.rally.BuildConfig;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.GameHealthRequest;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class PhoneDataLayerListener extends WearableListenerService {
    private static final String TAG = "PhoneDL";

    private static final String PATH_ACK = "/rally/ack";    // 폰→워치 ACK
    private static final String PATH_SNAPSHOT = "/rally/snapshot";       // 폰→워치 스냅샷(DataItem)
    private static final String PATH_SNAPSHOT_REQ = "/rally/snapshot_req";   // 워치→폰 스냅샷 요청
    private static final String PATH_HEALTH_SUMMARY = "/rally/health_summary";

    // Capability
    private static final String CAP_WATCH = "rally_watch";

    // App broadcast (액티비티/프래그먼트에서 registerReceiver로 수신)
    public static final String ACTION_WATCH_EVENT = "rally.EVENT_FROM_WATCH";
    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_JSON = "json";

    @Override
    public void onMessageReceived(@NonNull MessageEvent ev) {
        final String path = ev.getPath();
        final String fromNode = ev.getSourceNodeId();
        PhoneDataLayerClient.setWatchNodeId(fromNode);
        final String body = new String(ev.getData(), StandardCharsets.UTF_8);
        Log.d(TAG, "onMessageReceived <- " + path + " from " + fromNode + " : " + body);

        try {
            if (PATH_HEALTH_SUMMARY.equals(path)) {
                // 워치에서 보낸 헬스 요약 처리
                handleHealthSummary(body);
                return;
            }
            if (path.startsWith("/rally/event/")) {
                // ACK 비동기 전송
                sendAckAsync(fromNode, safeEventId(body));
                // 액티비티로 브릿지
                broadcastEventToApp(path, body);
                return;
            }
            if (PATH_SNAPSHOT_REQ.equals(path)) {
                pushSnapshotNow();
                return;
            }
            super.onMessageReceived(ev);

        } catch (Throwable t) {
            Log.e(TAG, "onMessageReceived error", t);
        }
    }

    // DataItem(스냅샷) 수신
    @Override
    public void onDataChanged(@NonNull DataEventBuffer events) {
        for (DataEvent e : events) {
            if (e.getType() == DataEvent.TYPE_CHANGED &&
                    PATH_SNAPSHOT.equals(e.getDataItem().getUri().getPath())) {
                String json = DataMapItem.fromDataItem(e.getDataItem())
                        .getDataMap()
                        .getString("json", "{}");
                Log.d(TAG, "onDataChanged <- snapshot (debug) : " + json);
            }
        }
    }

    // Capability 변경 로그
    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: " + capabilityInfo.getName() + " nodes=" + capabilityInfo.getNodes().size());
    }

    private void broadcastEventToApp(String path, String json) {
        Intent i = new Intent(ACTION_WATCH_EVENT)
                .putExtra(EXTRA_PATH, path)
                .putExtra(EXTRA_JSON, json);
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .sendBroadcast(i);
    }

    private void sendAckAsync(String toNodeId, String eventId) {
        try {
            JSONObject ack = new JSONObject().put("eventId", eventId);
            byte[] data = ack.toString().getBytes(StandardCharsets.UTF_8);
            Wearable.getMessageClient(this)
                    .sendMessage(toNodeId, PATH_ACK, data)
                    .addOnSuccessListener(reqId ->
                            Log.d(TAG, "ACK -> " + toNodeId + " (" + eventId + ") ok=" + reqId))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "ACK failed (" + eventId + ")", e));
        } catch (Exception e) {
            Log.e(TAG, "sendAckAsync build failed", e);
        }
    }

    private void pushSnapshotNow() {
        try {
            JSONObject payload = new JSONObject()
                    .put("setNumber", 1)
                    .put("user1Sets", 0)
                    .put("user2Sets", 0)
                    .put("user1Score", 0)
                    .put("user2Score", 0)
                    .put("currentServer", "USER1");
            JSONObject snap = new JSONObject()
                    .put("version", 1)
                    .put("type", "SNAPSHOT")
                    .put("eventId", java.util.UUID.randomUUID().toString())
                    .put("createdAt", System.currentTimeMillis())
                    .put("gameId", 123L)   // TODO: 실제 gameId(Long)로 교체
                    .put("payload", payload);

            PutDataMapRequest req = PutDataMapRequest.create(PATH_SNAPSHOT);
            req.getDataMap().putString("json", snap.toString());
            PutDataRequest put = req.asPutDataRequest().setUrgent();
            Tasks.await(Wearable.getDataClient(this).putDataItem(put));
            Log.d(TAG, "snapshot -> watch");
        } catch (Exception e) {
            Log.e(TAG, "pushSnapshotNow failed", e);
        }
    }

    private String safeEventId(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj.optString("eventId", "");
        } catch (JSONException e) {
            return "";
        }
    }

    private void handleHealthSummary(String json) {
        try {
            Log.d(TAG, "handleHealthSummary raw=" + json);
            JSONObject root = new JSONObject(json);
            long gameId = root.optLong("gameId", 123L);
            if (gameId <= 0L) {
                return;
            }

            JSONObject payload = root.optJSONObject("payload");
            if (payload == null) payload = new JSONObject();

            int totalSteps = (int)payload.optLong("totalSteps", 0L);
            int totalKcal = payload.optInt("totalKcal", 0);

            Integer overallMax = payload.isNull("overallMaxBpm")
                    ? null : payload.optInt("overallMaxBpm");
            Integer overallMin = payload.isNull("overallMinBpm")
                    ? null : payload.optInt("overallMinBpm");

            // heartSeries 합치기
            JSONObject seriesRoot = new JSONObject();
            org.json.JSONArray perSet = payload.optJSONArray("perSet");
            org.json.JSONArray allHeart = new org.json.JSONArray();

            if (perSet != null) {
                for (int i = 0; i < perSet.length(); i++) {
                    JSONObject setObj = perSet.optJSONObject(i);
                    if (setObj == null) continue;

                    org.json.JSONArray heartArr = setObj.optJSONArray("heartSeries");
                    if (heartArr == null || heartArr.length() == 0) continue;

                    for (int j = 0; j < heartArr.length(); j++) {
                        allHeart.put(heartArr.getJSONObject(j)); // {epochMs, bpm} 형식
                    }
                }
            }

            seriesRoot.put("heartSeries", allHeart);
            String seriesHr = seriesRoot.toString();

            GameHealthRequest dto = new GameHealthRequest(gameId, totalSteps, overallMax, overallMin, totalKcal, seriesHr);

            ApiService api = RetrofitClient
                    .getSecureClient(getApplicationContext(), BuildConfig.API_BASE_URL)
                    .create(ApiService.class);

            api.saveGameHealth(dto).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call,
                                       retrofit2.Response<Void> response) {
                    Log.d(TAG, "saveGameHealth success code=" + response.code());
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    Log.e(TAG, "saveGameHealth failed", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "handleHealthSummary parse error", e);
        }
    }
}
