package com.example.rally.presentation

import androidx.compose.foundation.Image
import com.example.rally.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

@Composable
fun FinishScreen(
) {
    val haptic = LocalHapticFeedback.current  // 진동 피드백

    Box(
        modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.black_bg)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_white),
                contentDescription = "rally",
                modifier = Modifier
                    .width(109.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.green_active)),
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(29.dp),

                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ) {
                Text(
                    text = "종료하기",
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_regular))
                )
            }
        }

    }
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun FinishScreenPreview() {
    FinishScreen(
    )
}