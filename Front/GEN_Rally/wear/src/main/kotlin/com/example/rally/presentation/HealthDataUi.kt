package com.example.rally.presentation

import GameHealthPayload
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
fun HealthDataScreen( data: GameHealthPayload?
) {
    val steps  = data?.totalSteps ?: 0L
    val kcal   = data?.totalKcal ?: 0
    val maxBpm = data?.overallMaxBpm ?: 0
    val minBpm = data?.overallMinBpm ?: 0

    fun Long.pretty() = "%,d".format(this)
    fun Int.pretty()  = "%,d".format(this)
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
            // 걸음수
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_footprints),
                    contentDescription = "step count",
                    modifier = Modifier
                        .width(32.dp)
                )
                Spacer(modifier=Modifier.width(6.dp))
                Text(
                    text = steps.pretty(),
                    fontSize = 28.sp,
                    color = colorResource(id = R.color.green_active) ,
                    fontFamily = FontFamily(Font(R.font.kimm_bold))
                )
                Spacer(modifier=Modifier.width(3.dp))
                Text(
                    text = "걸음",
                    fontSize = 8.sp,
                    color = colorResource(id = R.color.gray_text2),
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    letterSpacing = (-0.04).em,
                    modifier = Modifier.offset(y=5.dp)
                )
            }
            Spacer(modifier=Modifier.height(12.dp))
            // 심박수
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_heartrate),
                    contentDescription = "step count",
                    modifier = Modifier
                        .width(32.dp)
                )
                Spacer(modifier=Modifier.width(6.dp))
                Text(
                    text = maxBpm.toString(),
                    fontSize = 28.sp,
                    color = colorResource(id = R.color.green_active) ,
                    fontFamily = FontFamily(Font(R.font.kimm_bold))
                )
                Spacer(modifier=Modifier.width(4.dp))
                Text(
                    text = "/$minBpm",
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.green_active) ,
                    fontFamily = FontFamily(Font(R.font.kimm_bold)),
                    modifier = Modifier.offset(y=4.dp)
                )
                Spacer(modifier=Modifier.width(3.dp))
                Text(
                    text = "bpm",
                    fontSize = 8.sp,
                    color = colorResource(id = R.color.gray_text2),
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    letterSpacing = (-0.04).em,
                    modifier = Modifier.offset(y=5.dp)
                )
            }
            Spacer(modifier=Modifier.height(12.dp))
            // 칼로리
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_calorie),
                    contentDescription = "calorie",
                    modifier = Modifier
                        .width(32.dp)
                )
                Spacer(modifier=Modifier.width(6.dp))
                Text(
                    text = kcal.pretty(),
                    fontSize = 28.sp,
                    color = colorResource(id = R.color.green_active) ,
                    fontFamily = FontFamily(Font(R.font.kimm_bold))
                )
                Spacer(modifier=Modifier.width(3.dp))
                Text(
                    text = "kcal",
                    fontSize = 8.sp,
                    color = colorResource(id = R.color.gray_text2),
                    fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                    letterSpacing = (-0.04).em,
                    modifier = Modifier.offset(y=5.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun HealthDataScreenPreview(
) {
//    HealthDataScreen(
//        data =
//    )
}