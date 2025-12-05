package com.example.quickbitefinalproject.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R
import com.example.quickbitefinalproject.service.FirebaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun LoginAdminScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val customColor = Color(0xFFAC0000)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState() // Added scroll state for smaller screens

    // DISABLE BACK BUTTON
    BackHandler(enabled = true) {
        // Do nothing when back is pressed to lock the screen
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        // Beige top curved
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
            drawPath(path, color = Color(0xFFFFEEDA), style = Fill)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp) // Adjusted padding
                .verticalScroll(scrollState) // Added vertical scroll
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Card(
                modifier = Modifier
                    .padding(horizontal = 25.dp)
                    .padding(bottom = 30.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(10.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Admin Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // ERROR MESSAGE ABOVE EMAIL
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )
                    }

                    // EMAIL
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            cursorColor = customColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password") },
                        leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_lock), contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed),
                                contentDescription = null,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            cursorColor = customColor
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() })
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // LOGIN BUTTON
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()

                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "All fields are required."
                                scope.launch { delay(2000); errorMessage = null }
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            FirebaseService.auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener { result ->
                                    val uid = result.user!!.uid
                                    FirebaseService.db.collection("users").document(uid).get()
                                        .addOnSuccessListener { doc ->
                                            val role = doc.getString("role")
                                            if (role == "admin") {
                                                // Navigate with fade + slide animation
                                                scope.launch {
                                                    delay(200)
                                                    navController.navigate("admin_panel") {
                                                        popUpTo("admin_login") { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            } else {
                                                errorMessage = "This account is not an admin."
                                                scope.launch { delay(2000); errorMessage = null }
                                            }
                                            isLoading = false
                                        }
                                        .addOnFailureListener {
                                            errorMessage = "Failed to verify user role."
                                            isLoading = false
                                            scope.launch { delay(2000); errorMessage = null }
                                        }
                                }
                                .addOnFailureListener {
                                    errorMessage = "Invalid email or password."
                                    isLoading = false
                                    scope.launch { delay(2000); errorMessage = null }
                                }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC0000))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(text = "Login", fontSize = 16.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Note: For system administrators only",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // LOGIN AS CUSTOMER BUTTON
                    TextButton(
                        onClick = {
                            navController.navigate("user_auth")
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = customColor,
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "Login as Customer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = customColor
                        )
                    }
                }
            }
        }
    }
}
