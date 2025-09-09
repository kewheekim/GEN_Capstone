package com.example.rally.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.wear.compose.material.Text
import com.example.rally.R
import kotlinx.coroutines.delay

@Composable
fun ScoreEventToast(
    title: String,
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        // 2초 후 종료
        LaunchedEffect(Unit) {
            delay(2000)
            onDismiss()
        }

        Box(
            modifier = Modifier .fillMaxSize().zIndex(999f).offset(y=32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xE6525252),
                        shape = RoundedCornerShape(50.dp),
                    )
                    .width(138.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        color = colorResource(id = R.color.green_active),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_extrabold))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false, widthDp = 192, heightDp = 192)
@Composable
fun ScoreEventToastPreview() {
    ScoreEventToast(
        title= "세트 종료",
        message= "1세트 선취!\n" +
                "시작이 좋아요!",
        visible = true,
        onDismiss = {}
    )
}