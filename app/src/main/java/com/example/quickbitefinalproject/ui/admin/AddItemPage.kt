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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun AddItemPage(navController: NavController, tabIndex: Int = 0) {

    val customColor = Color(0xFFAC0000)
    val darkGray = Color(0xFF8D838D)

    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var categoryList by remember { mutableStateOf(listOf("Burgers", "Drinks", "Snacks", "Desserts")) }
    var selectedCategory by remember { mutableStateOf("") }
    var addNewCategory by remember { mutableStateOf(false) }

    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryImage by remember { mutableStateOf<Uri?>(null) }
    var itemImageUri by remember { mutableStateOf<Uri?>(null) }
    var availability by remember { mutableStateOf(true) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // --------------------------------
    // SCREEN ANIMATION (ENTIRE COLUMN)
    // --------------------------------
    var screenVisible by remember { mutableStateOf(false) }
    val screenOffsetX by animateDpAsState(
        targetValue = if (screenVisible) 0.dp else 200.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )
    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = spring()
    )

    LaunchedEffect(Unit) { screenVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer {
                translationX = screenOffsetX.value
                alpha = screenAlpha
            }
    ) {

        // Back button
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
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // disables the gray hover/ripple
                        onClick = {
                            // Animate entire screen out
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

        Text("ADD ITEM", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
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

            // Price — numbers only
            OutlinedTextField(
                value = price,
                onValueChange = { if (it.all { c -> c.isDigit() }) price = it },
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
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                if (itemImageUri == null) Text("Upload Item Image")
                else AsyncImage(model = itemImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(customColor)
                ) {
                    categoryList.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = Color.White) },
                            onClick = {
                                selectedCategory = cat
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

            // New category
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
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    if (newCategoryImage == null) Text("Upload Category Image")
                    else AsyncImage(model = newCategoryImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }

            // Stock — numbers only
            OutlinedTextField(
                value = stock,
                onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
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
            ) { Text("Cancel") }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = { showSuccessDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = customColor),
                modifier = Modifier.weight(1f)
            ) { Text("Save") }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = Color(0xFFFFEEDA),
            tonalElevation = 0.dp,
            title = { Text("Success", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text("Item has been saved successfully.", fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTab", tabIndex)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = customColor)
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            }
        )
    }
}
