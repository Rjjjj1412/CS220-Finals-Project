package com.example.quickbitefinalproject.ui.admin

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ------------------------------
// Model
// ------------------------------
data class ProductItem(
    val id: String,
    val name: String,
    val price: Double,
    val category: String
)

// ------------------------------
// Dummy products
// ------------------------------
val dummyProducts = listOf(
    ProductItem("1", "Zinger Burger", 2.0, "Burgers"),
    ProductItem("2", "Long Burger", 5.0, "Burgers"),
    ProductItem("3", "Sandwich", 2.0, "Sandwiches"),
    ProductItem("4", "Beef Burger", 3.0, "Burgers"),
    ProductItem("5", "Roll Paratha", 3.0, "Sides")
)

// ------------------------------
// Admin Menu Management Screen
// ------------------------------
@Composable
fun AdminMenuManagementScreen(navController: NavController) {
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var selectedTab by remember { mutableStateOf(savedStateHandle?.get<Int>("selectedTab") ?: 0) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var backPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val redColor = Color(0xFFAC0000)
    val shadowColor = Color(0xFFFFEEDA)

    val targetOffsetX by animateDpAsState(if (backPressed) 120.dp else 0.dp)
    val alpha by animateFloatAsState(if (backPressed) 0f else 1f)

    var showDeleteDialog by remember { mutableStateOf<Pair<Boolean, ProductItem?>>(false to null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .graphicsLayer {
                    translationX = targetOffsetX.value
                    this.alpha = alpha
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
                    tint = redColor,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(
                            onClick = {
                                backPressed = true
                                scope.launch {
                                    delay(300)
                                    navController.popBackStack()
                                }
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                )
            }

            Text(
                text = "MENU",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Tabs with smooth sliding indicator
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    val currentTab = tabPositions[selectedTab]
                    Box(
                        Modifier
                            .tabIndicatorOffset(currentTab)
                            .height(4.dp)
                            .background(redColor, shape = RoundedCornerShape(2.dp))
                    )
                }
            ) {
                val tabs = listOf("Categories", "Products")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            savedStateHandle?.set("selectedTab", index)
                        },
                        text = { Text(title, color = if (selectedTab == index) redColor else Color.Black) },
                        interactionSource = remember { MutableInteractionSource() }, // removes gray hover
                        selectedContentColor = redColor,
                        unselectedContentColor = Color.Black
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> AdminCategoriesTab(
                    onCategoryClick = { category ->
                        selectedCategory = category
                        selectedTab = 1
                    }
                )
                1 -> AdminProductsTab(
                    products = if (selectedCategory == null) dummyProducts
                    else dummyProducts.filter { it.category == selectedCategory },
                    onEditClick = { item ->
                        navController.navigate("edit_item/${item.id}?tabIndex=$selectedTab")
                   },
                    onDeleteClick = { item -> showDeleteDialog = true to item }
                )
            }
        }

        // Animated Circular FAB with shadow FFEEDA
        PressableFAB(
            onClick = {
                navController.navigate("add_item_page?tabIndex=$selectedTab")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 60.dp),
            containerColor = redColor,
            iconColor = Color.White,
            shadowColor = shadowColor
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog.first && showDeleteDialog.second != null) {
        val itemToDelete = showDeleteDialog.second!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false to null },
            containerColor = Color(0xFFFFEEDA),
            tonalElevation = 0.dp,
            title = { Text("Delete Item", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text("Are you sure you want to delete '${itemToDelete.name}'?", fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false to null /* TODO delete */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = redColor)
                ) { Text("Yes", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false to null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF646464))
                ) { Text("No", fontWeight = FontWeight.Bold) }
            }
        )
    }
}

// ------------------------------
// Categories Tab
// ------------------------------
@Composable
fun AdminCategoriesTab(onCategoryClick: (String) -> Unit) {
    val categories = listOf("Burgers", "Sandwiches", "Sides", "Drinks", "Desserts")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { categoryName ->
            AdminCategoryCard(
                title = categoryName,
                itemCount = (3..12).random(),
                onClick = { onCategoryClick(categoryName) }
            )
        }
    }
}

@Composable
fun AdminCategoryCard(title: String, itemCount: Int, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3EA)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.placeholder_food),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(86.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(18.dp))
            Column {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAC0000))
                Spacer(Modifier.height(6.dp))
                Text("$itemCount Items", fontSize = 14.sp, color = Color(0xFF646464))
            }
        }
    }
}

// ------------------------------
// Products Tab
// ------------------------------
@Composable
fun AdminProductsTab(
    products: List<ProductItem>,
    onEditClick: (ProductItem) -> Unit,
    onDeleteClick: (ProductItem) -> Unit
) {
    if (products.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items available", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { item ->
                AdminProductCard(item, onEdit = { onEditClick(item) }, onDelete = { onDeleteClick(item) })
            }
        }
    }
}

@Composable
fun AdminProductCard(item: ProductItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8D9)),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top-right icons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedIcon(R.drawable.ic_edit, onClick = onEdit)
                AnimatedIcon(R.drawable.ic_delete, onClick = onDelete)
            }

            // Product image (center, no shadow)
            Image(
                painter = painterResource(id = R.drawable.placeholder_food),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp).align(Alignment.Center)
            )

            // Name & price at bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Text("${item.price}$", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFAC0000))
            }
        }
    }
}

// ------------------------------
// Animated Icon for Edit/Delete
// ------------------------------
@Composable
fun AnimatedIcon(iconRes: Int, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = Color(0xFFAC0000),
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            // Wait for release or cancel
                            tryAwaitRelease()
                        } finally {
                            pressed = false
                        }
                        onClick() // trigger the click after release
                    }
                )
            }
    )
}

// ------------------------------
// Pressable FAB Composable
// ------------------------------
@Composable
fun PressableFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Red,
    iconColor: Color = Color.White,
    shadowColor: Color = Color(0xFFFFEEDA)
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = iconColor,
        shape = CircleShape,
        modifier = modifier
            .size(70.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(36.dp))
    }
}
