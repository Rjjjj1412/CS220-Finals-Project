package com.example.quickbitefinalproject.ui.kiosk

import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
// The 'Remove' icon that was causing issues is no longer needed.
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quickbitefinalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- Models ---
data class MainCategoryItem(
    val id: String,
    val name: String,
    val imageBase64: String
)

data class MainProductItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: String,
    val available: Boolean,
    val imageBase64: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavController) {
    val quickBiteRed = Color(0xFFAC0000)
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- Data States ---
    var categories by remember { mutableStateOf<List<MainCategoryItem>>(emptyList()) }
    var allProducts by remember { mutableStateOf<List<MainProductItem>>(emptyList()) }

    // --- View States ---
    var selectedCategory by remember { mutableStateOf<MainCategoryItem?>(null) }
    var selectedProduct by remember { mutableStateOf<MainProductItem?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- Dialog States ---
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var itemToAdd by remember { mutableStateOf<Pair<MainProductItem, Int>?>(null) }


    // Navigation state
    var selectedNavItem by remember { mutableStateOf(0) }

    // --- Back Handler ---
    BackHandler(enabled = selectedCategory != null || selectedProduct != null) {
        if (selectedProduct != null) {
            selectedProduct = null
        } else if (selectedCategory != null) {
            selectedCategory = null
        }
    }

    // --- Fetch Data ---
    LaunchedEffect(true) {
        if (auth.currentUser == null) {
            navController.navigate("user_auth") { popUpTo("main_menu") { inclusive = true } }
            return@LaunchedEffect
        }

        // 1. Fetch Categories
        db.collection("categories").addSnapshotListener { snapshot, e ->
            if (e != null) { errorMessage = e.message; return@addSnapshotListener }
            if (snapshot != null) {
                categories = snapshot.documents.map { doc ->
                    MainCategoryItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        imageBase64 = doc.getString("imageUrl") ?: ""
                    )
                }
            }
        }

        // 2. Fetch All Products
        db.collection("menu_items").whereEqualTo("available", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { errorMessage = e.message; isLoading = false; return@addSnapshotListener }
                if (snapshot != null) {
                    allProducts = snapshot.documents.map { doc ->
                        MainProductItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            categoryId = doc.getString("categoryId") ?: "",
                            available = doc.getBoolean("available") ?: true,
                            imageBase64 = doc.getString("imageUrl") ?: ""
                        )
                    }
                    isLoading = false
                }
            }
    }

    // --- Add to Cart Function ---
    fun addToCart(product: MainProductItem, quantity: Int) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "You must be logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                // Create a map for the cart item
                val cartItem = hashMapOf(
                    "productId" to product.id,
                    "name" to product.name,
                    "price" to product.price,
                    "quantity" to quantity,
                    "imageBase64" to product.imageBase64 // Storing image for easy display in cart
                )
                // Add to a 'cart' sub-collection under the user's document
                db.collection("users").document(userId)
                    .collection("cart").document(product.id) // Use product ID as doc ID to prevent duplicates
                    .set(cartItem)
                    .await()

                showSuccessDialog = true // Show success message
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to add to cart: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "QuickBite Kiosk", fontWeight = FontWeight.Bold, color = quickBiteRed) },
                actions = {
                    TextButton(
                        onClick = {
                            auth.signOut()
                            navController.navigate("user_auth") { popUpTo(0) { inclusive = true } }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = quickBiteRed)
                    ) { Text(text = "Logout", fontWeight = FontWeight.Bold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = quickBiteRed)
                }
            } else {

                // --- VIEW LOGIC ---
                when {
                    // 3. PRODUCT DETAILS VIEW
                    selectedProduct != null -> {
                        ProductDetailsView(
                            product = selectedProduct!!,
                            onBack = { selectedProduct = null },
                            onAddToCart = { product, quantity ->
                                itemToAdd = Pair(product, quantity)
                                showConfirmDialog = true
                            }
                        )
                    }

                    // 2. PRODUCT LIST VIEW (Minimal)
                    selectedCategory != null -> {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)) {

                            // Close button is now BEFORE the category name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { selectedCategory = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Filter", tint = Color.Gray)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = selectedCategory!!.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            val filteredProducts = allProducts.filter { it.categoryId == selectedCategory!!.id }

                            if (filteredProducts.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No items available.", color = Color.Gray)
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 100.dp)
                                ) {
                                    items(filteredProducts) { product ->
                                        MainProductCard(
                                            product = product,
                                            onClick = { selectedProduct = product }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1. CATEGORY LIST VIEW (Default)
                    else -> {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.quickbite_logo),
                                    contentDescription = "QuickBite Logo",
                                    modifier = Modifier.width(190.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Pick a category to start your order",
                                    fontSize = 14.sp,
                                    color = Color(0xFF646464),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(Modifier.height(18.dp))
                            Text(
                                text = "Categories",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Spacer(Modifier.height(8.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                items(categories) { category ->
                                    MainCategoryCard(
                                        category = category,
                                        onClick = { selectedCategory = category }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // FLOATING BOTTOM NAVBAR
            if (selectedProduct == null) {
                FloatingBottomNavbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    selectedItem = selectedNavItem,
                    onItemSelected = { index: Int ->
                        selectedNavItem = index
                        when (index) {
                            0 -> {
                                selectedProduct = null
                                selectedCategory = null
                            }
                            1 -> navController.navigate("kiosk_cart")
                            2 -> navController.navigate("order_details/{orderId}")
                            3 -> navController.navigate("user_profile")
                        }
                    }
                )
            }

            // --- DIALOGS ---

            // Confirmation Dialog
            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    containerColor = Color.White,
                    title = { Text("Confirm", fontWeight = FontWeight.Bold, color = Color.Black) },
                    text = { Text("Add ${itemToAdd?.second} x ${itemToAdd?.first?.name} to your cart?", color = Color.Black) },
                    confirmButton = {
                        Button(
                            onClick = {
                                itemToAdd?.let { (product, quantity) ->
                                    addToCart(product, quantity)
                                }
                                showConfirmDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed)
                        ) {
                            Text("Yes, Add", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    containerColor = Color.White,
                    title = { Text("Success!", fontWeight = FontWeight.Bold, color = Color.Black) },
                    text = { Text("Item has been added to your cart.", color = Color.Black) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                selectedProduct = null // Go back to product list
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}


// --- SUB-COMPOSABLE ---

@Composable
fun ProductDetailsView(
    product: MainProductItem,
    onBack: () -> Unit,
    onAddToCart: (MainProductItem, Int) -> Unit
) {
    val quickBiteRed = Color(0xFFAC0000)
    val context = LocalContext.current
    var quantity by remember { mutableIntStateOf(1) }

    // Decode the image bytes only once to prevent flickering
    val imageBytes = remember(product.imageBase64) {
        try { Base64.decode(product.imageBase64, Base64.DEFAULT) } catch (e: Exception) { null }
    }

    LaunchedEffect(product.id) { quantity = 1 }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // 1. Large Image Header
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.LightGray)) {
                if (imageBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(imageBytes).crossfade(true).build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.White.copy(alpha = 0.7f), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }

            // 2. Content Body
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₱${String.format("%.2f", product.price)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = quickBiteRed
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if(product.available) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if(product.available) "Available" else "Out of Stock",
                        color = if(product.available) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if(product.description.isNotEmpty()) product.description else "No description available.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
                if (product.available) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape).border(1.dp, Color.LightGray, CircleShape)
                        ) {
                            // Using Text composable for minus symbol
                            Text(
                                text = "—",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = quantity.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape).border(1.dp, Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        if (product.available) {
            Button(
                onClick = { onAddToCart(product, quantity) },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed)
            ) {
                Text("Add to Cart - ₱${String.format("%.2f", product.price * quantity)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MainCategoryCard(category: MainCategoryItem, onClick: () -> Unit) {
    val quickBiteRed = Color(0xFFAC0000)
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3EA)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            if (category.imageBase64.isNotEmpty()) {
                val imageBytes = remember(category.imageBase64) {
                    try { Base64.decode(category.imageBase64, Base64.DEFAULT) } catch (e: Exception) { null }
                }
                if (imageBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(imageBytes).crossfade(true).build(),
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Image(painter = painterResource(R.drawable.placeholder_food), contentDescription = null, modifier = Modifier.size(80.dp))
                }
            } else {
                Image(painter = painterResource(R.drawable.placeholder_food), contentDescription = null, modifier = Modifier.size(80.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = category.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = quickBiteRed, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun MainProductCard(product: MainProductItem, onClick: () -> Unit) {
    val quickBiteRed = Color(0xFFAC0000)
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)) {
                if (product.imageBase64.isNotEmpty()) {
                    val imageBytes = remember(product.imageBase64) {
                        try { Base64.decode(product.imageBase64, Base64.DEFAULT) } catch (e: Exception) { null }
                    }
                    if (imageBytes != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(imageBytes).crossfade(true).build(),
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(text = "₱${String.format("%.2f", product.price)}", fontWeight = FontWeight.Bold, color = quickBiteRed, fontSize = 14.sp)
        }
    }
}
