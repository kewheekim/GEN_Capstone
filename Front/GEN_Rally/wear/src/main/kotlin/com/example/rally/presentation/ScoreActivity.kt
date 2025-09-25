package com.example.rally.presentation

import HealthSessionManager
import SetHealthSummary
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.rally.R
import com.example.rally.datalayer.WatchDataLayerClient
import com.example.rally.datalayer.WatchDataLayerListener
import com.example.rally.viewmodel.Player
import com.example.rally.viewmodel.ScoreViewModel
import com.example.rally.viewmodel.SetResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScoreActivity : ComponentActivity() {
    private lateinit var vm: ScoreViewModel

    private lateinit var health: HealthSessionManager
    private var hsRunning: Boolean = false
    private val setHealthResults = mutableListOf<SetHealthSummary>()
    private val healthPermLauncher = registerForActivityResult (
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it == true }
        if (granted) {
            lifecycleScope.launch {
                if (!hsRunning) {
                    hsRunning = true
                    runCatching { health.beginSet() }
                        .onSuccess { Log.d("HS", "beginSet OK (after grant)") }
                        .onFailure { e ->
                            hsRunning = false
                            Log.e("HS", "beginSet FAILED even after grant", e)
                        }
                }
            }
        } else {
            Log.w("HS", "Permissions denied: can't start Health Services")
            hsRunning = false
        }
    }
    private fun ensureHsPermissions(onGranted: () -> Unit) {
        val needed = buildList {
            if (ContextCompat.checkSelfPermission(
                    this@ScoreActivity, android.Manifest.permission.BODY_SENSORS
                ) != PackageManager.PERMISSION_GRANTED
            ) add(android.Manifest.permission.BODY_SENSORS)

            if (ContextCompat.checkSelfPermission(
                    this@ScoreActivity, android.Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) add(android.Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (needed.isEmpty()) {
            onGranted()
        } else {
            healthPermLauncher.launch(needed.toTypedArray())
        }
    }

    private val watchRelayReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(
            context: android.content.Context,
            intent: android.content.Intent
        ) {
            Log.d("Watch/ScoreActivity", "BRIDGE <- " + (intent.getStringExtra("path")) + " : " + (intent.getStringExtra("json")))
            if (intent.action != "rally.EVENT_FROM_PHONE") return
            val path = intent.getStringExtra("path") ?: return
            val json = intent.getStringExtra("json") ?: "{}"

            try {
                val obj = org.json.JSONObject(json)
                val payload = obj.optJSONObject("payload") ?: org.json.JSONObject()
                when (path) {
                    "/rally/event/set_start" -> {
                        val setNum = payload.optInt("setNumber", vm.setNumber.value)
                        val firstServer = if ((payload?.optString("firstServer", "USER1") ?: "USER1") == "USER1") Player.USER1 else Player.USER2
                        val startAt = payload.optLong("startAt", System.currentTimeMillis())
                        vm.markStarted(true)
                        vm.startSet(setNum, firstServer)
                        vm.startStopwatchAt(startAt)

                        // 헬스 데이터 측정 시작
                        ensureHsPermissions {
                            lifecycleScope.launch {
                                if (!hsRunning) {
                                    hsRunning = true
                                    runCatching { health.beginSet() }
                                        .onSuccess { Log.d("HS", "beginSet OK") }
                                        .onFailure { e ->
                                            hsRunning = false
                                            Log.e("HS", "beginSet FAILED (permission/capability?)", e)
                                        }
                                }
                            }
                        }
                    }
                    "/rally/snapshot" -> {
                        val p = obj.optJSONObject("payload") ?: org.json.JSONObject()
                        val setNum = p.optInt("setNumber", vm.setNumber.value)
                        val u1 = p.optInt("user1Score", -1)
                        val u2 = p.optInt("user2Score", -1)
                        val u1Sets = p.optInt("user1Sets", 0)
                        val u2Sets = p.optInt("user2Sets", 0)
                        val server = if (p.optString(
                                "currentServer",
                                "USER1"
                            ) == "USER2"
                        ) Player.USER2 else Player.USER1

                        vm.markStarted(true)
                        val mySets   = if (vm.isUser1.value) u1Sets else u2Sets
                        val oppSets  = if (vm.isUser1.value) u2Sets else u1Sets
                        vm.initSets(mySets, oppSets)
                        vm.startSet(setNum, server)

                        if (u1 >= 0 && u2 >= 0) {
                            val my = if (vm.isUser1.value) u1 else u2
                            val opp = if (vm.isUser1.value) u2 else u1
                            vm.applyScoreSnapshot(my, opp)
                        }
                    }
                    "/rally/event/score" -> {
                        // 스냅샷 반영
                        val u1 = payload.optInt("user1Score", -1)
                        val u2 = payload.optInt("user2Score", -1)
                        val scorer = payload.optString("scorer", "")

                        if (u1 >= 0 && u2 >= 0) {
                            val my = if (vm.isUser1.value) u1 else u2
                            val opp = if (vm.isUser1.value) u2 else u1
                            vm.applyScoreSnapshot(my, opp)

                            if (scorer == "user1" || scorer == "user2") {
                                val pre = vm.currentServer.value ?: Player.USER1;
                                val scorerAbs = if (scorer == "user1") Player.USER1 else Player.USER2
                                vm.noteRally(pre, scorerAbs);
                            }
                        } else {  // 델타값 반영(score 오류 방지)
                            val scorer = payload.optString("scorer", "")
                            val mine = (vm.isUser1.value && scorer == "user1") || (!vm.isUser1.value && scorer == "user2")
                            if (mine) vm.addUserScore() else vm.applyOpponentScoreDelta(+1)
                        }
                    }
                    "/rally/event/undo" -> {
                        val u1 = payload.optInt("user1Score", -1)
                        val u2 = payload.optInt("user2Score", -1)
                        if (u1 >= 0 && u2 >= 0) {
                            val my = if (vm.isUser1.value) u1 else u2
                            val opp = if (vm.isUser1.value) u2 else u1
                            vm.applyScoreSnapshot(my, opp)
                        } else {  // 델타값 반영
                            vm.applyOpponentScoreDelta(-1)
                        }
                    }
                    "/rally/event/pause" -> {
                        val at = payload.optLong("timeStamp", 0L)
                        if (at > 0) vm.pauseAt(at) else vm.pause()

                        lifecycleScope.launch { runCatching { health.pauseSet() }}
                    }
                    "/rally/event/resume" -> {
                        val at = payload.optLong("timeStamp", 0L)
                        if (at > 0) vm.resumeAt(at) else vm.resume()

                        lifecycleScope.launch { runCatching { health.resumeSet() }}
                    }
                    "/rally/event/set_finish" -> {
                        val setNum = payload.optInt("setNumber", vm.setNumber.value)
                        val u1 = payload.optInt("user1Score", -1)
                        val u2 = payload.optInt("user2Score", -1)
                        val isGameFinished = payload.optBoolean("isGameFinished", false)
                        val winnerStr = payload.optString("winner", "")
                        // sets 반영
                        val u1Sets = payload.optInt("user1Sets", -1)
                        val u2Sets = payload.optInt("user2Sets", -1)

                        // 스냅샷 맞추기
                        if (u1 >= 0 && u2 >= 0) {
                            val my = if (vm.isUser1.value) u1 else u2
                            val opp = if (vm.isUser1.value) u2 else u1
                            vm.applyScoreSnapshot(my, opp)
                        }

                        val winner: Player? = when {
                            (u1 >= 0 && u2 >= 0) && (u1 != u2) ->
                                if (u1 > u2) Player.USER1 else Player.USER2
                            winnerStr.equals("user1", true) -> Player.USER1
                            winnerStr.equals("user2", true) -> Player.USER2
                            else -> {
                                //  로컬 점수 비교
                                val my = vm.userScore.value
                                val opp = vm.opponentScore.value
                                if (vm.isUser1.value) {
                                    if (my > opp) Player.USER1 else if (opp > my) Player.USER2 else null
                                } else {
                                    if (my > opp) Player.USER2 else if (opp > my) Player.USER1 else null
                                }
                            }
                        }
                        vm.applySetFinishFromRemote(
                            setNumberFinished = setNum,
                            user1Score = u1,
                            user2Score = u2,
                            isGameFinishedRemote = isGameFinished,
                            nextFirstServer = winner,
                            user1Sets = u1Sets,
                            user2Sets = u2Sets
                        )

                        //  헬스데이터 측정 종료, 요약
                        lifecycleScope.launch {
                            if (hsRunning) {
                                val summary = runCatching { health.endSet() }
                                    .onSuccess { hsRunning = false }
                                    .onFailure { e ->
                                        hsRunning = false
                                        Log.e("HS", "endSet() failed from broadcast", e)
                                    }
                                    .getOrNull()

                                summary?.let {
                                    setHealthResults += it
                                    Log.d("HS", "avgBpm=${it.minHeartRateBpm} steps=${it.steps} kcal=${it.caloriesKcal}")
                                }
                            } else {
                                Log.w("HS", "broadcast endSet skipped: hsRunning=false")
                            }
                        }
                    }
                    "/rally/event/game_finish" -> {
                        vm.pause()
                        vm.resetStopwatch()
                        vm.markStarted(false)
                        vm.markGameFinished(true)

                        lifecycleScope.launch {
                            if (hsRunning) {
                              runCatching { health.endSet() }
                              hsRunning = false
                        }
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e("Watch/ScoreActivity", "릴레이 파싱 에러", t)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기 세팅 값
        val initialOpponentName = intent.getStringExtra("opponentName") ?: "상대"
        val initialUserName = intent.getStringExtra("userName") ?: "나"
        val initialIsUser1 = intent.getBooleanExtra("localIsUser1", false)
        health = HealthSessionManager(this)
        vm = androidx.lifecycle.ViewModelProvider(this)[ScoreViewModel::class.java]
        vm.initSets(0, 0)
        vm.initPlayer(isUser1 = initialIsUser1)
        vm.setNames(user = initialUserName, opponent = initialOpponentName)

        setContent {
            //  스와이프
            val nav = rememberSwipeDismissableNavController()
            //  단일 ScoreViewModel
            val viewModel = vm
            val currentSetNumber by viewModel.setNumber.collectAsState()
            val userScore by viewModel.userScore.collectAsState()
            val opponentScore by viewModel.opponentScore.collectAsState()
            val isGameFinished by viewModel.isGameFinished.collectAsState()
            var nextFirstServer by rememberSaveable { mutableStateOf(Player.USER1) }
            val opponentSets by viewModel.opponentSets.collectAsState()
            val opponentName by viewModel.opponentName.collectAsState()
            val userSets by viewModel.userSets.collectAsState()
            val userName by viewModel.userName.collectAsState()

            SwipeDismissableNavHost(
                navController = nav,
                startDestination = "container"
            ) {
                // 시작/요약 화면
                composable("container") {
                    val started by viewModel.hasStarted.collectAsState()
                    val isSetFinished by viewModel.isSetFinished
                    val screen = if (!started || isSetFinished) "start" else "score"
                    val ctx = LocalContext.current
                    val displayNextSet = userSets + opponentSets + 1

                    when (screen) {
                        "start" -> {
                            StartScreen(
                                setNumber = displayNextSet,
                                opponentScore = if (!isGameFinished) 0 else opponentScore,
                                opponentSets = opponentSets,
                                opponentName = opponentName,
                                userName = userName,
                                userScore = if (!isGameFinished) 0 else userScore,
                                userSets = userSets,
                                isGameFinished = isGameFinished,
                                onStart = {
                                    if (!isGameFinished) {
                                        viewModel.markStarted(true)
                                        viewModel.startSet(displayNextSet, Player.USER1)
                                        viewModel.startStopwatchAt(System.currentTimeMillis())
                                        // 헬스 데이터 측정 시작
                                        ensureHsPermissions {
                                            lifecycleScope.launch {
                                                if (!hsRunning) {
                                                    hsRunning = true
                                                    runCatching { health.beginSet() }
                                                        .onSuccess { Log.d("HS", "beginSet OK") }
                                                        .onFailure { e ->
                                                            hsRunning = false
                                                            Log.e("HS", "beginSet FAILED", e)
                                                        }
                                                }
                                            }
                                        }
                                        // 폰으로 경기 시작 이벤트 전송
                                        WatchDataLayerClient.sendGameStart(
                                            context = ctx,
                                            matchId = "match-123",
                                            setNumber = displayNextSet
                                        )
                                    } else {
                                        // 결과 화면
                                        if (setHealthResults.isNotEmpty()) {
                                            val payload = GameHealthPayload.from(setHealthResults)
                                            val intent = Intent(this@ScoreActivity, ResultActivity::class.java)
                                                .putExtra("health_payload", payload)
                                            startActivity(intent)
                                        }
                                    }
                                }
                            )
                        }
                        "score" -> {
                            // 점수/일시정지(스와이프) 화면
                            ScorePager(
                                setNumber = currentSetNumber,
                                viewModel = viewModel,
                                onSetFinished = { result, elapsedSec ->
                                    lifecycleScope.launch {
                                        if (hsRunning) {
                                            val summary = runCatching { health.endSet() }
                                                .onSuccess { hsRunning = false }
                                                .onFailure { e ->
                                                    hsRunning = false
                                                    Log.e("HS", "endSet() failed", e)
                                                }
                                                .getOrNull()

                                            summary?.let {
                                                setHealthResults += it
                                                Log.d("HS", "avgBpm=${it.minHeartRateBpm} steps=${it.steps} kcal=${it.caloriesKcal}")
                                            }
                                        } else {
                                            Log.w("HS", "endSet() skipped: hsRunning=false")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val f = android.content.IntentFilter(WatchDataLayerListener.ACTION_PHONE_EVENT)
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(watchRelayReceiver, f)

        WatchDataLayerClient.requestSnapshot(this, matchId = "match-123")
        lifecycleScope.launch {
            hsRunning = runCatching { health.ownsActiveExercise() }.getOrDefault(false)
            Log.d("HS", "onStart sync: hsRunning=$hsRunning")
        }
    }
    override fun onStop() {
        super.onStop()
         LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(watchRelayReceiver)
    }
}

@Composable
private fun ScorePager(
    setNumber: Int,
    viewModel: ScoreViewModel,
    onSetFinished: (SetResult, Long) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val elapsed by viewModel.elapsed.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val isUser1 by viewModel.isUser1.collectAsState()
    val context = LocalContext.current

    // 세트 종료시 3초 대기 후 다음 화면 이동
    var pendingResult by remember { mutableStateOf<SetResult?>(null) }
    LaunchedEffect(pendingResult) {
        pendingResult?.let { result -> delay(3000)

            val winner = if (result.userScore > result.opponentScore) {
                if (viewModel.isUser1.value) Player.USER1 else Player.USER2
            } else {
                if (viewModel.isUser1.value) Player.USER2 else Player.USER1
            }
            viewModel.applySetFinishLocal(
                finishedSetNumber = viewModel.setNumber.value, // 방금 끝난 세트 번호
                winner = winner,
                nextFirstServer = result.currentServer,
                gameFinished = result.isGameFinished
            )
            onSetFinished(result, elapsed)

            pendingResult = null
        }
    }

    // ScoreUi.kt에서 onSetFinished(viewModel.onSetFinished())를 호출
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black_bg))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> ScoreScreen(
                    setNumber = setNumber,
                    viewModel = viewModel,
                    onSetFinished = { result ->
                        viewModel.pause()
                        WatchDataLayerClient.sendSetFinish(
                            context = context,
                            matchId = "match-123",
                            setNumber = viewModel.setNumber.value ,
                            userScore = result.userScore,
                            opponentScore = result.opponentScore,
                            elapsed = elapsed,
                            isGameFinished = result.isGameFinished,
                            localIsUser1 = isUser1
                        )
                        pendingResult = result
                    }
                )
                1 -> PauseScreen(
                    elapsedTime = elapsed,
                    isPaused = isPaused,
                    onPause = {
                        if (isPaused) {
                            // 폰으로 이벤트 전송
                            WatchDataLayerClient.sendResume(context, matchId = "match-123")
                        } else {
                            WatchDataLayerClient.sendPause(context, matchId = "match-123")
                        }
                    }
                )
            }
        }

        // 하단 페이지 인디케이터
        val indicatorImage = when (pagerState.currentPage) {
            0 -> R.drawable.ic_dot1
            else -> R.drawable.ic_dot2
        }
        Image(
            painter = painterResource(id = indicatorImage),
            contentDescription = "Page Indicator",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(18.dp)
                .offset(y = (-12).dp)
        )
    }
}