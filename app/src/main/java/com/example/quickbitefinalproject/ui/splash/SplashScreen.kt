package com.example.quickbitefinalproject.ui.splash

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current

    // Auto-Navigate after delay
    LaunchedEffect(Unit) {
        Handler(Looper.getMainLooper()).postDelayed({

            // TODO: If you want auto-login for admin, check Firebase Admin session here
            // Example (later):
            // if (FirebaseAuth.getInstance().currentUser != null) {
            //      navController.navigate("admin_dashboard")
            // } else {
            //      navController.navigate("admin_login")
            // }

            navController.navigate("admin_login") {
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
                painter = painterResource(id = R.drawable.quickbite_logo), // replace with provided logo
                contentDescription = "QuickBite Logo",
                modifier = Modifier
                    .width(260.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Food Illustration
            Image(
                painter = painterResource(id = R.drawable.bottom_food), // your bottom food graphic
                contentDescription = "Food Illustration",
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.Start)
            )
        }
    }
}
