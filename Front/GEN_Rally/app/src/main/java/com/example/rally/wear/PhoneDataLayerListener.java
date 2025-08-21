package com.example.rally.wear;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
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

    private static final String PATH_ACK          = "/rally/ack";            // 폰→워치 ACK
    private static final String PATH_SNAPSHOT     = "/rally/snapshot";       // 폰→워치 스냅샷(DataItem)
    private static final String PATH_SNAPSHOT_REQ = "/rally/snapshot_req";   // 워치→폰 스냅샷 요청

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
        final String body = new String(ev.getData(), StandardCharsets.UTF_8);
        Log.d(TAG, "onMessageReceived <- " + path + " from " + fromNode + " : " + body);

        try {
            if (path.startsWith("/rally/event/")) {
                // ACK 비동기 전송(아래 3번)
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
            // TODO: 현재 상태로 교체 (ViewModel또는 Repo에서 읽어오기)
            JSONObject snap = new JSONObject()
                    .put("type", "SNAPSHOT")
                    .put("state", new JSONObject()
                            .put("setNumber", 1)
                            .put("userSets", 0)
                            .put("opponentSets", 0)
                            .put("userScore", 0)
                            .put("opponentScore", 0)
                            .put("currentServer", "USER1")
                    );

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

    // 가까운 워치 노드(참고용)
    private String pickWatchNodeOrNull() {
        try {
            CapabilityInfo info = Tasks.await(
                    Wearable.getCapabilityClient(this).getCapability(
                            CAP_WATCH, CapabilityClient.FILTER_REACHABLE));
            Set<Node> nodes = info.getNodes();
            for (Node n : nodes) if (n.isNearby()) return n.getId();
            return nodes.isEmpty() ? null : nodes.iterator().next().getId();
        } catch (Exception e) {
            return null;
        }
    }
}
