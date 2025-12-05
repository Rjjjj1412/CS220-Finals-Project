package com.example.quickbitefinalproject.ui.admin

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R

@Composable
fun AdminPanelScreen(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val customColor = Color(0xFFAC0000)

    Box(modifier = Modifier.fillMaxSize()) {

        // Curved beige background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val beigeHeight = height * 0.50f

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, beigeHeight)
                cubicTo(
                    width * 0.75f, beigeHeight + 150f,
                    width * 0.25f, beigeHeight + 150f,
                    0f, beigeHeight
                )
                close()
            }

            drawPath(
                path = path,
                color = Color(0xFFFFEEDA),
                style = Fill
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp)
        ) {

            Text(
                text = "Hi Admin!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome back to your panel",
                fontSize = 20.sp,
                color = Color(0xFF646464)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AdminCard(
                        title = "Menu Management",
                        iconRes = R.drawable.ic_menu,
                        onClick = { navController.navigate("admin_menu_management") }
                    )
                    AdminCard(
                        title = "Profile",
                        iconRes = R.drawable.ic_profile,
                        onClick = { navController.navigate("admin_edit_profile") }
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    AdminCard(
                        title = "Log Out",
                        iconRes = R.drawable.ic_logout,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    // --- STANDARDIZED LOGOUT DIALOG ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White, // White Container
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black // Black Text
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to Log Out?",
                    fontSize = 16.sp,
                    color = Color.Black // Black Text
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        navController.navigate("admin_login") {
                            popUpTo("admin_panel") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColor // Red background
                    )
                ) {
                    Text(
                        text = "Yes",
                        color = Color.White, // White text
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = "No",
                        color = Color.Gray, // Gray text for cancel
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun AdminCard(
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val animatedSize by animateDpAsState(
        targetValue = if (pressed) 150.dp else 140.dp,
        animationSpec = tween(durationMillis = 150)
    )

    Card(
        modifier = Modifier
            .size(animatedSize)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(Color(0xFFAC0000))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
