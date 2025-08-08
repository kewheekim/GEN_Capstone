package com.example.rally.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import com.example.rally.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun PauseScreen(
    elapsedTime: Long,
    isPaused: Boolean,
    onPause: () -> Unit
) {
    val haptic = LocalHapticFeedback.current  // 진동 피드백
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black_bg)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "경기 시간",
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTime(elapsedTime),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onPause()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)  // 진동 피드백
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.green_active)),
                modifier = Modifier.width(120.dp).height(48.dp),
                shape = RoundedCornerShape(29.dp)
            ) {
                Text(
                    text = if (isPaused) "경기 재개" else "경기 일시정지",
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun PauseScreenPreview() {
    PauseScreen(
        elapsedTime = 2000L,
        isPaused = true,
        onPause = {})
}