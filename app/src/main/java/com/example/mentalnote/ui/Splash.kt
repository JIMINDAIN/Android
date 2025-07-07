package com.example.mentalnote.ui

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mentalnote.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navToMain: () -> Unit){
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(Unit){
        scale.animateTo(
            targetValue = 1.5f,
            animationSpec = tween(durationMillis = 500, easing = FastOutLinearInEasing)
        )
        delay(500)
        navToMain()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ){
        Box(contentAlignment = Alignment.Center){
            Image(
                painter = painterResource(id = R.drawable.logo_calendar),
                contentDescription = "Emotion logo",
                modifier = Modifier.size(150.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.logo_pencil),
                contentDescription = "Pencil",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value
                    )
                    .align(Alignment.BottomEnd)
                    .offset(x = 17.dp, y = -3.dp)
            )
        }
    }
}