package com.example.quickbitefinalproject.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R

@Composable
fun LoginAdminScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val customColor = Color(0xFFAC0000)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // default bottom background
    ) {

        // ==========================
        // Beige top with curved bottom
        // ==========================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Target: Beige occupies top ~486px of screen, proportionally scaled
            val beigeHeight = height * 0.50f   // roughly half of a taller screen (adjustable)

            val path = Path().apply {
                moveTo(0f, 0f)                 // top-left
                lineTo(width, 0f)              // top-right
                lineTo(width, beigeHeight)     // bottom-right before curve

                // Smooth arc curve at the bottom of beige
                cubicTo(
                    width * 0.75f, beigeHeight + 150f,  // right control point (bulge)
                    width * 0.25f, beigeHeight + 150f,  // left control point
                    0f, beigeHeight                     // end point bottom-left
                )

                close()
            }

            drawPath(
                path = path,
                color = Color(0xFFFFEEDA),
                style = Fill
            )
        }
        // ==========================
        // Main Content
        // ==========================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Image(
                painter = painterResource(id = R.drawable.quickbite_logo),
                contentDescription = "QuickBite Logo",
                modifier = Modifier.width(200.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Deliver Favourite Food",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(35.dp))

            // Login Card
            Card(
                modifier = Modifier
                    .padding(horizontal = 25.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(10.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Admin Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // EMAIL TEXTFIELD
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = null
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            focusedLabelColor = customColor,
                            cursorColor = customColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // PASSWORD TEXTFIELD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = null
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            focusedLabelColor = customColor,
                            cursorColor = customColor
                        ),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible)
                                        R.drawable.ic_eye_open
                                    else
                                        R.drawable.ic_eye_closed
                                ),
                                contentDescription = null,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // FORGOT PASSWORD
                    Text(
                        text = "Forget Password?",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // LOGIN BUTTON
                    Button(
                        onClick = { // TODO: Add actual authentication check here
                            // For now, navigate directly to Admin Panel
                            navController.navigate("admin_panel") {
                                popUpTo("admin_login") { inclusive = true } // remove login screen from backstack
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAC0000)
                        )
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Note: For system administrators only",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
