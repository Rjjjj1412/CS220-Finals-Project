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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quickbitefinalproject.R
import com.example.quickbitefinalproject.service.FirebaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun EditItemPage(
    navController: NavController,
    itemId: String?,
    tabIndex: Int = 0
) {
    val customColor = Color(0xFFAC0000)
    val darkGray = Color(0xFF8D838D)
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf(true) }

    var categoryList by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }

    var existingImageBase64 by remember { mutableStateOf("") }
    var newItemImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Animation
    var screenVisible by remember { mutableStateOf(false) }
    val screenOffsetX by animateDpAsState(if (screenVisible) 0.dp else 200.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "")
    val screenAlpha by animateFloatAsState(if (screenVisible) 1f else 0f, spring(), label = "")

    // Load Data
    LaunchedEffect(Unit) {
        screenVisible = true
        val catSnapshot = FirebaseService.db.collection("categories").get().await()
        categoryList = catSnapshot.documents.map { Pair(it.id, it.getString("name") ?: "") }

        if (itemId != null) {
            val doc = FirebaseService.db.collection("menu_items").document(itemId).get().await()
            if (doc.exists()) {
                itemName = doc.getString("name") ?: ""
                description = doc.getString("description") ?: ""
                price = doc.getDouble("price")?.toString() ?: ""
                availability = doc.getBoolean("available") ?: true
                selectedCategoryId = doc.getString("categoryId") ?: ""
                existingImageBase64 = doc.getString("imageUrl") ?: ""
                selectedCategoryName = categoryList.find { it.first == selectedCategoryId }?.second ?: ""
            }
        }
    }

    val itemLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newItemImageBytes = resizeImageForDb(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer { translationX = screenOffsetX.value; alpha = screenAlpha }
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back", tint = customColor,
                    modifier = Modifier.size(30.dp).clickable {
                        screenVisible = false
                        scope.launch {
                            delay(300)
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedTab", tabIndex)
                            navController.popBackStack()
                        }
                    }
                )
                Spacer(Modifier.width(10.dp))
                Text("EDIT ITEM", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = customColor)
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(scrollState).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            OutlinedTextField(itemName, { itemName = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor))

            OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor))

            OutlinedTextField(price, { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) price = it },
                label = { Text("Price") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColor, focusedLabelColor = customColor, cursorColor = customColor))

            // Image Picker
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp)).clickable { itemLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    newItemImageBytes != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(newItemImageBytes).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    existingImageBase64.isNotEmpty() -> {
                        val imageBytes = Base64.decode(existingImageBase64, Base64.DEFAULT)
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(imageBytes).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> Text("Upload Item Image")
                }
            }

            // Category Dropdown
            Text("Category", fontWeight = FontWeight.SemiBold)
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = selectedCategoryName, onValueChange = {}, enabled = false,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = darkGray)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(customColor)) {
                    categoryList.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.second, color = Color.White) }, onClick = {
                            selectedCategoryName = cat.second; selectedCategoryId = cat.first; expanded = false
                        })
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Available", fontWeight = FontWeight.SemiBold)
                Switch(checked = availability, onCheckedChange = { availability = it }, colors = SwitchDefaults.colors(checkedTrackColor = customColor))
            }

            Spacer(Modifier.height(20.dp))

            Button(onClick = {
                scope.launch {
                    focusManager.clearFocus(); isLoading = true
                    try {
                        if (itemId != null) {
                            val updates = hashMapOf<String, Any>(
                                "name" to itemName,
                                "description" to description,
                                "price" to (price.toDoubleOrNull() ?: 0.0),
                                "categoryId" to selectedCategoryId,
                                "available" to availability
                            )
                            if (newItemImageBytes != null) {
                                updates["imageUrl"] = Base64.encodeToString(newItemImageBytes, Base64.DEFAULT)
                            }
                            FirebaseService.db.collection("menu_items").document(itemId).update(updates).await()
                            dialogTitle = "Success"; dialogMessage = "Item updated!"; showDialog = true
                        }
                    } catch (e: Exception) {
                        dialogTitle = "Error"; dialogMessage = e.message ?: "Error"; showDialog = true
                    }
                    isLoading = false
                }
            }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = customColor), enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(color = Color.White) else Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // --- STANDARDIZED INFO DIALOG ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color.White,
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text(dialogMessage, color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = customColor)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- STANDARDIZED DELETE CONFIRMATION DIALOG ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color.White,
            title = { Text("Delete Item", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text("Are you sure you want to delete this item?", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (itemId != null) {
                                FirebaseService.db.collection("menu_items").document(itemId).delete().await()
                                navController.popBackStack()
                            }
                            showDeleteConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = customColor)
                ) { Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Reusing helper function
private fun resizeImageForDb(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (originalBitmap == null) return null
        val maxWidth = 500
        val maxHeight = 500
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
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
