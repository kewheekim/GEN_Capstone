package com.example.rally.datalayer

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject

class WatchDataLayerListener : WearableListenerService() {

    companion object {
        private const val TAG = "WatchDL"
        private const val PATH_ACK = "/rally/ack"
        private const val PATH_SNAPSHOT = "/rally/snapshot"
        private const val PATH_GAME_SETUP = "/rally/game_setup"
    }

    // Message 수신
    override fun onMessageReceived(event: MessageEvent) {
        val path = event.path
        val body = event.data?.let { String(it) } ?: ""
        Log.d(TAG, "onMessageReceived <- $path : $body")

        when (path) {
            PATH_GAME_SETUP -> {
                runCatching {
                    val obj = JSONObject(body)
                    val payload = obj.getJSONObject("payload")
                    val user1 = payload.optString("user1Name", "상대")
                    val user2 = payload.optString("user2Name", "나")
                    val isUser1 = payload.optBoolean("localIsUser1", true)
                    val setNumber = payload.optInt("setNumber", 1)

                    // ScoreActivity로 진입 (앱이 백그라운드여도 뜰 수 있게 NEW_TASK)
                    val i = Intent(this, com.example.rally.presentation.ScoreActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("setNumber", setNumber)
                        putExtra("opponentName", if (isUser1) user2 else user1) // 상대 닉네임
                        putExtra("userName", if (isUser1) user1 else user2)     // 로컬 사용자 닉네임
                        putExtra("localIsUser1", isUser1)
                    }
                    startActivity(i)
                }.onFailure {
                    Log.e(TAG, "GAME_SETUP parse/start failed", it)
                }
            }
            PATH_ACK -> {
                // eventId 기반 전송 큐에서 제거 등 추가
                Log.d(TAG, "ACK 수신: $body")
            }
            else -> super.onMessageReceived(event)
        }
    }

    // DataItem(스냅샷) 수신
    override fun onDataChanged(events: DataEventBuffer) {
        for (e in events) {
            if (e.type == DataEvent.TYPE_CHANGED &&
                e.dataItem.uri.path == PATH_SNAPSHOT) {
                val json = DataMapItem.fromDataItem(e.dataItem)
                    .dataMap.getString("json") ?: "{}"
                Log.d(TAG, "SNAPSHOT 수신: $json")

                //  ViewModel.applySnapshot(json) 호출로 ui 갱신
            }
        }
    }
}