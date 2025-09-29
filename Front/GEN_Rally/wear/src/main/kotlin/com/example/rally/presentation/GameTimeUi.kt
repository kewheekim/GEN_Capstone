package com.example.rally.presentation

import com.example.rally.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

@Composable
fun GameTimeScreen(
    totalElapsed: Long,
    sets: List<SetSummary>
) {
    Box(
        modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.black_bg)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_clock),
            contentDescription = "clock icon",
            modifier = Modifier
                .align(Alignment.Center)
                .width(149.dp).offset(x=-44.dp, y=42.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "경기 시간",
                fontSize = 12.sp,
                color = colorResource(id = R.color.gray_text2),
                fontFamily = FontFamily(Font(R.font.pretendard_medium))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatTime(totalElapsed),
                fontSize = 28.sp,
                color = colorResource(id = R.color.green_active) ,
                fontFamily = FontFamily(Font(R.font.kimm_bold)),
                letterSpacing = (-0.04).em
            )
            Spacer(modifier=Modifier.height(16.dp))
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "세트",
                    fontSize = 8.sp,
                    color = colorResource(id = R.color.gray_inactive),
                    fontFamily = FontFamily(Font(R.font.pretendard_regular))
                )
                Spacer(modifier=Modifier.width(60.dp))
            }
            Spacer(modifier=Modifier.height(5.dp))
            sets.forEach { s ->
                Spacer(Modifier.height(13.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = s.setNumber.toString(),
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.white),
                        fontFamily = FontFamily(Font(R.font.pretendard_regular))
                    )
                    Spacer(Modifier.width(18.dp))
                    Text(
                        text = formatTime(s.elapsedSec),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular))
                    )
                }
            }
            Spacer(modifier=Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun GameTimeScreenPreview() {
}
