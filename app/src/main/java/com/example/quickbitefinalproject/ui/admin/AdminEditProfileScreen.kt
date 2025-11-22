package com.example.quickbitefinalproject.ui.admin

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminEditProfileScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneDigits by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSavedDialog by remember { mutableStateOf(false) }
    var passwordVisibleOld by remember { mutableStateOf(false) }
    var passwordVisibleNew by remember { mutableStateOf(false) }
    var passwordVisibleConfirm by remember { mutableStateOf(false) }

    var backPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Overshoot exit animation states
    val overshootOffset = 30f
    val targetOffsetX by animateFloatAsState(
        targetValue = if (backPressed) 120f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val overshootAlpha by animateFloatAsState(
        targetValue = if (backPressed) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    )
    val targetScale by animateFloatAsState(
        targetValue = if (backPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val overshootX by animateFloatAsState(
        targetValue = if (backPressed) overshootOffset else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 30.dp)
            .graphicsLayer {
                // Apply overshoot: first small left (-overshoot), then slide right
                translationX = targetOffsetX - overshootX
                alpha = overshootAlpha
                scaleX = targetScale
                scaleY = targetScale
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color(0xFFAC0000),
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(
                            onClick = {
                                backPressed = true
                                scope.launch {
                                    delay(350) // wait for smooth animation
                                    navController.popBackStack()
                                }
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Edit Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Avatar with edit icon animation
            var editPressed by remember { mutableStateOf(false) }
            val editSize by animateDpAsState(
                targetValue = if (editPressed) 38.dp else 32.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEEDA)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        colorFilter = ColorFilter.tint(Color(0xFFAC0000))
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit Avatar",
                    tint = Color(0xFFAC0000),
                    modifier = Modifier
                        .size(editSize)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .clickable(
                            onClick = {
                                editPressed = true
                                scope.launch {
                                    delay(150)
                                    editPressed = false
                                    // TODO: Open image picker
                                }
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Input fields
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneDigits,
                    onValueChange = { input ->
                        phoneDigits = input.filter { it.isDigit() }.take(11)
                    },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    placeholder = { Text("Ex. 09696610598") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                PasswordField("Old Password", oldPassword, { oldPassword = it }, passwordVisibleOld) {
                    passwordVisibleOld = !passwordVisibleOld
                }
                PasswordField("New Password", newPassword, { newPassword = it }, passwordVisibleNew) {
                    passwordVisibleNew = !passwordVisibleNew
                }
                PasswordField("Confirm New Password", confirmPassword, { confirmPassword = it }, passwordVisibleConfirm) {
                    passwordVisibleConfirm = !passwordVisibleConfirm
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { showSavedDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC0000))
            ) {
                Text(
                    text = "Save",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            containerColor = Color(0xFFFFEEDA),
            tonalElevation = 0.dp,
            title = { Text("Saved Successfully!", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text("Your profile has been updated.", fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = { showSavedDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAC0000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Icon(
                painter = painterResource(if (visible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed),
                contentDescription = null,
                modifier = Modifier.clickable { onToggleVisibility() }
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}
