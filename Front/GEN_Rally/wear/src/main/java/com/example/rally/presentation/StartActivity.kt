package com.example.rally.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val setNumber = intent.getIntExtra("setNumber", 1)
        val userSets = intent.getIntExtra("userSets", 0)
        val opponentSets = intent.getIntExtra("opponentSets", 0)

        setContent {
            StartScreen(
                setNumber = setNumber,
                opponentSets= opponentSets,
                opponentName = "상대",
                opponentScore = 0,
                userName = "나",
                userScore = 0,
                userSets = userSets,
                onStart = {
                    val intent = Intent(this, ScoreSwipeActivity::class.java)
                    intent.putExtra("startTime", System.currentTimeMillis())  // 시작 시간 전달
                    intent.putExtra("setNumber", setNumber)
                    intent.putExtra("opponentSets", opponentSets)
                    intent.putExtra("userSets", userSets)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            )
        }
    }
}