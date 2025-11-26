package com.example.rally.datalayer

import GameHealthPayload
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID

object WatchDataLayerClient {
    private const val TAG = "WatchDL"
    private const val CAP_PHONE = "rally_phone"     // 폰 capability
    private const val PATH_EVENT_SET_START = "/rally/event/set_start"
    private const val PATH_EVENT_SCORE = "/rally/event/score"
    private const val PATH_EVENT_UNDO = "/rally/event/undo"
    private const val PATH_EVENT_PAUSE = "/rally/event/pause"
    private const val PATH_EVENT_RESUME = "/rally/event/resume"
    private const val PATH_EVENT_SET_FINISH = "/rally/event/set_finish"
    private const val PATH_SNAPSHOT_REQ = "/rally/snapshot_req"
    private const val PATH_HEALTH_SUMMARY = "/rally/health_summary"

    // 노드 조회 + 메시지 전송
    private suspend fun sendBytes(ctx: Context, path: String, data: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                val targets = mutableListOf<String>()
                // capability 우선
                runCatching {
                    val info = Tasks.await(
                        Wearable.getCapabilityClient(ctx)
                            .getCapability(CAP_PHONE, CapabilityClient.FILTER_REACHABLE)
                    )
                    info.nodes.forEach { targets.add(it.id) }
                    Log.d(TAG, "capability $CAP_PHONE nodes=${targets.size}")
                }.onFailure {
                    Log.w(TAG, "capability lookup failed", it)
                }

                // 폴백: 연결 노드
                if (targets.isEmpty()) {
                    val nodes = Tasks.await(Wearable.getNodeClient(ctx).connectedNodes)
                    Log.d(TAG, "fallback connected nodes=${nodes.size}")
                    nodes.forEach { targets.add(it.id) }
                }
                if (targets.isEmpty()) {
                    Log.w(TAG, "No reachable phone node")
                    return@withContext
                }
                val mc = Wearable.getMessageClient(ctx)
                targets.forEach { id ->
                    val reqId = Tasks.await(mc.sendMessage(id, path, data))
                    Log.d(TAG, "$path -> $id result=$reqId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendBytes failed ($path)", e)
            }
        }
    }

    // json 전송
    private fun sendJson(context: Context, path: String, body: JSONObject) {
        val bytes = body.toString().toByteArray(StandardCharsets.UTF_8)
        CoroutineScope(Dispatchers.IO).launch {
            sendBytes(context, path, bytes)
        }
    }

    fun sendGameStart(context: Context, gameId: Long, setNumber: Int) {
        val json = JSONObject()
            .put("version", 1)
            .put("type", "SET_START")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAt", System.currentTimeMillis())
            .put("gameId", gameId)
            .put(
                "payload", JSONObject()
                    .put("setNumber", setNumber)
                    .put("startAt", System.currentTimeMillis())
            )
        val data = json.toString().toByteArray(Charsets.UTF_8)

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
                        val r = Tasks.await(
                            Wearable.getMessageClient(context)
                                .sendMessage(n.id, PATH_EVENT_SET_START, data)
                        )
                        Log.d(TAG, "START -> ${n.displayName} result=$r")
                    }
                } else {
                    for (n in nodes) {
                        val r = Tasks.await(
                            Wearable.getMessageClient(context)
                                .sendMessage(n.id, PATH_EVENT_SET_START, data)
                        )
                        Log.d(TAG, "START -> ${n.displayName} result=$r")
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "sendStart failed", t)
            }
        }
    }

    fun sendScore(
        context: Context,
        gameId: Long,
        userScore: Int,
        opponentScore: Int,
        setNumber: Int,
        localIsUser1: Boolean
    ) {
        // 로컬 관점 점수를 절대 좌표(user1/user2)로 변환
        val u1 = if (localIsUser1) userScore else opponentScore
        val u2 = if (localIsUser1) opponentScore else userScore
        val json = JSONObject()
            .put("version", 1)
            .put("type", "SCORE")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAt", System.currentTimeMillis())
            .put("gameId", gameId)
            .put(
                "payload", JSONObject()
                    .put("setNumber", setNumber)
                    .put("user1Score", u1)
                    .put("user2Score", u2)
            )
        sendJson(context, PATH_EVENT_SCORE, json)
    }

    fun sendUndo(
        context: Context,
        gameId: Long,
        userScore: Int,
        opponentScore: Int,
        setNumber: Int,
        localIsUser1: Boolean
    ) {
        val u1 = if (localIsUser1) userScore else opponentScore
        val u2 = if (localIsUser1) opponentScore else userScore
        val json = JSONObject()
            .put("version", 1)
            .put("type", "UNDO")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAt", System.currentTimeMillis())
            .put("gameId", gameId)
            .put(
                "payload", JSONObject()
                    .put("setNumber", setNumber)
                    .put("user1Score", u1)
                    .put("user2Score", u2)
            )
        sendJson(context, PATH_EVENT_UNDO, json)
    }

    fun sendPause(context: Context, gameId: Long) {
        val json = JSONObject()
            .put("version", 1)
            .put("type", "PAUSE")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAt", System.currentTimeMillis())
            .put("gameId", gameId)
            .put("payload", JSONObject()
                .put("timeStamp", System.currentTimeMillis())
            )
        sendJson(context, PATH_EVENT_PAUSE, json)
    }

    fun sendResume(context: Context, gameId: Long) {
        val json = JSONObject()
            .put("version", 1).put("type", "RESUME")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAtUtc", System.currentTimeMillis())
            .put("gameId", gameId)
            .put("payload", JSONObject()
                .put("timeStamp", System.currentTimeMillis())
            )
        sendJson(context, PATH_EVENT_RESUME, json)
    }

    fun sendSetFinish(
        context: Context,
        gameId: Long,
        setNumber: Int,
        userScore: Int,
        opponentScore: Int,
        elapsed: Long,
        isGameFinished: Boolean,
        localIsUser1: Boolean
    ) {
        val u1 = if (localIsUser1) userScore else opponentScore
        val u2 = if (localIsUser1) opponentScore else userScore
        val payload = JSONObject()
            .put("setNumber", setNumber)
            .put("user1Score", u1)
            .put("user2Score", u2)
            .put("elapsed", elapsed)
            .put("isGameFinished", isGameFinished)

        val json = JSONObject()
            .put("version", 1)
            .put("type", "SET_FINISH")
            .put("eventId", UUID.randomUUID().toString())
            .put("createdAt", System.currentTimeMillis())
            .put("gameId", gameId)
            .put("payload", payload)

        sendJson(context, PATH_EVENT_SET_FINISH, json)
    }

    fun requestSnapshot(context: Context, gameId: Long) {
        val json = org.json.JSONObject()
            .put("version", 1)
            .put("type", "SNAPSHOT_REQ")
            .put("eventId", java.util.UUID.randomUUID().toString())
            .put("createdAtUtc", System.currentTimeMillis())
            .put("gameId", gameId)
        sendJson(context, PATH_SNAPSHOT_REQ, json)
    }

    fun sendHealthSummary(
        context: Context,
        gameId: Long,
        payload: GameHealthPayload,
        localIsUser1: Boolean
    ) {
        try {
            val root = JSONObject().apply {
                put("version", 1)
                put("type", "HEALTH_SUMMARY")
                put("eventId", UUID.randomUUID().toString())
                put("createdAt", System.currentTimeMillis())
                put("gameId", gameId)

                val p = JSONObject().apply {
                    put("localIsUser1", localIsUser1)
                    put("totalSteps", payload.totalSteps)
                    put("totalKcal", payload.totalKcal)
                    put("overallMaxBpm", payload.overallMaxBpm ?: JSONObject.NULL)
                    put("overallMinBpm", payload.overallMinBpm ?: JSONObject.NULL)

                    val setsArr = org.json.JSONArray()
                    payload.perSet.forEachIndexed { index, set ->
                        val setObj = JSONObject().apply {
                            put("setNumber", index + 1)
                            put("maxHr", set.maxHeartRateBpm ?: JSONObject.NULL)
                            put("minHr", set.minHeartRateBpm ?: JSONObject.NULL)
                            put("steps", set.steps)
                            put("caloriesKcal", set.caloriesKcal)

                            val seriesArr = org.json.JSONArray()
                            set.heartSeries.forEach { sample ->
                                val s = JSONObject()
                                s.put("epochMs", sample.epochMs)
                                s.put("bpm", sample.bpm)
                                seriesArr.put(s)
                            }
                            put("heartSeries", seriesArr)
                        }
                        setsArr.put(setObj)
                    }
                    put("perSet", setsArr)
                }
                put("payload", p)
            }

            sendJson(context, PATH_HEALTH_SUMMARY, root)
        } catch (t: Throwable) {
            Log.e(TAG, "sendHealthSummary failed", t)
        }
    }
}