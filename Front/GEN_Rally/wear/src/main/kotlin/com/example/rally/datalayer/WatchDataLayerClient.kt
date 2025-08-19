package com.example.rally.datalayer

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.Executors

object WatchDataLayerClient {
    private const val TAG = "WatchDL"
    private const val CAP_PHONE = "rally_phone"               // 폰 capability(없어도 폴백)
    private const val PATH_EVENT_START  = "/rally/event/start"
    private const val PATH_EVENT_SCORE  = "/rally/event/score"
    private const val PATH_EVENT_UNDO   = "/rally/event/undo"
    private const val PATH_EVENT_PAUSE  = "/rally/event/pause"
    private const val PATH_EVENT_RESUME = "/rally/event/resume"

    private fun sendJson(ctx: Context, path: String, body: JSONObject) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val data = body.toString().toByteArray(StandardCharsets.UTF_8)

                // 1) capability 우선
                val targets = mutableListOf<String>()
                try {
                    val info = Tasks.await(
                        Wearable.getCapabilityClient(ctx)
                            .getCapability(CAP_PHONE, CapabilityClient.FILTER_REACHABLE)
                    )
                    info.nodes.forEach { targets.add(it.id) }
                    Log.d(TAG, "capability $CAP_PHONE nodes=${info.nodes.size}")
                } catch (e: Exception) {
                    Log.w(TAG, "capability lookup failed", e)
                }

                // 2) 폴백: 연결된 모든 노드(=폰)로
                if (targets.isEmpty()) {
                    val nodes = Tasks.await(Wearable.getNodeClient(ctx).connectedNodes)
                    Log.d(TAG, "fallback connected nodes=${nodes.size}")
                    nodes.forEach { targets.add(it.id) }
                }

                if (targets.isEmpty()) {
                    Log.w(TAG, "No reachable phone node")
                    return@execute
                }

                val mc = Wearable.getMessageClient(ctx)
                for (id in targets) {
                    val reqId = Tasks.await(mc.sendMessage(id, path, data))
                    Log.d(TAG, "$path -> $id result=$reqId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendJson failed ($path)", e)
            }
        }
    }

    fun sendGameStart(context: Context, matchId: String, setNumber: Int) {
        val json = JSONObject()
            .put("eventId", UUID.randomUUID().toString())
            .put("type", "START")
            .put("matchId", matchId)
            .put("setNumber", setNumber)
            .put("timeStamp", System.currentTimeMillis())
            .toString()
        val data = json.toByteArray(Charsets.UTF_8)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val info = Tasks.await(
                    Wearable.getCapabilityClient(context)
                        .getCapability(CAP_PHONE, CapabilityClient.FILTER_REACHABLE)
                )
                val nodes = info.nodes
                if (nodes.isEmpty()) {
                    val connected = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
                    for (n in connected) {
                        val r = Tasks.await(Wearable.getMessageClient(context)
                            .sendMessage(n.id, PATH_EVENT_START, data))
                        Log.d(TAG, "START -> ${n.displayName} result=$r")
                    }
                } else {
                    for (n in nodes) {
                        val r = Tasks.await(Wearable.getMessageClient(context)
                            .sendMessage(n.id, PATH_EVENT_START, data))
                        Log.d(TAG, "START -> ${n.displayName} result=$r")
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "sendStart failed", t)
            }
        }
    }

    fun sendScore(ctx: Context, matchId: String, userScore: Int, oppScore: Int, setNumber: Int) {
        val json = JSONObject()
            .put("type", "SCORE")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAtUtc", System.currentTimeMillis())
            .put("matchId", matchId)
            .put("payload", JSONObject()
                .put("setNumber", setNumber)
                .put("userScore", userScore)
                .put("opponentScore", oppScore)
            )
        sendJson(ctx, PATH_EVENT_SCORE, json)
    }

    fun sendUndo(ctx: Context, matchId: String, setNumber: Int) {
        val json = JSONObject()
            .put("type", "UNDO")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAtUtc", System.currentTimeMillis())
            .put("matchId", matchId)
            .put("payload", JSONObject()
                .put("setNumber", setNumber))
        sendJson(ctx, PATH_EVENT_UNDO, json)
    }

    fun sendPause(ctx: Context, matchId: String) {
        val json = JSONObject()
            .put("type", "PAUSE")
            .put("eventId", UUID.randomUUID().toString())
            .put("timeStamp", System.currentTimeMillis())
            .put("matchId", matchId)
        sendJson(ctx, PATH_EVENT_PAUSE, json)
    }

    fun sendResume(ctx: Context, matchId: String) {
        val json = JSONObject()
            .put("type", "RESUME")
            .put("eventId", UUID.randomUUID().toString())
            .put("timeStamp", System.currentTimeMillis())
            .put("matchId", matchId)
        sendJson(ctx, PATH_EVENT_RESUME, json)
    }
}