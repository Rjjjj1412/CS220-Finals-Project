package com.example.quickbitefinalproject.ui.kiosk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quickbitefinalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    googleEmail: String = "",
    googlePassword: String = ""
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Branding Color
    val customColor = Color(0xFFAC0000)

    // Check if this is a Google Sign-In flow
    val isGoogleUser = googleEmail.isNotEmpty()

    // Form States
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Pre-fill and lock if Google User
    var email by remember { mutableStateOf(googleEmail) }
    var password by remember { mutableStateOf(googlePassword) }
    var confirmPassword by remember { mutableStateOf(googlePassword) }

    // Image Picker States
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var resizedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var editPressed by remember { mutableStateOf(false) }
    val editSize by animateDpAsState(if (editPressed) 38.dp else 32.dp, spring())

    // Dropdown States
    var paymentExpanded by remember { mutableStateOf(false) }
    val paymentOptions = listOf("Cash on Delivery", "G-Cash", "Credit/Debit Card")
    var selectedPaymentMethod by remember { mutableStateOf(paymentOptions[0]) }

    // Password Visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Loading & Dialog States
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    // Animation variables for Back Press
    var backPressed by remember { mutableStateOf(false) }
    val overshootOffset = 30f
    val targetOffsetX by animateFloatAsState(
        if (backPressed) 120f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "offsetX"
    )
    val overshootAlpha by animateFloatAsState(
        if (backPressed) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "alpha"
    )
    val overshootX by animateFloatAsState(
        if (backPressed) overshootOffset else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "overshootX"
    )

    // Image Launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            resizedImageBytes = resizeImage(context, it, 300, 300)
        }
    }

    // Success/Error Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSuccess) showDialog = false },
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (isSuccess) {
                            if (isGoogleUser) {
                                // Go straight to main menu for Google users
                                navController.navigate("main_menu") {
                                    popUpTo("register_screen") { inclusive = true }
                                    popUpTo("user_auth") { inclusive = true }
                                }
                            } else {
                                // Standard flow goes to login
                                navController.navigate("user_auth") {
                                    popUpTo("register_screen") { inclusive = true }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = customColor)
                ) {
                    Text(if (isSuccess) (if(isGoogleUser) "Start Ordering" else "Login Now") else "OK", color = Color.White)
                }
            },
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer {
                translationX = targetOffsetX - overshootX
                alpha = overshootAlpha
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------------------------------------------------------
            // BACK BUTTON & TITLE HEADER
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back), // Ensure ic_back exists
                    contentDescription = "Back",
                    tint = customColor,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            backPressed = true
                            scope.launch {
                                delay(300)
                                navController.popBackStack()
                            }
                        }
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = if (isGoogleUser) "Complete Profile" else "Create Account",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = customColor
                )
            }

            Text(
                text = if (isGoogleUser) "One last step to get started!" else "Sign up to get started!",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---------------------------------------------------------
            // AVATAR PICKER
            // ---------------------------------------------------------
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEEDA)),
                    contentAlignment = Alignment.Center
                ) {
                    if (resizedImageBytes != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(resizedImageBytes).build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_profile), // Ensure exists
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            colorFilter = ColorFilter.tint(customColor)
                        )
                    }
                }

                Icon(
                    painter = painterResource(R.drawable.ic_edit), // Ensure exists
                    contentDescription = "Edit Photo",
                    tint = customColor,
                    modifier = Modifier
                        .size(editSize)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(6.dp)
                        .clickable {
                            editPressed = true
                            scope.launch {
                                delay(150)
                                editPressed = false
                                launcher.launch("image/*")
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ---------------------------------------------------------
            // FORM FIELDS
            // ---------------------------------------------------------
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Name Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            focusedLabelColor = customColor,
                            cursorColor = customColor
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            focusedLabelColor = customColor,
                            cursorColor = customColor
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { newPhone ->
                        if (newPhone.all { it.isDigit() } && newPhone.length <= 11) {
                            phone = newPhone
                        }
                    },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColor,
                        focusedLabelColor = customColor,
                        cursorColor = customColor
                    )
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColor,
                        focusedLabelColor = customColor,
                        cursorColor = customColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Preferred Payment") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (paymentExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            focusedLabelColor = customColor,
                            cursorColor = customColor
                        ),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true) // <--- Fixed
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false }
                    ) {
                        paymentOptions.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedPaymentMethod = method
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Email (Locked if Google)
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (!isGoogleUser) email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = isGoogleUser,
                    enabled = !isGoogleUser,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColor,
                        focusedLabelColor = customColor,
                        cursorColor = customColor,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray,
                        disabledTextColor = Color.DarkGray
                    )
                )

                // Password (Locked and Hashed/Hidden if Google)
                OutlinedTextField(
                    value = password,
                    onValueChange = { if (!isGoogleUser) password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = isGoogleUser,
                    enabled = !isGoogleUser,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        if (!isGoogleUser) {
                            Icon(
                                painter = painterResource(if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed),
                                contentDescription = null,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColor,
                        focusedLabelColor = customColor,
                        cursorColor = customColor,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray,
                        disabledTextColor = Color.DarkGray
                    )
                )

                // Confirm Password - HIDE if Google User
                AnimatedVisibility(visible = !isGoogleUser) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(if (confirmPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed),
                                contentDescription = null,
                                modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            focusedLabelColor = customColor,
                            cursorColor = customColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ---------------------------------------------------------
            // REGISTER BUTTON
            // ---------------------------------------------------------
            Button(
                onClick = {
                    focusManager.clearFocus()

                    // 1. Validation
                    if (firstName.isBlank() || lastName.isBlank() || phone.isBlank() || address.isBlank() || email.isBlank() || password.isBlank()) {
                        dialogTitle = "Missing Info"
                        dialogMessage = "Please fill in all required fields."
                        isSuccess = false
                        showDialog = true
                        return@Button
                    }

                    if (!isGoogleUser && password != confirmPassword) {
                        dialogTitle = "Error"
                        dialogMessage = "Passwords do not match."
                        isSuccess = false
                        showDialog = true
                        return@Button
                    }

                    if (!isGoogleUser && password.length < 6) {
                        dialogTitle = "Weak Password"
                        dialogMessage = "Password must be at least 6 characters."
                        isSuccess = false
                        showDialog = true
                        return@Button
                    }

                    isLoading = true

                    // Helper function to save Firestore data
                    fun saveUserToFirestore(uid: String) {
                        val userMap = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "fullName" to "$firstName $lastName",
                            "phone" to phone,
                            "address" to address,
                            "email" to email,
                            "paymentMethod" to selectedPaymentMethod,
                            "role" to "customer",
                            "profileImage" to (if (resizedImageBytes != null) Base64.encodeToString(resizedImageBytes, Base64.DEFAULT) else "")
                        )

                        db.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                isLoading = false
                                isSuccess = true
                                dialogTitle = "Success"
                                dialogMessage = if(isGoogleUser) "Profile completed successfully!" else "Account created successfully! Please login."
                                showDialog = true
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                isSuccess = false
                                dialogTitle = "Database Error"
                                dialogMessage = e.message ?: "Failed to save profile."
                                showDialog = true
                            }
                    }

                    // 2. Execution Branch
                    if (isGoogleUser) {
                        // --- GOOGLE FLOW ---
                        // User is already signed in via UserAuthScreen.
                        // We just need to create the Firestore document.
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            saveUserToFirestore(currentUser.uid)
                        } else {
                            isLoading = false
                            dialogTitle = "Auth Error"
                            dialogMessage = "No Google user found. Please try signing in again."
                            showDialog = true
                        }
                    } else {
                        // --- STANDARD FLOW ---
                        // Create new Auth user
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = task.result?.user?.uid
                                    if (uid != null) {
                                        saveUserToFirestore(uid)
                                    } else {
                                        isLoading = false
                                        dialogTitle = "Error"
                                        dialogMessage = "User created but ID missing."
                                        showDialog = true
                                    }
                                } else {
                                    isLoading = false
                                    dialogTitle = "Registration Failed"
                                    dialogMessage = task.exception?.message ?: "Unknown error."
                                    showDialog = true
                                }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = customColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if(isGoogleUser) "COMPLETE REGISTRATION" else "REGISTER", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// --- HELPER FUNCTION: RESIZE IMAGE ---
fun resizeImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): ByteArray? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) return null

        val width = originalBitmap.width
        val height = originalBitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)
        val outputStream = ByteArrayOutputStream()

        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
