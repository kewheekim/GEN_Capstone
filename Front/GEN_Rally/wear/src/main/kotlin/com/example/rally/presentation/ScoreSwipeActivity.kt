package com.example.rally.presentation

import androidx.activity.ComponentActivity
import com.example.rally.R
import com.example.rally.viewmodel.ScoreViewModel
import com.example.rally.viewmodel.SetResult
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rally.viewmodel.Player
import kotlinx.coroutines.delay

class ScoreSwipeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startTime = intent.getLongExtra("startTime", System.currentTimeMillis())
        val setNumber = intent.getIntExtra("setNumber", 1)
        val opponentSets = intent.getIntExtra("opponentSets", 0)
        val userSets = intent.getIntExtra("userSets", 0)

        val nextFirstServerStr = intent.getStringExtra("nextFirstServer")
        val nextFirstServer = nextFirstServerStr?.let { com.example.rally.viewmodel.Player.valueOf(it) }
            ?: com.example.rally.viewmodel.Player.USER1
        setContent {
            ScoreSwipeScreen(startTime, setNumber, opponentSets, userSets, nextFirstServer)
        }
    }
}

@Composable
fun ScoreSwipeScreen(startTime: Long, setNumber: Int, opponentSets:Int, userSets:Int, nextFirstServer: Player) {
    val pagerState = rememberPagerState (pageCount = { 2 })

    val viewModel: ScoreViewModel = viewModel()
    val elapsed by viewModel.elapsed.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val context = LocalContext.current

    var navigateToStartActivity by remember { mutableStateOf<SetResult?>(null) }

    LaunchedEffect(Unit) {
        viewModel.initSets(userSets, opponentSets)
        viewModel.initPlayer(isUser1 = false)
        val firstServer =
            if (setNumber == 1) Player.USER1 else nextFirstServer
        viewModel.startSet(setNumber, firstServer)
        viewModel.startStopwatch()
    }

    LaunchedEffect(navigateToStartActivity) {
        navigateToStartActivity?.let { result ->
            delay(2500)
            val intent = Intent(context, StartActivity::class.java).apply {
                putExtra("setNumber", result.nextSetNumber)
                putExtra("userSets", result.userSets)
                putExtra("userScore", result.userScore)
                putExtra("opponentSets", result.opponentSets)
                putExtra("opponentScore", result.opponentScore)
                putExtra("nextFirstServer", result.currentServer.name)
                putExtra("isGameFinished", result.isGameFinished)
            }
            context.startActivity(intent)
            (context as? ComponentActivity)?.overridePendingTransition(0, 0)
            (context as? ComponentActivity)?.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black_bg))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> ScoreScreen(
                        setNumber = setNumber,
                        opponentName = "상대",
                        userName = "나",
                        viewModel = viewModel,
                        onSetFinished = { result ->
                            navigateToStartActivity = result
                        }
                    )
                    1 -> PauseScreen(
                        elapsedTime = elapsed,
                        isPaused = isPaused,
                        onPause = {
                            if (isPaused) viewModel.resume()
                            else viewModel.pause()
                        }
                    )
                }
            }

            // 현재 페이지에 따라 이미지 선택
            val indicatorImage = when (pagerState.currentPage) {
                0 -> R.drawable.ic_dot1
                else -> R.drawable.ic_dot2
            }

            Image(
                painter = painterResource(id = indicatorImage),
                contentDescription = "Page Indicator",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(18.dp).offset(y=-12.dp)
            )
        }
    }
}