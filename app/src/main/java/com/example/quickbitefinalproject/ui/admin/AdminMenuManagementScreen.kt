package com.example.quickbitefinalproject.ui.admin

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import coil.compose.AsyncImage
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
    val localImagePath: String
)

data class CategoryItem(
    val id: String,
    val name: String,
    val localImagePath: String,
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
    val redColor = Color(0xFFAC0000)
    val shadowColor = Color(0xFFFFEEDA)
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    // Tabs
    val tabs = listOf("Categories", "Products")

    var selectedTab by remember { mutableStateOf(savedStateHandle?.get<Int>("selectedTab") ?: 0) }
    val pagerState = rememberPagerState(initialPage = selectedTab) { tabs.size }

    var categories by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var products by remember { mutableStateOf<List<ProductItem>>(emptyList()) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(savedStateHandle?.get<String>("selectedCategoryId")) }
    var showDeleteDialog by remember { mutableStateOf<Pair<Boolean, ProductItem?>>(false to null) }

    var catListener: ListenerRegistration? = remember { null }
    var prodListener: ListenerRegistration? = remember { null }

    // Save state to Handle
    LaunchedEffect(selectedCategoryId) {
        savedStateHandle?.set("selectedCategoryId", selectedCategoryId)
    }

    // Animation variables
    var backPressed by remember { mutableStateOf(false) }
    val overshootOffset = 30f
    val targetOffsetX by animateFloatAsState(if (backPressed) 120f else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    val overshootAlpha by animateFloatAsState(if (backPressed) 0f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
    val targetScale by animateFloatAsState(if (backPressed) 0.95f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    val overshootX by animateFloatAsState(if (backPressed) overshootOffset else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))

    // ------------------------------
    // Real-time Firestore listeners
    // ------------------------------
    DisposableEffect(true) {
        catListener = db.collection("categories")
            .addSnapshotListener { snap, _ ->
                snap?.let {
                    categories = it.documents.map { doc ->
                        CategoryItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            localImagePath = doc.getString("localImagePath") ?: "",
                            itemCount = 0
                        )
                    }
                    // Update item counts
                    val counts = products.groupingBy { it.categoryId }.eachCount()
                    categories = categories.map { c -> c.copy(itemCount = counts[c.id] ?: 0) }
                }
            }

        prodListener = db.collection("items")
            .whereEqualTo("available", true)
            .addSnapshotListener { snap, _ ->
                snap?.let {
                    products = it.documents.map { doc ->
                        ProductItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            categoryId = doc.getString("categoryId") ?: "",
                            localImagePath = doc.getString("localImagePath") ?: ""
                        )
                    }
                    // Update category counts
                    val counts = products.groupingBy { it.categoryId }.eachCount()
                    categories = categories.map { c -> c.copy(itemCount = counts[c.id] ?: 0) }
                }
            }

        onDispose {
            catListener?.remove()
            prodListener?.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)
        .graphicsLayer {
            translationX = targetOffsetX - overshootX
            alpha = overshootAlpha
            scaleX = targetScale
            scaleY = targetScale
        }) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = redColor,
                    modifier = Modifier.size(30.dp).clickable {
                        backPressed = true
                        scope.launch {
                            delay(300)
                            navController.popBackStack()
                        }
                    }
                )
            }
            Text("MENU", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

            TabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.White, contentColor = Color.Black, indicator = { positions ->
                Box(Modifier.tabIndicatorOffset(positions[pagerState.currentPage]).height(4.dp).background(redColor, RoundedCornerShape(2.dp)))
            }) {
                tabs.forEachIndexed { index, title ->
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        Tab(selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title, color = if (pagerState.currentPage == index) redColor else Color.Black) },
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> AdminCategoriesTab(categories = categories) { catId ->
                        selectedCategoryId = if (selectedCategoryId == catId) null else catId
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
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

        PressableFAB(
            onClick = { navController.navigate("add_item_page?tabIndex=${pagerState.currentPage}") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 60.dp),
            containerColor = redColor,
            iconColor = Color.White,
            shadowColor = shadowColor
        )
    }

    // Delete confirmation
    if (showDeleteDialog.first && showDeleteDialog.second != null) {
        val item = showDeleteDialog.second!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false to null },
            containerColor = Color(0xFFFFEEDA),
            tonalElevation = 0.dp,
            title = { Text("Delete Item", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text("Are you sure you want to delete '${item.name}'?", fontSize = 16.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseFirestore.getInstance().collection("items").document(item.id)
                            .delete()
                        showDeleteDialog = false to null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAC0000))
                ) { Text("Yes", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false to null }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                    Text("No", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ------------------------------
// Categories Tab
// ------------------------------
@Composable
fun AdminCategoriesTab(categories: List<CategoryItem>, onCategoryClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(categories) { category ->
            AdminCategoryCard(title = category.name, itemCount = category.itemCount, imagePath = category.localImagePath) {
                onCategoryClick(category.id)
            }
        }
    }
}

@Composable
fun AdminCategoryCard(title: String, itemCount: Int, imagePath: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3EA)),
        modifier = Modifier.fillMaxWidth().height(110.dp).clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = Uri.parse(imagePath), contentDescription = null, contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.ic_launcher_background),
                modifier = Modifier.padding(start = 12.dp).size(86.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.width(18.dp))
            Column {
                Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAC0000))
                Spacer(Modifier.height(6.dp))
                Text("$itemCount Items", fontSize = 15.sp, color = Color(0xFF646464))
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
    categories: List<CategoryItem>,
    selectedCategoryId: String?,
    onEditClick: (ProductItem) -> Unit,
    onDeleteClick: (ProductItem) -> Unit,
    onClearFilter: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedCategoryId != null) {
            val catName = categories.find { it.id == selectedCategoryId }?.name ?: "Category"
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8D9)),
                    modifier = Modifier.clickable { onClearFilter() }
                ) {
                    Text("Filter: $catName  ×", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = Color(0xFFAC0000))
                }
            }
        }

        if (products.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items available", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(products) { item ->
                    AdminProductCard(item, onEdit = { onEditClick(item) }, onDelete = { onDeleteClick(item) })
                }
            }
        }
    }
}

@Composable
fun AdminProductCard(item: ProductItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8D9)),
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AnimatedIcon(R.drawable.ic_edit, onClick = onEdit)
                AnimatedIcon(R.drawable.ic_delete, onClick = onDelete)
            }
            AsyncImage(model = Uri.parse(item.localImagePath), contentDescription = null, contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.ic_launcher_background),
                modifier = Modifier.size(100.dp).align(Alignment.Center))
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Text("${String.format("%.2f", item.price)}$", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFAC0000))
            }
        }
    }
}

// ------------------------------
// Animated Icon
// ------------------------------
@Composable
fun AnimatedIcon(iconRes: Int, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )
    Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color(0xFFAC0000),
        modifier = Modifier.size(24.dp).graphicsLayer(scaleX = scale, scaleY = scale).pointerInput(Unit) {
            detectTapGestures(onPress = {
                pressed = true
                tryAwaitRelease()
                pressed = false
                onClick()
            })
        })
}

// ------------------------------
// Pressable FAB
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
    val scale by animateFloatAsState(targetValue = if (pressed) 1.5f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    FloatingActionButton(onClick = onClick, containerColor = containerColor, contentColor = iconColor,
        shape = CircleShape, modifier = modifier.size(70.dp).graphicsLayer(scaleX = scale, scaleY = scale).pointerInput(Unit) {
            detectTapGestures(onPress = {
                pressed = true
                tryAwaitRelease()
                pressed = false
            })
        },
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp)) {
        Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(36.dp))
    }
}
