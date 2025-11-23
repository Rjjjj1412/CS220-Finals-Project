package com.example.quickbitefinalproject.ui.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quickbitefinalproject.R
import com.example.quickbitefinalproject.service.FirebaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun AdminEditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uid = FirebaseService.auth.currentUser?.uid ?: return

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneDigits by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
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

    val overshootOffset = 30f
    val targetOffsetX by animateFloatAsState(if (backPressed) 120f else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    val overshootAlpha by animateFloatAsState(if (backPressed) 0f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
    val targetScale by animateFloatAsState(if (backPressed) 0.95f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    val overshootX by animateFloatAsState(if (backPressed) overshootOffset else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))

    // Load profile from Firestore
    LaunchedEffect(uid) {
        val doc = FirebaseService.db.collection("users").document(uid).get().await()
        fullName = "${doc.getString("first_name") ?: ""} ${doc.getString("last_name") ?: ""}"
        email = doc.getString("email") ?: ""
        phoneDigits = doc.getString("phone") ?: ""
        avatarUrl = doc.getString("avatarUrl")
    }

    // Image picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            resizedImageBytes = resizeImage(context, it, 300, 300)
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
            val editSize by animateDpAsState(if (editPressed) 38.dp else 32.dp, spring())
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFFFEEDA)), contentAlignment = Alignment.Center) {
                    when {
                        resizedImageBytes != null -> AsyncImage(model = ImageRequest.Builder(context).data(resizedImageBytes).build(), contentDescription = null)
                        !avatarUrl.isNullOrEmpty() -> {
                            val bitmap = BitmapFactory.decodeFile(avatarUrl)
                            if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
                            else Image(painterResource(R.drawable.ic_profile), contentDescription = null, modifier = Modifier.size(100.dp), colorFilter = ColorFilter.tint(customColor))
                        }
                        else -> Image(painterResource(R.drawable.ic_profile), contentDescription = null, modifier = Modifier.size(100.dp), colorFilter = ColorFilter.tint(customColor))
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
                OutlinedTextField(fullName, { fullName = it }, label = { Text("Full Name") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phoneDigits, { phoneDigits = it.filter { c -> c.isDigit() }.take(11) }, label = { Text("Phone Number") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    placeholder = { Text("Ex. 09696610598") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                PasswordField("Old Password", oldPassword, { oldPassword = it }, passwordVisibleOld) { passwordVisibleOld = !passwordVisibleOld }
                PasswordField("New Password", newPassword, { newPassword = it }, passwordVisibleNew) { passwordVisibleNew = !passwordVisibleNew }
                PasswordField("Confirm New Password", confirmPassword, { confirmPassword = it }, passwordVisibleConfirm) { passwordVisibleConfirm = !passwordVisibleConfirm }
            }

            Spacer(Modifier.height(30.dp))

            Button(onClick = {
                scope.launch {
                    focusManager.clearFocus()
                    isLoading = true
                    val error = validateInputs(oldPassword, newPassword, confirmPassword, email)
                    if (error != null) { dialogTitle = "Error"; dialogMessage = error; showDialog = true; isLoading = false; return@launch }
                    try {
                        saveProfileCoroutine(context, uid, fullName, email, phoneDigits, oldPassword, newPassword, confirmPassword, resizedImageBytes)
                        dialogTitle = "Success"
                        dialogMessage = "Your profile has been updated successfully."
                        showDialog = true
                        oldPassword = ""; newPassword = ""; confirmPassword = ""
                    } catch (e: Exception) {
                        dialogTitle = "Error"
                        dialogMessage = e.message ?: "Something went wrong"
                        showDialog = true
                    }
                    isLoading = false
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(containerColor = customColor), enabled = !isLoading) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false }, containerColor = Color(0xFFFFEEDA),
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text(dialogMessage, fontSize = 16.sp) },
            confirmButton = { TextButton(onClick = { showDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = customColor)) { Text("OK", fontWeight = FontWeight.Bold) } })
    }
}

fun resizeImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): ByteArray? = try {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!, maxWidth, maxHeight, true)
    ByteArrayOutputStream().apply { scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, this) }.toByteArray()
} catch (e: Exception) { e.printStackTrace(); null }

fun saveImageLocally(context: Context, imageBytes: ByteArray, fileName: String): String? {
    return try {
        val file = File(context.filesDir, fileName)
        file.outputStream().use { it.write(imageBytes) }
        file.absolutePath
    } catch (e: Exception) { e.printStackTrace(); null }
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return email.matches(emailRegex)
}

fun validateInputs(oldPassword: String, newPassword: String, confirmPassword: String, email: String): String? {
    if (!isValidEmail(email)) return "Please enter a valid email address."
    return when {
        oldPassword.isNotBlank() && (newPassword.isBlank() || confirmPassword.isBlank()) -> "Please enter new password and confirm password."
        oldPassword.isBlank() && (newPassword.isNotBlank() || confirmPassword.isNotBlank()) -> "Old password must be entered to change password."
        newPassword != confirmPassword -> "New password and confirm password do not match."
        else -> null
    }
}

suspend fun saveProfileCoroutine(context: Context, uid: String, fullName: String, email: String, phone: String, oldPassword: String, newPassword: String, confirmPassword: String, avatarBytes: ByteArray?) {
    val db = FirebaseService.db
    val user = FirebaseService.auth.currentUser ?: throw Exception("User not found")
    val updates = mutableMapOf<String, Any>()
    fullName.takeIf { it.isNotBlank() }?.let {
        val names = it.split(" ")
        updates["first_name"] = names.firstOrNull() ?: ""
        updates["last_name"] = names.drop(1).joinToString(" ")
    }
    email.takeIf { it.isNotBlank() }?.let { updates["email"] = it }
    phone.takeIf { it.isNotBlank() }?.let { updates["phone"] = it }

    // Save avatar locally
    if (avatarBytes != null) {
        val localPath = saveImageLocally(context, avatarBytes, "avatar_$uid.jpg")
        if (localPath != null) updates["avatarUrl"] = localPath
    }

    // Update Firestore
    db.collection("users").document(uid).update(updates).await()

    // Password change
    if (oldPassword.isNotBlank()) {
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, oldPassword)
        user.reauthenticate(credential).await()
        if (newPassword.isNotBlank()) user.updatePassword(newPassword).await()
    }
}

@Composable
fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit, visible: Boolean, onToggleVisibility: () -> Unit) {
    val customColor = Color(0xFFAC0000)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = { Icon(painterResource(if (visible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed), contentDescription = null, modifier = Modifier.clickable { onToggleVisibility() }) },
        modifier = Modifier.fillMaxWidth()
    )
}
