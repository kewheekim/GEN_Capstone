package com.example.rally.wear;

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

    // Capability 상수 정의
    private static final String PATH_EVENT  = "/rally/event";        // 워치→폰 이벤트
    private static final String PATH_ACK  = "/rally/ack";          // 폰→워치 ACK
    private static final String PATH_SNAPSHOT  = "/rally/snapshot";     // 폰→워치 스냅샷(DataItem) 전송
    private static final String PATH_SNAPSHOT_REQ = "/rally/snapshot_req";  // 워치→폰 스냅샷 요청
    private static final String CAP_WATCH  = "rally_watch";

    // Message 수신
    @Override
    public void onMessageReceived(@NonNull MessageEvent ev) {
        final String path = ev.getPath();
        final String fromNode = ev.getSourceNodeId();
        final String body = new String(ev.getData(), StandardCharsets.UTF_8);
        Log.d(TAG, "onMessageReceived <- " + path + " from " + fromNode + " : " + body);

        try {
            if (PATH_EVENT.equals(path)) {
                // 워치에서 온 이벤트(JSON): eventId가 있으면 그대로 ACK 회신
                String eventId = safeEventId(body);
                sendAck(fromNode, eventId);

                // 앱 내부로 브릿지 → ViewModel/Repository에 반영
                // ex) LocalBroadcast, singleton, or direct repository call
                // broadcastEventToApp(body);

            } else if (PATH_SNAPSHOT_REQ.equals(path)) {
                // 워치가 스냅샷 요청 → 현재 상태를 DataItem으로 push
                pushSnapshotNow();

            } else {
                super.onMessageReceived(ev);
            }
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

    // Capability 선택
    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: " + capabilityInfo.getName() + " nodes=" + capabilityInfo.getNodes().size());
    }

    // ACK 전송
    private void sendAck(String toNodeId, String eventId) {
        try {
            JSONObject ack = new JSONObject();
            ack.put("eventId", eventId);
            byte[] data = ack.toString().getBytes(StandardCharsets.UTF_8);
            Tasks.await(Wearable.getMessageClient(this).sendMessage(toNodeId, PATH_ACK, data));
            Log.d(TAG, "ACK -> " + toNodeId + " (" + eventId + ")");
        } catch (Exception e) {
            Log.e(TAG, "sendAck failed", e);
        }
    }

    // 스냅샷 전송 JSON
    private void pushSnapshotNow() {
        try {
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
            // DataItem 키 충돌을 피하려면 timestamp를 파라미터로 넣어 경로를 바꾸거나, setUrgent 사용
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

    //  가까운 워치 노드 찾기
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