package com.example.quickbitefinalproject.ui.admin

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun EditItemPage(
    navController: NavController,
    itemId: String?,          // comes from NavGraph argument
    tabIndex: Int = 0         // remember which tab we came from
) {
    val customColor = Color(0xFFAC0000)
    val darkGray = Color(0xFF8D838D)
    val scope = rememberCoroutineScope()

    // Placeholder pre-filled values (simulate fetched data for now)
    var itemName by remember { mutableStateOf("Classic Burger") }
    var description by remember {
        mutableStateOf("Juicy beef patty with lettuce and tomato")
    }
    var price by remember { mutableStateOf("150") }
    var stock by remember { mutableStateOf("20") }

    var selectedCategory by remember { mutableStateOf("Burgers") }
    var addNewCategory by remember { mutableStateOf(false) }

    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryImage by remember { mutableStateOf<Uri?>(null) }
    var itemImageUri by remember { mutableStateOf<Uri?>(null) }
    var availability by remember { mutableStateOf(true) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // For now static list; later you could load from repository / Firebase
    val categoryList by remember {
        mutableStateOf(listOf("Burgers", "Drinks", "Snacks", "Desserts"))
    }

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

    LaunchedEffect(Unit) {
        screenVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer {
                translationX = screenOffsetX.value
                alpha = screenAlpha
            }
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
                                delay(200)
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selectedTab", tabIndex)
                                navController.popBackStack()
                            }
                        }
                    )
            )
        }

        Text(
            text = "EDIT ITEM: ${itemId ?: "-"}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

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
                    .clickable { /* TODO: open picker */ },
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
                        .clickable { /* TODO: category image picker */ },
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
                onClick = { showSuccessDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = customColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }

    // --- Success dialog ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = Color(0xFFFFEEDA),
            tonalElevation = 0.dp,
            title = {
                Text(
                    "Success",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    "Item has been updated successfully.",
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
                    colors = ButtonDefaults.textButtonColors(contentColor = customColor)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
