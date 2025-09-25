package com.example.rally.presentation

import com.example.rally.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Text

@Composable
fun ScoreResultScreen(
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.black_bg)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "경기 종료",
                fontSize = 12.sp,
                color = colorResource(id = R.color.gray_text2),
                fontFamily = FontFamily(Font(R.font.pretendard_medium))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.width(33.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "상대",
                        fontSize = 8.sp,
                        color = colorResource(id = R.color.gray_text2),
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        modifier = Modifier.widthIn(max = 50.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "2",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.kimm_bold)),
                        letterSpacing = (-0.06).em
                    )
                }
                Text(
                    text = ":",
                    fontSize = 28.sp,
                    color = colorResource(id = R.color.black_text),
                    fontFamily = FontFamily(Font(R.font.kimm_bold)),
                    letterSpacing = (-0.06).em
                )
                Column(
                    modifier = Modifier.width(33.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "나",
                        fontSize = 8.sp,
                        color =  colorResource(id = R.color.gray_text2),
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        modifier = Modifier.widthIn(max = 50.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "1",
                        fontSize = 28.sp,
                        color = colorResource(id = R.color.gray_text),
                        fontFamily = FontFamily(Font(R.font.kimm_bold)),
                        letterSpacing = (-0.06).em
                    )
                }
            }
            Spacer(modifier = Modifier.height(9.dp))
            // 1세트
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(31.dp)
                        .height(22.dp)
                        .background(
                            color = colorResource(id = R.color.green_active),
                            shape = RoundedCornerShape(
                                topStart = 6.dp,
                                bottomStart = 6.dp,
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text ="21",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .width(31.dp)
                        .height(22.dp)
                        .background(
                            color = colorResource(id = R.color.black_text),
                            shape = RoundedCornerShape(
                                topEnd = 6.dp,
                                bottomEnd = 6.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text ="21",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                        color = colorResource(id = R.color.gray_text)
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            // 2세트
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(31.dp)
                        .height(22.dp)
                        .background(
                            color = colorResource(id = R.color.black_text),
                            shape = RoundedCornerShape(
                                topStart = 6.dp,
                                bottomStart = 6.dp,
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text ="21",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                        color = colorResource(id = R.color.gray_text)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .width(31.dp)
                        .height(22.dp)
                        .background(
                            color = colorResource(id = R.color.green_active),
                            shape = RoundedCornerShape(
                                topEnd = 6.dp,
                                bottomEnd = 6.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text ="21",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            // 3세트
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(31.dp)
                        .height(22.dp)
                        .background(
                            color = colorResource(id = R.color.green_active),
                            shape = RoundedCornerShape(
                                topStart = 6.dp,
                                bottomStart = 6.dp,
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text ="21",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .width(31.dp)
                        .height(22.dp)
                        .background(
                            color = colorResource(id = R.color.black_text),
                            shape = RoundedCornerShape(
                                topEnd = 6.dp,
                                bottomEnd = 6.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text ="21",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_bold)),
                        color = colorResource(id = R.color.gray_text)
                    )
                }
            }
            Spacer(modifier=Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun ScoreResultScreenPreview() {
    ScoreResultScreen(
    )
}
