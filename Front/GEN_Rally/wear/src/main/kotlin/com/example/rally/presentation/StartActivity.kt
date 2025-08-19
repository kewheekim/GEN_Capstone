package com.example.rally.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import com.example.rally.datalayer.WatchDataLayerClient
import com.example.rally.viewmodel.Player

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val setNumber = intent.getIntExtra("setNumber", 1)
        val opponentName = intent.getStringExtra("opponentName") ?: "상대"
        val opponentSets = intent.getIntExtra("opponentSets", 0)
        val opponentScore =  intent.getIntExtra("opponentScore", 0)
        val userName = intent.getStringExtra("userName") ?: "나"
        val userSets = intent.getIntExtra("userSets", 0)
        val userScore =  intent.getIntExtra("userScore", 0)
        val nextFirstServer = intent.getStringExtra("nextFirstServer") ?: Player.USER1.name
        val isGameFinished = intent.getBooleanExtra("isGameFinished", false)

        setContent {
            StartScreen(
                setNumber = setNumber,
                opponentName = opponentName,
                opponentSets= opponentSets,
                opponentScore =  if(!isGameFinished) 0 else opponentScore,
                userName = userName,
                userSets = userSets,
                userScore =  if(!isGameFinished) 0 else userScore,
                isGameFinished = isGameFinished,
                onStart = {
                    if(!isGameFinished) {
                        val intent = Intent(this, ScoreSwipeActivity::class.java). apply {
                            putExtra("startTime", System.currentTimeMillis())  // 시작 시간 전달
                            putExtra("setNumber", setNumber)
                            putExtra("opponentName", opponentName)
                            putExtra("opponentSets", opponentSets)
                            putExtra("userName", userName)
                            putExtra("userSets", userSets)
                            putExtra("nextFirstServer", nextFirstServer)
                        }

                        startActivity(intent)
                        overridePendingTransition(0, 0)
                        WatchDataLayerClient.sendGameStart(context = this@StartActivity , matchId = "match-123", setNumber = 1)
                    }
                    else {
                        val intent = Intent(this, ResultSwipeActivity::class.java)
                        startActivity(intent)
                    }

                }
            )
        }
    }
}