package com.example.quickbitefinalproject.ui.kiosk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val uid = user?.uid

    // If no user logged in, return to auth
    if (uid == null) {
        LaunchedEffect(Unit) {
            navController.navigate("user_auth") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    // --- State Variables ---
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Payment Dropdown
    var paymentExpanded by remember { mutableStateOf(false) }
    val paymentOptions = listOf("Cash on Delivery", "G-Cash", "Credit/Debit Card")
    var selectedPaymentMethod by remember { mutableStateOf(paymentOptions[0]) }

    // Password fields
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Image (Base64 String from Firestore)
    var profileImageBase64 by remember { mutableStateOf("") }
    var resizedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // UI States
    var passwordVisibleOld by remember { mutableStateOf(false) }
    var passwordVisibleNew by remember { mutableStateOf(false) }
    var passwordVisibleConfirm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) } // Start loading immediately

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val customColor = Color(0xFFAC0000)

    // Animations
    var backPressed by remember { mutableStateOf(false) }
    val overshootOffset = 30f
    val targetOffsetX by animateFloatAsState(
        if (backPressed) 120f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val overshootAlpha by animateFloatAsState(
        if (backPressed) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val overshootX by animateFloatAsState(
        if (backPressed) overshootOffset else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )

    // --- Load Data ---
    LaunchedEffect(uid) {
        try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                // Using camelCase keys to match RegisterScreen
                firstName = doc.getString("firstName") ?: ""
                lastName = doc.getString("lastName") ?: ""
                email = doc.getString("email") ?: user.email ?: ""
                phone = doc.getString("phone") ?: ""
                address = doc.getString("address") ?: ""
                selectedPaymentMethod = doc.getString("paymentMethod") ?: paymentOptions[0]
                profileImageBase64 = doc.getString("profileImage") ?: ""
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Image Picker ---
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // RENAMED FUNCTION CALL HERE
            resizedImageBytes = resizeProfileImage(context, it, 300, 300)
        }
    }

    // --- Dialog ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("OK", color = customColor) }
            },
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // Animation Layer
            .graphicsLayer {
                translationX = targetOffsetX - overshootX
                alpha = overshootAlpha
            }
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {

        // SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 30.dp), // Top padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Title ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = customColor)
            }

            // --- Avatar ---
            var editPressed by remember { mutableStateOf(false) }
            val editSize by animateDpAsState(if (editPressed) 38.dp else 32.dp, spring(), label = "")

            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEEDA)), contentAlignment = Alignment.Center) {

                    when {
                        // 1. User selected a new image from gallery
                        resizedImageBytes != null -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(resizedImageBytes).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // 2. Existing image from Firestore (Base64)
                        profileImageBase64.isNotEmpty() -> {
                            val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(imageBytes).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // 3. Default
                        else -> {
                            Image(
                                painterResource(R.drawable.ic_profile),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                colorFilter = ColorFilter.tint(customColor)
                            )
                        }
                    }
                }

                Icon(painterResource(R.drawable.ic_edit), contentDescription = null, tint = customColor,
                    modifier = Modifier
                        .size(editSize)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .clickable {
                            editPressed = true
                            scope.launch {
                                delay(150); editPressed = false; launcher.launch("image/*")
                            }
                        })
            }

            Spacer(Modifier.height(30.dp))

            // --- Profile Fields ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {

                // Names
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = firstName, onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor)
                    )
                    OutlinedTextField(
                        value = lastName, onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor)
                    )
                }

                // Email (Read Only mostly)
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColor,
                        focusedLabelColor = customColor,
                        cursorColor = customColor,
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if(it.length <= 11) phone = it.filter { c -> c.isDigit() } },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shipping Address") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor),
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment Dropdown
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = {
                            Icon(
                                if (paymentExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColor,
                            focusedLabelColor = customColor,
                            cursorColor = customColor
                        ),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        paymentOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedPaymentMethod = option
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                // Password Section
                Spacer(Modifier.height(10.dp))
                Text("Change Password (Optional)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                PasswordField("Old Password", oldPassword, { oldPassword = it }, passwordVisibleOld, { passwordVisibleOld = !passwordVisibleOld }, customColor)
                PasswordField("New Password", newPassword, { newPassword = it }, passwordVisibleNew, { passwordVisibleNew = !passwordVisibleNew }, customColor)
                PasswordField("Confirm New Password", confirmPassword, { confirmPassword = it }, passwordVisibleConfirm, { passwordVisibleConfirm = !passwordVisibleConfirm }, customColor)
            }

            Spacer(Modifier.height(30.dp))

            // --- Save Button ---
            Button(
                onClick = {
                    scope.launch {
                        focusManager.clearFocus()
                        isLoading = true

                        // Validation
                        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                            dialogTitle = "Missing Info"
                            dialogMessage = "Please fill in all profile fields."
                            showDialog = true
                            isLoading = false
                            return@launch
                        }

                        // Password Change Logic
                        val passwordChangeRequested = oldPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()

                        if (passwordChangeRequested) {
                            if (oldPassword.isEmpty()) {
                                dialogTitle = "Error"
                                dialogMessage = "Enter old password to verify changes."
                                showDialog = true
                                isLoading = false
                                return@launch
                            }
                            if (newPassword != confirmPassword) {
                                dialogTitle = "Error"
                                dialogMessage = "New passwords do not match."
                                showDialog = true
                                isLoading = false
                                return@launch
                            }
                            if (newPassword.length < 6) {
                                dialogTitle = "Error"
                                dialogMessage = "Password must be at least 6 characters."
                                showDialog = true
                                isLoading = false
                                return@launch
                            }
                        }

                        try {
                            // 1. Re-authenticate if changing password
                            if (passwordChangeRequested) {
                                val credential = EmailAuthProvider.getCredential(user!!.email!!, oldPassword)
                                user.reauthenticate(credential).await()
                                user.updatePassword(newPassword).await()
                            }

                            // 2. Prepare Update Map (CamelCase keys)
                            val updates = hashMapOf<String, Any>(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "phone" to phone,
                                "address" to address,
                                "paymentMethod" to selectedPaymentMethod
                            )

                            // 3. Handle Image (Base64)
                            if (resizedImageBytes != null) {
                                val base64String = Base64.encodeToString(resizedImageBytes, Base64.DEFAULT)
                                updates["profileImage"] = base64String
                                // Update local state
                                profileImageBase64 = base64String
                            }

                            // 4. Update Firestore
                            db.collection("users").document(uid!!).update(updates).await()

                            dialogTitle = "Success"
                            dialogMessage = "Profile updated successfully!"
                            showDialog = true

                            // Clear password fields on success
                            oldPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            resizedImageBytes = null

                        } catch (e: Exception) {
                            dialogTitle = "Update Failed"
                            dialogMessage = e.message ?: "An unknown error occurred"
                            showDialog = true
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = customColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Spacer at the bottom to prevent Navbar from covering content
            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- FLOATING BOTTOM NAVBAR ---
        FloatingBottomNavbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            selectedItem = 3, // Profile is index 3
            onItemSelected = { index: Int ->
                // Handle Navigation
                if (index != 3) {
                    when (index) {
                        0 -> navController.navigate("main_menu")
                        1 -> navController.navigate("kiosk_cart") // Correct route
                        2 -> navController.navigate("order_details/{orderId}") // Or your order history screen
                    }
                }
            }
        )
    }
}

// --- Helpers ---

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    customColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = customColor,
            focusedLabelColor = customColor,
            cursorColor = customColor
        ),
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

// RENAMED TO AVOID CONFLICT with RegisterScreen.kt
private fun resizeProfileImage(context: Context, uri: Uri, width: Int, height: Int): ByteArray? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        originalBitmap?.let {
            val scaledBitmap = Bitmap.createScaledBitmap(it, width, height, true)
            val stream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.toByteArray()
        }
    } catch (e: Exception) {
        null
    }
}
