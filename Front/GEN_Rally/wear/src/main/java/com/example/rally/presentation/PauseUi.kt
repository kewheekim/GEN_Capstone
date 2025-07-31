package com.example.rally.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.example.rally.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

@Composable
fun PauseScreen(onPause: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black_bg)),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onPause,
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.green_active)),
            modifier = Modifier.width(120.dp).height(48.dp),
            shape = RoundedCornerShape(29.dp)
        ) {
            Text(
                text = "일시정지",
                fontFamily = FontFamily(Font(R.font.pretendard_variable)),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(53.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_dot2),
            contentDescription = "page1",
            modifier = Modifier
                .width(18.dp)
                .offset(y = 80.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 192, heightDp = 192)
@Composable
fun PauseScreenPreview() {
    PauseScreen(onPause = {})
}