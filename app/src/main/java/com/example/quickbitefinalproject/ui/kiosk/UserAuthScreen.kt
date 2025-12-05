package com.example.quickbitefinalproject.ui.kiosk

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun UserAuthScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val customColor = Color(0xFFAC0000)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // DISABLE BACK BUTTON
    BackHandler(enabled = true) {
        // Do nothing when back is pressed to lock the screen
    }

    // Google Sign-In launcher
    val googleSignInLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential =
                        GoogleAuthProvider.getCredential(account.idToken, null)

                    isLoading = true

                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val user = auth.currentUser
                                val uid = user?.uid
                                val googleEmail = user?.email ?: ""
                                val placeholderPass = "GoogleAuthUser"

                                if (uid != null) {
                                    // CHECK IF USER EXISTS IN FIRESTORE
                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    db.collection("users").document(uid).get()
                                        .addOnSuccessListener { document ->
                                            scope.launch {
                                                delay(200)
                                                if (document.exists()) {
                                                    // User exists -> Go to Main Menu
                                                    navController.navigate("main_menu") {
                                                        popUpTo("user_auth") { inclusive = true }
                                                    }
                                                } else {
                                                    // User NEW -> Go to Register Screen to fill details
                                                    navController.navigate("register_screen/$googleEmail/$placeholderPass") {
                                                        popUpTo("user_auth") { inclusive = true }
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            errorMessage = "Database check failed."
                                        }
                                } else {
                                    isLoading = false
                                    errorMessage = "Auth successful but UID missing."
                                }
                            } else {
                                isLoading = false
                                errorMessage = authTask.exception?.message
                                    ?: "Google Sign-In failed"
                            }
                        }
                }
            } catch (e: ApiException) {
                isLoading = false
                errorMessage = "Google Sign-In Error: ${e.message}"
            }
        } else {
            isLoading = false
        }
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

        // Top beige curved background
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
                .padding(top = 40.dp)
                .verticalScroll(scrollState)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.quickbite_logo),
                contentDescription = "QuickBite Logo",
                modifier = Modifier.width(180.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Deliver Favourite Food",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login Card
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
                        text = "Customer Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 15.dp)
                    )

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

                    // Email Field
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
                            cursorColor = customColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
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
                        trailingIcon = {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) R.drawable.ic_eye_open
                                    else R.drawable.ic_eye_closed
                                ),
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    passwordVisible = !passwordVisible
                                }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            cursorColor = customColor
                        ),
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()

                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "All fields are required."
                                scope.launch {
                                    delay(2000)
                                    errorMessage = null
                                }
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        scope.launch {
                                            delay(200)
                                            navController.navigate("main_menu") {
                                                popUpTo("user_auth") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = task.exception?.message ?: "Login failed"
                                        scope.launch { delay(2000); errorMessage = null }
                                    }
                                }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = customColor)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Social Login Divider ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        Text(
                            text = "OR LOGIN WITH",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    }

                    // --- Google Button Only ---
                    OutlinedButton(
                        onClick = {
                            if (!isLoading) {
                                isLoading = true
                                initiateGoogleLogin(context, googleSignInLauncher)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Sign in with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // Registration Link
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Don't have an account?",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // REGISTER
                        TextButton(
                            onClick = {
                                // Navigate to Register with empty args when clicked manually
                                navController.navigate("register_screen")
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = customColor,
                                containerColor = Color.Transparent
                            )
                        ) {
                            Text(
                                text = "REGISTER",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = customColor
                            )
                        }
                    }

                    // Admin Login Button
                    TextButton(
                        onClick = {
                            navController.navigate("admin_login")
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = customColor, // Default text color
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "Login as Admin",
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

// Helper function to trigger the Intent
private fun initiateGoogleLogin(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    launcher.launch(googleSignInClient.signInIntent)
}
