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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun AddItemPage(
    navController: NavController,
    tabIndex: Int = 0
) {
    val customColor = Color(0xFFAC0000)
    val darkGray = Color(0xFF8D838D)
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Form States
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // Category Logic
    var categoryList by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var addNewCategory by remember { mutableStateOf(false) }

    // New Category States
    var newCategoryName by remember { mutableStateOf("") }

    // Image States (Base64)
    var newCategoryImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var itemImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    var availability by remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Animation
    var screenVisible by remember { mutableStateOf(false) }
    val screenOffsetX by animateDpAsState(
        targetValue = if (screenVisible) 0.dp else 200.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "screenOffsetX"
    )
    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = spring(),
        label = "screenAlpha"
    )

    // Load Categories
    LaunchedEffect(Unit) {
        screenVisible = true
        FirebaseService.db.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                categoryList = snapshot?.documents?.map { doc ->
                    Pair(doc.id, doc.getString("name") ?: "")
                } ?: listOf()
            }
    }

    // Image pickers
    val itemLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { itemImageBytes = resizeImageForDb(context, it) }
    }
    val categoryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newCategoryImageBytes = resizeImageForDb(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer { translationX = screenOffsetX.value; alpha = screenAlpha }
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {

        // Back button
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                tint = customColor,
                modifier = Modifier
                    .size(30.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        screenVisible = false
                        scope.launch {
                            delay(300)
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedTab", tabIndex)
                            navController.popBackStack()
                        }
                    }
            )
        }

        Text(
            text = "ADD ITEM",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(scrollState).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Item Name
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = customColor,
                    focusedLabelColor = customColor,
                    cursorColor = customColor
                )
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = customColor,
                    focusedLabelColor = customColor,
                    cursorColor = customColor
                )
            )

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        price = it
                    }
                },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = customColor,
                    focusedLabelColor = customColor,
                    cursorColor = customColor
                )
            )

            // Item Image
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                    .clickable { itemLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (itemImageBytes == null) {
                    Text("Upload Item Image")
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(itemImageBytes).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Category dropdown
            Text("Select Category", fontWeight = FontWeight.SemiBold)
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = if (addNewCategory) "" else selectedCategory.ifEmpty { if (categoryList.isEmpty()) "No categories yet" else "" },
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    placeholder = { Text(if (categoryList.isEmpty()) "No categories yet" else "Choose category") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = darkGray,
                        disabledLabelColor = darkGray
                    )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(customColor)
                ) {
                    categoryList.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.second, color = Color.White) },
                            onClick = {
                                selectedCategory = cat.second
                                selectedCategoryId = cat.first
                                addNewCategory = false
                                expanded = false
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("+ Add New Category", color = Color.White) },
                        onClick = {
                            addNewCategory = true
                            selectedCategory = ""
                            expanded = false
                        }
                    )
                }
            }

            // New category section
            if (addNewCategory) {
                Text("New Category", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColor,
                        focusedLabelColor = customColor,
                        cursorColor = customColor
                    )
                )

                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                        .clickable { categoryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (newCategoryImageBytes == null) {
                        Text("Upload Category Image")
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(newCategoryImageBytes).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Availability
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Available", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = availability,
                    onCheckedChange = { availability = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = customColor)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Add Button
            Button(
                onClick = {
                    scope.launch {
                        focusManager.clearFocus()
                        isLoading = true

                        if (itemName.isEmpty() || price.isEmpty()) {
                            dialogTitle = "Error"
                            dialogMessage = "Please fill Item Name and Price."
                            showDialog = true
                            isLoading = false
                            return@launch
                        }

                        try {
                            var finalCatId = selectedCategoryId

                            // 1. Handle New Category Creation
                            if (addNewCategory) {
                                if (newCategoryName.isEmpty()) {
                                    dialogTitle = "Error"; dialogMessage = "Enter Category Name."; showDialog = true; isLoading = false
                                    return@launch
                                }
                                val catMap = hashMapOf<String, Any>("name" to newCategoryName)
                                if (newCategoryImageBytes != null) {
                                    catMap["imageUrl"] = Base64.encodeToString(newCategoryImageBytes, Base64.DEFAULT)
                                }
                                val newCatRef = FirebaseService.db.collection("categories").add(catMap).await()
                                finalCatId = newCatRef.id
                            } else if (finalCatId.isEmpty()) {
                                dialogTitle = "Error"; dialogMessage = "Select a Category."; showDialog = true; isLoading = false
                                return@launch
                            }

                            // 2. Create Item Map
                            val itemMap = hashMapOf(
                                "name" to itemName,
                                "description" to description,
                                "price" to (price.toDoubleOrNull() ?: 0.0),
                                "categoryId" to finalCatId,
                                "available" to availability
                            )
                            if (itemImageBytes != null) {
                                itemMap["imageUrl"] = Base64.encodeToString(itemImageBytes, Base64.DEFAULT)
                            }

                            FirebaseService.db.collection("menu_items").add(itemMap).await()

                            dialogTitle = "Success"
                            dialogMessage = "Item added successfully!"
                            showDialog = true

                            // Reset
                            itemName = ""; description = ""; price = ""; itemImageBytes = null
                            newCategoryName = ""; newCategoryImageBytes = null; addNewCategory = false

                        } catch (e: Exception) {
                            dialogTitle = "Error"
                            dialogMessage = e.message ?: "Unknown error"
                            showDialog = true
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = customColor),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White) else Text("ADD ITEM", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- STANDARDIZED DIALOG ---
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
}

// Helper function for resizing images
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
