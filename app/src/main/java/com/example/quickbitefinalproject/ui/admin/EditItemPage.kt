package com.example.quickbitefinalproject.ui.admin

import android.content.Context
import android.net.Uri
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.quickbitefinalproject.R
import com.example.quickbitefinalproject.service.FirebaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun EditItemPage(navController: NavController, itemId: String?, tabIndex: Int = 0) {
    val customColor = Color(0xFFAC0000)
    val darkGray = Color(0xFF8D838D)
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State variables
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var categoryList by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var addNewCategory by remember { mutableStateOf(false) }

    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryImage by remember { mutableStateOf<Uri?>(null) }
    
    // Item image: can be a new Uri (picked) or an existing String path (fetched)
    var itemImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingItemImagePath by remember { mutableStateOf<String?>(null) }
    
    var availability by remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // --- screen enter animation ---
    var screenVisible by remember { mutableStateOf(false) }
    val screenOffsetX by animateDpAsState(
        targetValue = if (screenVisible) 0.dp else 200.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = spring()
    )

    // Load data
    LaunchedEffect(Unit) {
        screenVisible = true
        
        // Load Categories
        FirebaseService.db.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                categoryList = snapshot?.documents?.map { doc ->
                    Pair(doc.id, doc.getString("name") ?: "")
                } ?: listOf()
            }

        // Load Item Details
        if (itemId != null) {
            try {
                val doc = FirebaseService.db.collection("items").document(itemId).get().await()
                if (doc.exists()) {
                    itemName = doc.getString("name") ?: ""
                    description = doc.getString("description") ?: ""
                    price = doc.getDouble("price")?.toString() ?: ""
                    stock = doc.getLong("stock")?.toString() ?: ""
                    availability = doc.getBoolean("available") ?: true
                    selectedCategoryId = doc.getString("categoryId") ?: ""
                    existingItemImagePath = doc.getString("localImagePath")

                    // We need to find the category name based on ID after categories are loaded
                    // But categories might load after item, so we handle it inside category listener or here if cached?
                    // For now, we will rely on the fact that categories are fetched. 
                    // But since snapshot listener is async, let's do a quick lookup if categories are empty initially.
                    // Actually, let's just fetch the category name for this ID specifically if needed, 
                    // or wait for the list.
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Sync selectedCategory name when list or ID changes
    LaunchedEffect(categoryList, selectedCategoryId) {
        if (selectedCategoryId.isNotEmpty()) {
            selectedCategory = categoryList.find { it.first == selectedCategoryId }?.second ?: ""
        }
    }

    // Image pickers
    val itemLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { itemImageUri = it }
    }
    val categoryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newCategoryImage = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer {
                translationX = screenOffsetX.value
                alpha = screenAlpha
            }
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {

        // --- Back button ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                tint = customColor,
                modifier = Modifier
                    .size(30.dp)
                    .clickable(
                        onClick = {
                            screenVisible = false
                            scope.launch {
                                delay(300)
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selectedTab", tabIndex)
                                navController.popBackStack()
                            }
                        }
                    )
            )
        }

        Text("EDIT ITEM", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.width(10.dp))

        // --- main form ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(20.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                    .clickable { itemLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Logic: Show new picked image if available, else show existing image, else show placeholder
                if (itemImageUri != null) {
                     AsyncImage(model = itemImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (!existingItemImagePath.isNullOrEmpty()) {
                     AsyncImage(model = Uri.parse(existingItemImagePath), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text("Upload Item Image")
                }
            }

            // Category dropdown
            Text("Select Category", fontWeight = FontWeight.SemiBold)
            var expanded by remember { mutableStateOf(false) }

            Box {
                OutlinedTextField(
                    value = if (addNewCategory) "" else selectedCategory.ifEmpty { if (categoryList.isEmpty()) "Loading..." else "" },
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    placeholder = { Text("Choose category") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = darkGray,
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

            // New category fields
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                        .clickable { categoryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (newCategoryImage == null) {
                        Text("Upload Category Image")
                    } else {
                        AsyncImage(
                            model = newCategoryImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Stock
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it.filter { c -> c.isDigit() } },
                label = { Text("Available Stock") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = customColor,
                    focusedLabelColor = customColor,
                    cursorColor = customColor
                )
            )

            // Availability
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Available", modifier = Modifier.weight(1f))
                Switch(
                    checked = availability,
                    onCheckedChange = { availability = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = customColor,
                        checkedTrackColor = customColor.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // --- Bottom buttons ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    screenVisible = false
                    scope.launch {
                        delay(200)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTab", tabIndex)
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        focusManager.clearFocus()
                        val error = validateEditInputs(itemName, description, price, stock, selectedCategory, addNewCategory, newCategoryName, itemImageUri, existingItemImagePath, if (addNewCategory) newCategoryImage else null)
                        if (error != null) { dialogTitle = "Error"; dialogMessage = error; showDialog = true; return@launch }
                        isLoading = true
                        try {
                            updateItem(
                                context,
                                itemId ?: "",
                                itemName,
                                description,
                                price,
                                stock,
                                selectedCategoryId,
                                availability,
                                itemImageUri,
                                existingItemImagePath,
                                addNewCategory,
                                newCategoryName,
                                newCategoryImage
                            )
                            dialogTitle = "Success"
                            dialogMessage = "Item has been updated successfully."
                            showDialog = true
                        } catch (e: Exception) {
                            dialogTitle = "Error"
                            dialogMessage = e.message ?: "Something went wrong"
                            showDialog = true
                        }
                        isLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = customColor),
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) { Text("Save") }
        }
    }

    // Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFFFFEEDA),
            tonalElevation = 0.dp,
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text(dialogMessage, fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        if (dialogTitle == "Success") {
                            screenVisible = false
                            scope.launch {
                                delay(300)
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selectedTab", tabIndex)
                                navController.popBackStack()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = customColor)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ------------------------------
// Helper Functions
// ------------------------------

fun validateEditInputs(
    itemName: String,
    description: String,
    price: String,
    stock: String,
    selectedCategory: String,
    addNewCategory: Boolean,
    newCategoryName: String,
    itemImage: Uri?,
    existingImagePath: String?,
    newCategoryImage: Uri?
): String? {
    if (itemName.isBlank()) return "Item name is required."
    if (description.isBlank()) return "Description is required."
    if (price.isBlank()) return "Price is required."
    if (price.toDoubleOrNull() == null || price.toDouble() <= 0.0) return "Price must be a positive number."
    // The regex below allows strict 2 decimal places check if needed
    if (!Regex("^\\d+(\\.\\d{1,2})?$").matches(price)) return "Price can only have up to 2 decimal places."
    if (stock.isBlank()) return "Stock is required."
    if (stock.toIntOrNull() == null || stock.toInt() < 0) return "Stock cannot be negative." // ALLOW 0
    
    // Image check: Must have either a new image OR an existing image
    if (itemImage == null && existingImagePath.isNullOrEmpty()) return "Item image is required."
    
    if (addNewCategory) {
        if (newCategoryName.isBlank()) return "New category name is required."
        if (newCategoryImage == null) return "New category image is required."
    } else if (selectedCategory.isBlank()) return "Please select a category."
    return null
}

suspend fun updateItem(
    context: Context,
    itemId: String,
    itemName: String,
    description: String,
    price: String,
    stock: String,
    selectedCategoryId: String,
    availability: Boolean,
    itemImageUri: Uri?,
    existingImagePath: String?,
    addNewCategory: Boolean,
    newCategoryName: String,
    newCategoryImage: Uri?
) {
    val db = FirebaseService.db
    var categoryId = selectedCategoryId

    // Save new category if needed
    if (addNewCategory) {
        val newCatDoc = db.collection("categories").document()
        val categoryData = mutableMapOf<String, Any>("name" to newCategoryName)
        if (newCategoryImage != null) {
             categoryData["localImagePath"] = saveImageToInternalStorage(context, newCategoryImage)
        }
        newCatDoc.set(categoryData).await()
        categoryId = newCatDoc.id
    }

    val updates = mutableMapOf<String, Any>(
        "name" to itemName,
        "description" to description,
        "price" to price.toDouble(),
        "stock" to stock.toInt(),
        "categoryId" to categoryId,
        "available" to availability
    )

    // If a new image was picked, save it and update path.
    if (itemImageUri != null) {
        val newPath = saveImageToInternalStorage(context, itemImageUri)
        updates["localImagePath"] = newPath
        // Optionally delete old image file here if needed, but for now we keep it simple
    }

    db.collection("items").document(itemId).update(updates).await()
}
