package com.example.quickbitefinalproject.ui.admin

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quickbitefinalproject.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddItemPage(
    navController: NavController,
    tabIndex: Int = 0
) {
    val customColor = Color(0xFFAC0000)
    val darkGray = Color(0xFF8D838D)

    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var categoryList by remember {
        mutableStateOf(listOf("Burgers", "Drinks", "Snacks", "Desserts"))
    }
    var selectedCategory by remember { mutableStateOf("") }
    var addNewCategory by remember { mutableStateOf(false) }

    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryImage by remember { mutableStateOf<Uri?>(null) }
    var itemImageUri by remember { mutableStateOf<Uri?>(null) }
    var availability by remember { mutableStateOf(true) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // ---------- Screen enter animation ----------
    var screenVisible by remember { mutableStateOf(false) }
    val screenOffsetX by animateDpAsState(
        targetValue = if (screenVisible) 0.dp else 200.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "screenOffsetX"
    )
    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = spring(),
        label = "screenAlpha"
    )

    LaunchedEffect(Unit) {
        screenVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer {
                // simple slide-in animation
                translationX = screenOffsetX.value
                alpha = screenAlpha
            }
    ) {
        // Back button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
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
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedTab", tabIndex)
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
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Item name
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

            // Price (allow digits + one decimal point)
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    if (input.isBlank() ||
                        input.all { it.isDigit() || it == '.' } &&
                        input.count { it == '.' } <= 1
                    ) {
                        price = input
                    }
                },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    .clickable {
                        // TODO: open image picker
                    },
                contentAlignment = Alignment.Center
            ) {
                if (itemImageUri == null) {
                    Text("Upload Item Image")
                } else {
                    AsyncImage(
                        model = itemImageUri,
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
                    value = selectedCategory,
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

                androidx.compose.material3.DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(customColor)
                ) {
                    categoryList.forEach { cat ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(cat, color = Color.White) },
                            onClick = {
                                selectedCategory = cat
                                addNewCategory = false
                                expanded = false
                            }
                        )
                    }
                    androidx.compose.material3.DropdownMenuItem(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                        .clickable {
                            // TODO: open image picker
                        },
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

            // Stock — digits only
            OutlinedTextField(
                value = stock,
                onValueChange = { input ->
                    if (input.isBlank() || input.all { it.isDigit() }) {
                        stock = input
                    }
                },
                label = { Text("Available Stock") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = customColor,
                    focusedLabelColor = customColor,
                    cursorColor = customColor
                )
            )

            // Availability switch
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

        // Bottom buttons
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
                        delay(300)
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

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    // TODO: validate + save to Firebase / DB
                    showSuccessDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = customColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = Color(0xFFFFEEDA),
            title = {
                Text(
                    text = "Success",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Item has been saved successfully.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTab", tabIndex)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = customColor
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
