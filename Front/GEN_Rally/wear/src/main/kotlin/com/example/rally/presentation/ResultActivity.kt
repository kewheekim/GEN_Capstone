package com.example.rally.presentation

import GameHealthPayload
import com.example.rally.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class SetSummary (
    val setNumber: Int,
    val user1Score: Int,
    val user2Score: Int,
    val elapsedSec: Long
)

private fun parseSets (json: String): List<SetSummary> {
    val arr = org.json.JSONArray(json)
    val out = mutableListOf<SetSummary>()
    for (i in 0 until arr.length()) {
        val o = arr.optJSONObject(i) ?: continue
        out += SetSummary(
            setNumber   = o.optInt("setNumber", i + 1),
            user1Score  = o.optInt("user1Score", 0),
            user2Score  = o.optInt("user2Score", 0),
            elapsedSec  = o.optLong("elapsedSec", 0L)
        )
    }
    return out
}
private fun fmtTime(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}

class ResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val healthData = intent.getParcelableExtra<GameHealthPayload>("healthData")
        val setsJson = intent.getStringExtra("setsJson") ?: "[]"
        val totalElapsed = intent.getLongExtra("totalElapsed", 0L)
        val sets = parseSets(setsJson)

        val isUser1 = intent.getBooleanExtra("localIsUser1", true)
        val userName = intent.getStringExtra("userName") ?: "나"
        val opponentName = intent.getStringExtra("opponentName") ?: "상대"

        setContent {
            ResultPager(
                sets = sets,
                totalElapsed = totalElapsed,
                health = healthData,
                isUser1 = isUser1,
                userName = userName,
                opponentName = opponentName
            )
        }
    }

    @Composable
    private fun ResultPager(
        sets: List<SetSummary>,
        totalElapsed: Long,
        health: GameHealthPayload?,
        isUser1: Boolean,
        userName: String,
        opponentName: String
    ) {
        val pagerState = rememberPagerState(pageCount = { 4 })

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
                    0 -> ScoreResultScreen(
                        sets = sets,
                        isUser1 = isUser1,
                        userName = userName,
                        opponentName = opponentName
                    )

                    1 -> GameTimeScreen(
                        totalElapsed = totalElapsed,
                        sets = sets
                    )

                    2 -> HealthDataScreen(
                        data = health
                    )

                    3 -> FinishScreen(

                    )
                }
            }

            // 하단 페이지 인디케이터
            val indicatorImage = when (pagerState.currentPage) {
                0 -> R.drawable.ic_dot3
                1 -> R.drawable.ic_dot4
                2 -> R.drawable.ic_dot5
                else -> R.drawable.ic_dot6
            }
            Image(
                painter = painterResource(id = indicatorImage),
                contentDescription = "Page Indicator",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(41.dp)
                    .offset(y = -16.dp)
            )
        }
    }
}