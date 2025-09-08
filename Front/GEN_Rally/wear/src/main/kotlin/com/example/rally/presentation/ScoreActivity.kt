package com.example.rally.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class ScoreActivity : ComponentActivity() {
    private lateinit var vm: ScoreViewModel

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
                    }
                    "/rally/event/resume" -> {
                        val at = payload.optLong("timeStamp", 0L)
                        if (at > 0) vm.resumeAt(at) else vm.resume()
                    }
                    "/rally/event/set_finish" -> {
                        val setNum = payload.optInt("setNumber", vm.setNumber.value)
                        val u1 = payload.optInt("user1Score", -1)
                        val u2 = payload.optInt("user2Score", -1)
                        val isGameFinished = payload.optBoolean("isGameFinished", false)

                        // 스냅샷 맞추기
                        if (u1 >= 0 && u2 >= 0) {
                            val my = if (vm.isUser1.value) u1 else u2
                            val opp = if (vm.isUser1.value) u2 else u1
                            vm.applyScoreSnapshot(my, opp)
                        }

                        vm.applySetFinishFromRemote(
                            setNumberFinished = setNum,
                            user1Score = u1,
                            user2Score = u2,
                            isGameFinishedRemote = isGameFinished,
                            nextFirstServer = null
                        )
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("Watch/ScoreActivity", "relay parse error", t)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기 세팅 값
        val initialOpponentName = intent.getStringExtra("opponentName") ?: "아어려워요"
        val initialUserName = intent.getStringExtra("userName") ?: "안세영이되"
        val initialIsUser1 = intent.getBooleanExtra("localIsUser1", false)
        val initialSetNumber = intent.getIntExtra("setNumber", 1)
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
            var lastUserScore by rememberSaveable { mutableStateOf(0) }
            var lastOpponentScore by rememberSaveable { mutableStateOf(0) }
            var isGameFinished by rememberSaveable { mutableStateOf(false) }
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
                    val isUser1 by viewModel.isUser1.collectAsState()
                    val screen = if (!started || isSetFinished) "start" else "score"
                    val ctx = LocalContext.current
                    when (screen) {
                        "start" -> {
                            StartScreen(
                                setNumber = currentSetNumber,
                                opponentScore = if (!isGameFinished) 0 else lastOpponentScore,
                                opponentSets = opponentSets,
                                opponentName = opponentName,
                                userName = userName,
                                userScore = if (!isGameFinished) 0 else lastUserScore,
                                userSets = userSets,
                                isGameFinished = isGameFinished,
                                onStart = {
                                    viewModel.markStarted(true)
                                    viewModel.startSet(currentSetNumber, Player.USER1)
                                    viewModel.startStopwatchAt(System.currentTimeMillis())

                                    if (!isGameFinished) {
                                        // 폰으로 경기 시작 이벤트 전송
                                        WatchDataLayerClient.sendGameStart(
                                            context = ctx,
                                            matchId = "match-123",
                                            setNumber = currentSetNumber
                                        )
                                    } else {
                                        // 결과 화면
                                        startActivity(Intent(this@ScoreActivity, ResultSwipeActivity::class.java))
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
                                    // 세트 종료 결과
                                    lastUserScore = result.userScore
                                    lastOpponentScore = result.opponentScore
                                    isGameFinished = result.isGameFinished
                                    nextFirstServer = result.currentServer
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
        androidx.localbroadcastmanager.content.LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(watchRelayReceiver, f)

        WatchDataLayerClient.requestSnapshot(this, matchId = "match-123")
    }
    override fun onStop() {
        super.onStop()
        androidx.localbroadcastmanager.content.LocalBroadcastManager
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

    // 세트 종료시 2.5초 대기 후 다음 화면 이동
    var pendingResult by remember { mutableStateOf<SetResult?>(null) }
    LaunchedEffect(pendingResult) {
        pendingResult?.let { result ->
            delay(2500)
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
                            setNumber = result.nextSetNumber - 1,
                            userScore = result.userScore,
                            opponentScore = result.opponentScore,
                            elapsed = elapsed,              // 여기서 elapsed는 capture하거나 파라미터로 받기
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