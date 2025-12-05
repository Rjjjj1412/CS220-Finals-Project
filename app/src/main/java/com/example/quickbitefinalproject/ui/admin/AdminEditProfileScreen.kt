package com.example.quickbitefinalproject.ui.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quickbitefinalproject.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun AdminEditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid ?: return

    // State variables
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneDigits by remember { mutableStateOf("") }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Image handling (Base64)
    var profileImageBase64 by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var resizedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    var passwordVisibleOld by remember { mutableStateOf(false) }
    var passwordVisibleNew by remember { mutableStateOf(false) }
    var passwordVisibleConfirm by remember { mutableStateOf(false) }

    var backPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val customColor = Color(0xFFAC0000)

    // Animations
    val overshootOffset = 30f
    val targetOffsetX by animateFloatAsState(if (backPressed) 120f else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "")
    val overshootAlpha by animateFloatAsState(if (backPressed) 0f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), label = "")
    val targetScale by animateFloatAsState(if (backPressed) 0.95f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "")
    val overshootX by animateFloatAsState(if (backPressed) overshootOffset else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "")

    // Load profile from Firestore
    LaunchedEffect(uid) {
        try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                firstName = doc.getString("firstName") ?: ""
                lastName = doc.getString("lastName") ?: ""
                email = doc.getString("email") ?: ""
                phoneDigits = doc.getString("phone") ?: ""
                profileImageBase64 = doc.getString("profileImage") ?: ""
            }
        } catch (e: Exception) {
            // Handle error silently or show toast
        }
    }

    // Image picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Renamed function to avoid conflict
            resizedImageBytes = resizeAdminProfileImage(context, it, 300, 300)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 30.dp)
            .graphicsLayer {
                translationX = targetOffsetX - overshootX
                alpha = overshootAlpha
                scaleX = targetScale
                scaleY = targetScale
            }
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Back button
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = "Back", tint = customColor,
                    modifier = Modifier.size(30.dp).clickable {
                        backPressed = true
                        scope.launch { delay(300); navController.popBackStack() }
                    })
                Spacer(Modifier.width(16.dp))
                Text("Edit Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            // Avatar with edit icon
            var editPressed by remember { mutableStateOf(false) }
            val editSize by animateDpAsState(if (editPressed) 38.dp else 32.dp, spring(), label = "")

            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFFFEEDA)), contentAlignment = Alignment.Center) {
                    when {
                        // 1. New local image
                        resizedImageBytes != null -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(resizedImageBytes).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // 2. Existing Base64 image
                        profileImageBase64.isNotEmpty() -> {
                            val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(imageBytes).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // 3. Fallback
                        else -> {
                            Image(
                                painterResource(R.drawable.ic_profile),
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                colorFilter = ColorFilter.tint(customColor)
                            )
                        }
                    }
                }
                Icon(painterResource(R.drawable.ic_edit), contentDescription = null, tint = customColor,
                    modifier = Modifier.size(editSize).clip(CircleShape).background(Color.White).padding(4.dp).clickable {
                        editPressed = true
                        scope.launch { delay(150); editPressed = false; launcher.launch("image/*") }
                    })
            }

            Spacer(Modifier.height(30.dp))

            // Profile fields
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    modifier = Modifier.fillMaxWidth())

                OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    modifier = Modifier.fillMaxWidth())

                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor, disabledBorderColor = Color.Gray, disabledTextColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth())

                OutlinedTextField(phoneDigits, { phoneDigits = it.filter { c -> c.isDigit() }.take(11) }, label = { Text("Phone Number") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    placeholder = { Text("Ex. 09123456789") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                // Password Fields
                Spacer(Modifier.height(10.dp))
                Text("Change Password (Optional)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                PasswordField("Old Password", oldPassword, { oldPassword = it }, passwordVisibleOld) { passwordVisibleOld = !passwordVisibleOld }
                PasswordField("New Password", newPassword, { newPassword = it }, passwordVisibleNew) { passwordVisibleNew = !passwordVisibleNew }
                PasswordField("Confirm New Password", confirmPassword, { confirmPassword = it }, passwordVisibleConfirm) { passwordVisibleConfirm = !passwordVisibleConfirm }
            }

            Spacer(Modifier.height(30.dp))

            Button(onClick = {
                scope.launch {
                    focusManager.clearFocus()
                    isLoading = true

                    // Basic Validation
                    if (firstName.isBlank() || lastName.isBlank() || phoneDigits.isBlank()) {
                        dialogTitle = "Missing Info"; dialogMessage = "Please fill in all required fields."; showDialog = true; isLoading = false; return@launch
                    }

                    // Password Validation
                    val passwordChangeRequested = oldPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()
                    if (passwordChangeRequested) {
                        if (oldPassword.isEmpty()) { dialogTitle = "Error"; dialogMessage = "Enter old password to verify changes."; showDialog = true; isLoading = false; return@launch }
                        if (newPassword != confirmPassword) { dialogTitle = "Error"; dialogMessage = "New passwords do not match."; showDialog = true; isLoading = false; return@launch }
                        if (newPassword.length < 6) { dialogTitle = "Error"; dialogMessage = "Password must be at least 6 characters."; showDialog = true; isLoading = false; return@launch }
                    }

                    try {
                        // 1. Re-auth and update password if needed
                        if (passwordChangeRequested) {
                            val credential = EmailAuthProvider.getCredential(auth.currentUser!!.email!!, oldPassword)
                            auth.currentUser!!.reauthenticate(credential).await()
                            auth.currentUser!!.updatePassword(newPassword).await()
                        }

                        // 2. Prepare Updates
                        val updates = hashMapOf<String, Any>(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "fullName" to "$firstName $lastName",
                            "phone" to phoneDigits
                        )

                        // 3. Handle Image (Convert to Base64)
                        if (resizedImageBytes != null) {
                            val base64String = Base64.encodeToString(resizedImageBytes, Base64.DEFAULT)
                            updates["profileImage"] = base64String
                            profileImageBase64 = base64String
                        }

                        // 4. Update Firestore
                        db.collection("users").document(uid).update(updates).await()

                        dialogTitle = "Success"
                        dialogMessage = "Your profile has been updated successfully."
                        showDialog = true
                        oldPassword = ""; newPassword = ""; confirmPassword = ""
                        resizedImageBytes = null

                    } catch (e: Exception) {
                        dialogTitle = "Error"
                        dialogMessage = e.message ?: "Something went wrong"
                        showDialog = true
                    }
                    isLoading = false
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(containerColor = customColor), enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(50.dp))
        }
    }

    // --- STANDARDIZED DIALOG (UPDATED) ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black // Explicit Black
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    fontSize = 16.sp,
                    color = Color.Black // Explicit Black
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = customColor)
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val customColor = Color(0xFFAC0000)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Icon(
                painter = painterResource(if (visible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed),
                contentDescription = null,
                modifier = Modifier.clickable { onToggleVisibility() }
            )
        }
    )
}

// Renamed to avoid conflict with other screens
fun resizeAdminProfileImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): ByteArray? = try {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    if (bitmap != null) {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.toByteArray()
    } else {
        null
    }
} catch (e: Exception) {
    null
}
