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
        const val ACTION_PHONE_EVENT = "rally.EVENT_FROM_PHONE"
        private const val PATH_SNAPSHOT = "/rally/snapshot"
        private const val PATH_GAME_SETUP = "/rally/game_setup"
        private const val PATH_EVENT_SET_START = "/rally/event/set_start"
        private const val PATH_EVENT_SCORE = "/rally/event/score"
        private const val PATH_EVENT_UNDO  = "/rally/event/undo"
        private const val PATH_EVENT_PAUSE = "/rally/event/pause"
        private const val PATH_EVENT_RESUME= "/rally/event/resume"
        private const val PATH_EVENT_SET_FINISH = "/rally/event/set_finish"
        private const val PATH_EVENT_GAME_FINISH = "/rally/event/game_finish"
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
                    val gameId = obj.optLong("gameId", 0L)

                    // ScoreActivity로 진입 (앱이 백그라운드여도 뜰 수 있게 NEW_TASK)
                    val i = Intent(this, com.example.rally.presentation.ScoreActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("setNumber", setNumber)
                        putExtra("opponentName", if (isUser1) user2 else user1)
                        putExtra("userName", if (isUser1) user1 else user2)
                        putExtra("localIsUser1", isUser1)
                        putExtra("gameId", gameId)
                    }
                    startActivity(i)
                }.onFailure {
                    Log.e(TAG, "GAME_SETUP parse/start failed", it)
                }
            }
            PATH_EVENT_SET_START, PATH_EVENT_SCORE, PATH_EVENT_UNDO,
            PATH_EVENT_PAUSE, PATH_EVENT_RESUME, PATH_EVENT_SET_FINISH, PATH_EVENT_GAME_FINISH -> {
                Log.d("WatchDL", "BROADCAST -> $path")
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(ACTION_PHONE_EVENT).apply {
                        putExtra("path", path)
                        putExtra("json", body)
                    })
            }
            else -> super.onMessageReceived(event)
        }
    }

    // DataItem(스냅샷) 수신
    override fun onDataChanged(events: DataEventBuffer) {
        for (e in events) {
            if (e.type == DataEvent.TYPE_CHANGED && e.dataItem.uri.path == PATH_SNAPSHOT) {
                val json = DataMapItem.fromDataItem(e.dataItem).dataMap.getString("json") ?: "{}"
                Log.d(TAG, "SNAPSHOT 수신: $json")
                // 스냅샷 액티비티로 전달
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(ACTION_PHONE_EVENT).apply {
                        putExtra("path", PATH_SNAPSHOT)
                        putExtra("json", json)
                    })
            }
        }
    }
}