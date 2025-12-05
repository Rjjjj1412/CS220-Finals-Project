package com.example.quickbitefinalproject.ui.admin

import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quickbitefinalproject.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ------------------------------
// Models
// ------------------------------
data class ProductItem(
    val id: String,
    val name: String,
    val price: Double,
    val categoryId: String,
    val imageBase64: String
)

data class CategoryItem(
    val id: String,
    val name: String,
    val imageBase64: String,
    val itemCount: Int
)

// ------------------------------
// Admin Menu Management Screen
// ------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuManagementScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val customColor = Color(0xFFAC0000)
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    // Tabs
    val tabs = listOf("Categories", "Products")

    var selectedTab by remember { mutableStateOf(savedStateHandle?.get<Int>("selectedTab") ?: 0) }
    val pagerState = rememberPagerState(initialPage = selectedTab) { tabs.size }

    var categories by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var products by remember { mutableStateOf<List<ProductItem>>(emptyList()) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(savedStateHandle?.get<String>("selectedCategoryId")) }

    // Deletion State
    var showDeleteDialog by remember { mutableStateOf<Pair<Boolean, ProductItem?>>(false to null) }

    var catListener: ListenerRegistration? = remember { null }
    var prodListener: ListenerRegistration? = remember { null }

    // DISABLE BACK BUTTON
    BackHandler(enabled = true) { }

    // Save state to Handle
    LaunchedEffect(selectedCategoryId) {
        savedStateHandle?.set("selectedCategoryId", selectedCategoryId)
    }

    // Animation variables
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
    val targetScale by animateFloatAsState(
        if (backPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )
    val overshootX by animateFloatAsState(
        if (backPressed) overshootOffset else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )

    // ------------------------------
    // Real-time Firestore listeners
    // ------------------------------
    DisposableEffect(true) {
        // 1. Fetch Categories
        catListener = db.collection("categories")
            .addSnapshotListener { snap, _ ->
                snap?.let {
                    categories = it.documents.map { doc ->
                        CategoryItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            imageBase64 = doc.getString("imageUrl") ?: "",
                            itemCount = 0
                        )
                    }
                    val counts = products.groupingBy { it.categoryId }.eachCount()
                    categories = categories.map { c -> c.copy(itemCount = counts[c.id] ?: 0) }
                }
            }

        // 2. Fetch Menu Items
        prodListener = db.collection("menu_items")
            .addSnapshotListener { snap, _ ->
                snap?.let {
                    products = it.documents.map { doc ->
                        ProductItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            categoryId = doc.getString("categoryId") ?: "",
                            imageBase64 = doc.getString("imageUrl") ?: ""
                        )
                    }
                    val counts = products.groupingBy { it.categoryId }.eachCount()
                    categories = categories.map { c -> c.copy(itemCount = counts[c.id] ?: 0) }
                }
            }

        onDispose {
            catListener?.remove()
            prodListener?.remove()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .graphicsLayer {
            translationX = targetOffsetX - overshootX
            alpha = overshootAlpha
            scaleX = targetScale
            scaleY = targetScale
        }) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = customColor,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            backPressed = true
                            scope.launch {
                                delay(300)
                                navController.popBackStack()
                            }
                        }
                )
            }
            Text("MENU MANAGEMENT", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

            // Tab Row
            TabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.White, contentColor = Color.Black, indicator = { positions ->
                Box(Modifier
                    .tabIndicatorOffset(positions[pagerState.currentPage])
                    .height(4.dp)
                    .background(customColor, RoundedCornerShape(2.dp)))
            }) {
                tabs.forEachIndexed { index, title ->
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        Tab(selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title, color = if (pagerState.currentPage == index) customColor else Color.Black, fontWeight = FontWeight.Bold) },
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    }
                }
            }

            // Pager Content
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> AdminCategoriesTab(
                        categories = categories,
                        onCategoryClick = { catId ->
                            selectedCategoryId = if (selectedCategoryId == catId) null else catId
                            scope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )
                    1 -> AdminProductsTab(
                        products = if (selectedCategoryId == null) products else products.filter { it.categoryId == selectedCategoryId },
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onEditClick = { item -> navController.navigate("edit_item/${item.id}?tabIndex=${pagerState.currentPage}") },
                        onDeleteClick = { item -> showDeleteDialog = true to item },
                        onClearFilter = { selectedCategoryId = null }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { navController.navigate("add_item_page?tabIndex=${pagerState.currentPage}") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 40.dp),
            containerColor = customColor,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog.first && showDeleteDialog.second != null) {
        val item = showDeleteDialog.second!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false to null },
            containerColor = Color.White,
            title = { Text("Delete Item", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text("Are you sure you want to delete '${item.name}'?", fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseFirestore.getInstance().collection("menu_items").document(item.id).delete()
                        showDeleteDialog = false to null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = customColor)
                ) { Text("Yes, Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false to null }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ------------------------------
// 1. Categories Tab
// ------------------------------
@Composable
fun AdminCategoriesTab(
    categories: List<CategoryItem>,
    onCategoryClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp), // Added bottom padding
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onCategoryClick(category.id) },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (category.imageBase64.isNotEmpty()) {
                        val imageBytes = try {
                            Base64.decode(category.imageBase64, Base64.DEFAULT)
                        } catch (e: Exception) { null }

                        if (imageBytes != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(imageBytes).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(Modifier.fillMaxSize().background(Color.LightGray))
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(category.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${category.itemCount} items", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------
// 2. Products Tab
// ------------------------------
@Composable
fun AdminProductsTab(
    products: List<ProductItem>,
    categories: List<CategoryItem>,
    selectedCategoryId: String?,
    onEditClick: (ProductItem) -> Unit,
    onDeleteClick: (ProductItem) -> Unit,
    onClearFilter: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Indicator
        if (selectedCategoryId != null) {
            val catName = categories.find { it.id == selectedCategoryId }?.name ?: "Unknown"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color(0xFFFFEEDA), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Filtering by: $catName", color = Color(0xFFAC0000), fontWeight = FontWeight.Bold)
                IconButton(onClick = onClearFilter, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Filter", tint = Color(0xFFAC0000))
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp), // Added bottom padding
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(70.dp)
                        ) {
                            if (product.imageBase64.isNotEmpty()) {
                                val imageBytes = try {
                                    Base64.decode(product.imageBase64, Base64.DEFAULT)
                                } catch (e: Exception) { null }

                                if (imageBytes != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(imageBytes).build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                Box(Modifier.fillMaxSize().background(Color.LightGray))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("₱${String.format("%.2f", product.price)}", color = Color.Gray, fontSize = 14.sp)

                            val catName = categories.find { it.id == product.categoryId }?.name ?: ""
                            if (catName.isNotEmpty()) {
                                Text(catName, color = Color(0xFFAC0000), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row {
                            IconButton(onClick = { onEditClick(product) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                            }
                            IconButton(onClick = { onDeleteClick(product) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFAC0000))
                            }
                        }
                    }
                }
            }
        }
    }
}
