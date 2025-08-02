package com.example.rally.presentation
import com.example.rally.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

@Composable
fun StartScreen(
    setNumber: Int,
    opponentName: String,
    opponentScore: Int,
    opponentSets: Int,
    userName: String,
    userScore: Int,
    userSets: Int,
    onStart: () -> Unit
) {
    val haptic = LocalHapticFeedback.current  // 진동 피드백

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black_bg))
            .offset(y = -6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${setNumber}세트",
            fontSize = 12.sp,
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.pretendard_variable)),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(58.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$opponentName",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily(Font(R.font.pretendard_variable))
                )
                Text(
                    text = "$opponentScore",
                    fontSize = 46.sp,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$opponentSets",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                fontWeight = FontWeight.SemiBold
            )

            // 사용자 점수 영역
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "$userSets",
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.width(58.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$userName",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily(Font(R.font.pretendard_variable))
                )
                Text(
                    text = "$userScore",
                    fontSize = 46.sp,
                    color = colorResource(id = R.color.green_active),
                    fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 득점 버튼
        Button (
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)  // 진동 피드백
                onStart() },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.green_active)),
            modifier = Modifier
                .width(120.dp)
                .height(40.dp),
            shape = RoundedCornerShape(29.dp)
        ) {
            Text(
                text = "경기 시작",
                fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun StartScreenPreview() {
    StartScreen(
        setNumber = 1,
        opponentName = "아어려워요",
        opponentScore = 0,
        opponentSets = 0,
        userName = "안세영이되",
        userScore = 0,
        userSets = 0,
        onStart = {}
    )
}