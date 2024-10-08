package com.mike.uniadmin

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 0.8f else 0.6f,
        animationSpec = tween(
            durationMillis = 800,
            easing = { OvershootInterpolator(2f).getInterpolation(it) }),
        label = ""
    )

    val email = UniConnectPreferences.userEmail.value
    val courseCode = CourseManager.courseCode
    val userId = UniConnectPreferences.userID.value

    val destination = when {
        email.isEmpty() && userId.isEmpty() -> "login"
        courseCode.equals("") -> "courses"
        else -> "homeScreen"
    }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000)
        navController.navigate(destination)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CC.primary(),
                        CC.secondary()
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                tint = CC.tertiary(),
                modifier = Modifier.size(150.dp) // Slightly bigger logo for better focus
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Improved Text Style with Gradients and Shadows
            Text(
                text = "Uni Connect",
                style = CC.titleTextStyle().copy(
                    fontSize = 32.sp, // Larger font for emphasis
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow( // Add shadow for a slight 3D effect
                        color = Color.Black,
                        blurRadius = 8f,
                        offset = Offset(4f, 4f)
                    )
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Ultimate Educational Companion",
                style = CC.descriptionTextStyle().copy(
                    fontSize = 18.sp, // Smaller font for description
                    fontWeight = FontWeight.Normal,
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Footer text styled with bold font
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Developed by Mike",
                style = CC.descriptionTextStyle().copy(
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    shadow = Shadow(Color.Gray, Offset(2f, 2f), blurRadius = 4f)
                )
            )
        }
    }
}

