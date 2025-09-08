package com.example.rally.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.example.rally.R
import com.example.rally.datalayer.WatchDataLayerClient
import com.example.rally.viewmodel.Player
import com.example.rally.viewmodel.ScoreViewModel
import com.example.rally.viewmodel.SetResult
import kotlinx.coroutines.launch

@Composable
fun ScoreScreen(
    setNumber: Int,
    viewModel: ScoreViewModel,
    onSetFinished: (SetResult) -> Unit
) {
    val context = LocalContext.current
    val userScore by viewModel.userScore.collectAsState()
    val userSets by viewModel.userSets.collectAsState()
    val opponentScore by viewModel.opponentScore.collectAsState()
    val opponentSets by viewModel.opponentSets.collectAsState()
    val isUser1 by viewModel.isUser1.collectAsState()
    val currentServer by viewModel.currentServer.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val opponentName by viewModel.opponentName.collectAsState()
    val serveOnRight = (currentServer == Player.USER1 && isUser1) || (currentServer == Player.USER2 && !isUser1)

    val isPaused by viewModel.isPaused.collectAsState()
    val isSetFinished by viewModel.isSetFinished
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current  // 진동 피드백
    var showToast by remember { mutableStateOf(false) }  // 경기 이벤트 토스트
    var toastTitle by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf("") }

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
                text = "${setNumber}세트",
                fontSize = 12.sp,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.pretendard_medium))
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
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        modifier = Modifier.widthIn(max = 50.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$opponentScore",
                        fontSize = 46.sp,
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.pretendard_extrabold))
                    )
                }

                if (!serveOnRight) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_serve),
                        contentDescription = "User Serve",
                        modifier = Modifier
                            .width(8.dp)
                            .offset(x = 4.dp, y = 18.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "$opponentSets",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.pretendard_semibold))
                )

                if (checkMatchPoint(userScore, opponentScore)) {
                    Text(
                        text = "Match\nPoint!",
                        fontSize = 8.sp,
                        color = colorResource(id = R.color.green_active),
                        fontFamily = FontFamily(Font(R.font.pretendard_extrabold)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y=16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // 사용자 점수 영역
                Text(
                    text = "$userSets",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.pretendard_semibold))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (serveOnRight) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_serve),
                            contentDescription = "Opponent Serve",
                            modifier = Modifier
                                .width(8.dp)
                                .offset(x = 2.dp, y = 18.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Column(
                    modifier = Modifier.width(58.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$userName",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily(Font(R.font.pretendard_regular)),
                        modifier = Modifier.widthIn(max = 50.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$userScore",
                        fontSize = 46.sp,
                        color = colorResource(id = R.color.green_active),
                        fontFamily = FontFamily(Font(R.font.pretendard_extrabold))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            // 득점 버튼
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.green_active)),
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(29.dp),

                onClick = {
                    if (isPressed) return@Button
                    isPressed=true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)  // 진동 피드백
                    viewModel.addUserScore()
                    // 폰으로 전송
                    WatchDataLayerClient.sendScore(
                        context = context,
                        matchId = "match-123",
                        userScore = userScore + 1, opponentScore = opponentScore,
                        setNumber = setNumber, localIsUser1 = isUser1)

                    //세트 종료
                    if (checkSetWin(userScore + 1, opponentScore)) {
                        toastTitle = "세트 종료"
                        if(userSets==0)
                            toastMessage = "1세트 선취!\n시작이 좋아요!"
                        else if(userSets==1 && opponentSets==0)
                            toastMessage = "2세트 연속 획득!\n승리했습니다!"
                        else if(userSets==1 && opponentSets==1)
                            toastMessage= "\n승리했습니다!"
                        showToast = true

                        viewModel.setFinished()  // 세트 종료 처리
                        viewModel.pause()
                        val result = viewModel.computeSetResult()
                        onSetFinished(result)

                    } else if (checkMatchPoint(userScore + 1, opponentScore)) {
                        toastTitle = "Match Point!"
                        toastMessage = "이번 세트 승리까지 단 1점,\n마지막까지 최선을!"
                        showToast = true
                    }

                    // 1초동안 비활성화
                    scope.launch {
                        kotlinx.coroutines.delay(1000)
                        isPressed = false
                    }
                },
                enabled = !isPressed && !isPaused && !isSetFinished
            ) {
                Text(
                    text = "득점",
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.pretendard_medium))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "점수 되돌리기",
                fontSize = 10.sp,
                color = Color.LightGray,
                modifier = Modifier.clickable(
                    enabled = !isPressed && !isPaused && !isSetFinished,
                    onClick = {
                        if (isPressed) return@clickable
                        isPressed=true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)  // 진동 피드백
                        viewModel.undoUserScore()
                        WatchDataLayerClient.sendUndo(
                            context = context,
                            matchId = "match-123",
                            setNumber = setNumber,
                            userScore = userScore-1,
                            opponentScore = opponentScore,
                            localIsUser1 = isUser1
                        )

                        // 1초동안 비활성화
                        scope.launch {
                            kotlinx.coroutines.delay(1000)
                            isPressed = false
                        }
                    }),
                fontFamily = FontFamily(Font(R.font.pretendard_regular))
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 경기 이벤트 토스트 (매치포인트, 세트 종료)
        AnimatedVisibility(
            visible = showToast,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                ScoreEventToast(
                    title = toastTitle,
                    message = toastMessage,
                    visible = true,
                    onDismiss = { showToast = false },
                )
            }
        }
    }
}

fun checkMatchPoint(
    playerScore: Int,
    opponentScore: Int
): Boolean {
    return checkSetWin(playerScore+1, opponentScore)
}

fun checkSetWin(
    playerScore: Int,
    opponentScore: Int
): Boolean {
    return (playerScore >= 21 && (playerScore - opponentScore) >= 2) || (playerScore == 30)
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun MatchScoreScreenPreview() {
    ScoreScreen(
        setNumber = 1,
        viewModel = viewModel(),
        onSetFinished = {}
    )
}