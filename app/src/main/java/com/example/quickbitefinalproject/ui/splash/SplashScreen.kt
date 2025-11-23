package com.example.quickbitefinalproject.ui.splash

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R

@Composable
fun SplashScreen(navController: NavController) {

    // Auto-Navigate after delay
    LaunchedEffect(Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate("main_menu") {
                popUpTo("splash") { inclusive = true }
            }
        }, 2000) // 2 seconds splash time
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFEEDA))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(120.dp))

            // App Logo (Center)
            Image(
                painter = painterResource(id = R.drawable.quickbite_logo),
                contentDescription = "QuickBite Logo",
                modifier = Modifier.width(260.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Food Illustration
            Image(
                painter = painterResource(id = R.drawable.bottom_food),
                contentDescription = "Food Illustration",
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.Start)
            )
        }
    }
}
