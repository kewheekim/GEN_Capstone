package com.example.rally.presentation

import android.content.Intent
import android.os.Bundle
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.rally.R
import com.example.rally.datalayer.WatchDataLayerClient
import com.example.rally.viewmodel.Player
import com.example.rally.viewmodel.ScoreViewModel
import com.example.rally.viewmodel.SetResult
import kotlinx.coroutines.delay

class ScoreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기 세팅 값
        val initialOpponentName = intent.getStringExtra("opponentName") ?: "상대"
        val initialUserName = intent.getStringExtra("userName") ?: "나"

        setContent {
            //  스와이프
            val nav = rememberSwipeDismissableNavController()
            //  단일 ScoreViewModel
            val viewModel: ScoreViewModel = viewModel()
            var currentSetNumber by rememberSaveable { mutableStateOf(1) }
            var lastUserScore by rememberSaveable { mutableStateOf(0) }
            var lastOpponentScore by rememberSaveable { mutableStateOf(0) }
            var isGameFinished by rememberSaveable { mutableStateOf(false) }
            var nextFirstServer by rememberSaveable { mutableStateOf(Player.USER1) }
            val opponentSets by viewModel.opponentSets.collectAsState()
            val userSets by viewModel.userSets.collectAsState()

            // ViewModel 초기화 (세트 수/플레이어 위치 등)
            LaunchedEffect(Unit) {
                viewModel.initSets(0, 0)
                viewModel.initPlayer(isUser1 = false)
                viewModel.setNames(user=initialUserName, opponent = initialOpponentName)
            }

            SwipeDismissableNavHost(
                navController = nav,
                startDestination = "container"
            ) {
                // 시작/요약 화면
                composable("container") {
                    var screen by rememberSaveable { mutableStateOf("start") }
                    val ctx = LocalContext.current
                    when (screen) {
                        "start" -> {
                            StartScreen(
                                setNumber = currentSetNumber,
                                opponentName = initialOpponentName,
                                opponentScore = if (!isGameFinished) 0 else lastOpponentScore,
                                opponentSets = opponentSets,
                                userName = initialUserName,
                                userScore = if (!isGameFinished) 0 else lastUserScore,
                                userSets = userSets,
                                isGameFinished = isGameFinished,
                                onStart = {
                                    if (!isGameFinished) {
                                        // 세트 시작
                                        viewModel.startSet(currentSetNumber, nextFirstServer)
                                        viewModel.startStopwatch()
                                        // 폰으로 경기 시작 이벤트 전송
                                        WatchDataLayerClient.sendGameStart(
                                            context = ctx,
                                            matchId = "match-123",
                                            setNumber = currentSetNumber
                                        )
                                        screen = "score"
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
                                opponentName = initialOpponentName,
                                userName = initialUserName,
                                viewModel = viewModel,
                                onSetFinished = { result ->
                                    // 세트 종료 결과
                                    lastUserScore = result.userScore
                                    lastOpponentScore = result.opponentScore
                                    isGameFinished = result.isGameFinished
                                    nextFirstServer = result.currentServer
                                    currentSetNumber = result.nextSetNumber
                                    screen = "start"   // StartUi 화면으로 전환
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScorePager(
    setNumber: Int,
    opponentName: String,
    userName: String,
    viewModel: ScoreViewModel,
    onSetFinished: (SetResult) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val elapsed by viewModel.elapsed.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val context = LocalContext.current

    // 세트 종료시 2.5초 대기 후 다음 화면 이동
    var pendingResult by remember { mutableStateOf<SetResult?>(null) }
    LaunchedEffect(pendingResult) {
        pendingResult?.let { result ->
            delay(2500)
            onSetFinished(result)
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
                    opponentName = opponentName,
                    userName = userName,
                    viewModel = viewModel,
                    onSetFinished = { result ->
                        viewModel.setFinished()
                        viewModel.pause()
                        pendingResult = result
                    }
                )
                1 -> PauseScreen(
                    elapsedTime = elapsed,
                    isPaused = isPaused,
                    onPause = {
                        if (isPaused) {
                            viewModel.resume()
                            // 워치로 이벤트 전송
                            WatchDataLayerClient.sendResume(context, matchId = "match-123")
                        } else {
                            viewModel.pause()
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