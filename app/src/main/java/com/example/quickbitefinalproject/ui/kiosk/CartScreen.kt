package com.example.quickbitefinalproject.ui.kiosk

import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quickbitefinalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Model for items in the cart
data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    var quantity: Long = 0,
    val imageBase64: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val scope = rememberCoroutineScope()
    val quickBiteRed = Color(0xFFAC0000)

    // --- STATE VARIABLES ---
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // View State: false = Cart View, true = Checkout View
    var isCheckoutView by remember { mutableStateOf(false) }

    // Checkout Form States
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paymentExpanded by remember { mutableStateOf(false) }
    val paymentOptions = listOf("Cash on Delivery", "G-Cash", "Credit/Debit Card")
    var selectedPaymentMethod by remember { mutableStateOf(paymentOptions[0]) }

    // Dialog States
    var showConfirmOrderDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var newOrderId by remember { mutableStateOf<String?>(null) }

    // Price Calculation
    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val deliveryFee = 5.00
    val total = subtotal + deliveryFee

    // --- BACK HANDLER ---
    // If in checkout view, go back to cart view instead of exiting
    BackHandler(enabled = isCheckoutView) {
        isCheckoutView = false
    }

    // --- FETCH DATA ---
    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        // 1. Listen to Cart Items
        val cartRef = db.collection("users").document(userId).collection("cart")
        cartRef.addSnapshotListener { snapshot, error ->
            if (error != null) { isLoading = false; return@addSnapshotListener }
            if (snapshot != null) {
                cartItems = snapshot.documents.mapNotNull { doc -> doc.toObject(CartItem::class.java) }
                isLoading = false
            }
        }

        // 2. Pre-fill User Data for Checkout
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                name = userDoc.getString("fullName") ?: userDoc.getString("firstName") ?: ""
                email = userDoc.getString("email") ?: auth.currentUser?.email ?: ""
                phone = userDoc.getString("phone") ?: ""
                address = userDoc.getString("address") ?: ""
                val savedPayment = userDoc.getString("paymentMethod")
                if (savedPayment != null && savedPayment in paymentOptions) {
                    selectedPaymentMethod = savedPayment
                }
            }
        } catch (e: Exception) {
            // Ignore pre-fill errors
        }
    }

    // --- HELPER FUNCTIONS ---
    fun updateCartItemQuantity(productId: String, newQuantity: Long) {
        if (userId == null) return
        val itemRef = db.collection("users").document(userId).collection("cart").document(productId)
        if (newQuantity > 0) {
            itemRef.update("quantity", newQuantity)
        } else {
            itemRef.delete()
        }
    }

    fun placeOrder() {
        if (userId == null) return
        scope.launch {
            try {
                val order = hashMapOf(
                    "userId" to userId,
                    "userName" to name,
                    "userEmail" to email,
                    "userPhone" to phone,
                    "userAddress" to address,
                    "paymentMethod" to selectedPaymentMethod,
                    "items" to cartItems.map {
                        mapOf(
                            "productId" to it.productId,
                            "name" to it.name,
                            "price" to it.price,
                            "quantity" to it.quantity
                        )
                    },
                    "subtotal" to subtotal,
                    "deliveryFee" to deliveryFee,
                    "total" to total,
                    "order_status" to "Pending", // Initial status
                    "timestamp" to FieldValue.serverTimestamp()
                )

                // 1. Create Order
                val newOrderRef = db.collection("orders").add(order).await()
                newOrderId = newOrderRef.id

                // 2. Clear Cart
                val batch = db.batch()
                cartItems.forEach { item ->
                    val docRef = db.collection("users").document(userId).collection("cart").document(item.productId)
                    batch.delete(docRef)
                }
                batch.commit().await()

                // 3. Show Success
                showSuccessDialog = true

            } catch (e: Exception) {
                showErrorDialog = "Failed to place order: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isCheckoutView) "Checkout" else "My Cart",
                        fontWeight = FontWeight.Bold,
                        color = if(isCheckoutView) Color(0xFFAC0000) else quickBiteRed
                    )
                },
                navigationIcon = {
                    if (isCheckoutView) {
                        IconButton(onClick = { isCheckoutView = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isCheckoutView) Color.White else Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = quickBiteRed)
            } else {

                // --- VIEW SWITCHING ---
                if (isCheckoutView) {
                    // ===========================
                    // VIEW 2: CHECKOUT FORM
                    // ===========================
                    CheckoutView(
                        name = name, onNameChange = { name = it },
                        email = email, onEmailChange = { email = it },
                        phone = phone, onPhoneChange = { phone = it },
                        address = address, onAddressChange = { address = it },
                        paymentOptions = paymentOptions,
                        selectedPaymentMethod = selectedPaymentMethod,
                        onPaymentMethodChange = { selectedPaymentMethod = it },
                        paymentExpanded = paymentExpanded,
                        onPaymentExpandedChange = { paymentExpanded = it },
                        cartItems = cartItems,
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        total = total,
                        onPlaceOrderClick = {
                            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                                showErrorDialog = "Please fill in all shipping details."
                            } else {
                                showConfirmOrderDialog = true
                            }
                        },
                        quickBiteRed = quickBiteRed
                    )
                } else {
                    // ===========================
                    // VIEW 1: CART LIST
                    // ===========================
                    if (cartItems.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Your cart is empty.", color = Color.Gray)
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(cartItems, key = { it.productId }) { item ->
                                    CartItemCard(
                                        item = item,
                                        onQuantityChange = { new -> updateCartItemQuantity(item.productId, new) }
                                    )
                                }
                            }

                            // Cart Summary & Proceed Button
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(16.dp)
                                    .padding(bottom = 100.dp) // Space for Navbar
                            ) {
                                PriceSummaryRow("Subtotal", subtotal)
                                PriceSummaryRow("Delivery Fee", deliveryFee)
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                PriceSummaryRow("Total", total, isTotal = true)

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { isCheckoutView = true }, // Switch to View 2
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- FLOATING NAVBAR (Only show in Cart View) ---
            if (!isCheckoutView) {
                FloatingBottomNavbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    selectedItem = 1,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate("main_menu")
                            1 -> { /* Already here */ }
                            2 -> navController.navigate("order_details/{orderId}")
                            3 -> navController.navigate("user_profile")
                        }
                    }
                )
            }
        }

        // --- DIALOGS ---
        if (showConfirmOrderDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmOrderDialog = false },
                containerColor = Color.White,
                title = { Text("Confirm Order", fontWeight = FontWeight.Bold, color = Color.Black) },
                text = { Text("Are you sure you want to place this order?", color = Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmOrderDialog = false
                            placeOrder()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed)
                    ) { Text("Yes, Place Order", color = Color.White, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmOrderDialog = false }) { Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }
            )
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismiss */ },
                containerColor = Color.White,
                title = { Text("Order Placed!", fontWeight = FontWeight.Bold, color = Color.Black) },
                text = { Text("Your order has been successfully placed.", color = Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            navController.navigate("order_details/${newOrderId}") {
                                popUpTo("main_menu")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed)
                    ) { Text("Track My Order", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            )
        }

        if (showErrorDialog != null) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = null },
                containerColor = Color.White,
                title = { Text("Error", fontWeight = FontWeight.Bold, color = Color.Black) },
                text = { Text(showErrorDialog!!, color = Color.Black) },
                confirmButton = {
                    Button(onClick = { showErrorDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed)) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// --- SUB-COMPOSABLE ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutView(
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    paymentOptions: List<String>,
    selectedPaymentMethod: String, onPaymentMethodChange: (String) -> Unit,
    paymentExpanded: Boolean, onPaymentExpandedChange: (Boolean) -> Unit,
    cartItems: List<CartItem>,
    subtotal: Double,
    deliveryFee: Double,
    total: Double,
    onPlaceOrderClick: () -> Unit,
    quickBiteRed: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Shipping Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = quickBiteRed)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = quickBiteRed, cursorColor = quickBiteRed, focusedLabelColor = quickBiteRed)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = quickBiteRed, cursorColor = quickBiteRed, focusedLabelColor = quickBiteRed)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone, onValueChange = onPhoneChange,
            label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = quickBiteRed, cursorColor = quickBiteRed, focusedLabelColor = quickBiteRed)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address, onValueChange = onAddressChange,
            label = { Text("Shipping Address") }, modifier = Modifier.fillMaxWidth(), maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = quickBiteRed, cursorColor = quickBiteRed, focusedLabelColor = quickBiteRed)
        )

        Spacer(Modifier.height(20.dp))
        Text("Payment Method", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = quickBiteRed)
        Spacer(Modifier.height(10.dp))

        ExposedDropdownMenuBox(
            expanded = paymentExpanded,
            onExpandedChange = onPaymentExpandedChange,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedPaymentMethod, onValueChange = {}, readOnly = true,
                label = { Text("Payment Method") },
                trailingIcon = { Icon(if (paymentExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = quickBiteRed, cursorColor = quickBiteRed, focusedLabelColor = quickBiteRed)
            )
            ExposedDropdownMenu(
                expanded = paymentExpanded,
                onDismissRequest = { onPaymentExpandedChange(false) },
                modifier = Modifier.background(Color.White)
            ) {
                paymentOptions.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method) },
                        onClick = { onPaymentMethodChange(method); onPaymentExpandedChange(false) }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        Text("Order Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = quickBiteRed)
        Spacer(Modifier.height(10.dp))

        cartItems.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${item.quantity}x ${item.name}", fontSize = 14.sp, color = Color.Black)
                Text("₱${String.format("%.2f", item.price * item.quantity)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))
        PriceSummaryRow("Subtotal", subtotal)
        PriceSummaryRow("Delivery Fee", deliveryFee)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        PriceSummaryRow("Total", total, isTotal = true)

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = onPlaceOrderClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Place Order", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(50.dp))
    }
}

@Composable
fun CartItemCard(item: CartItem, onQuantityChange: (Long) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageBytes = remember(item.imageBase64) {
                try { Base64.decode(item.imageBase64, Base64.DEFAULT) } catch (e: Exception) { null }
            }
            AsyncImage(
                model = ImageRequest.Builder(context).data(imageBytes).build(),
                contentDescription = item.name,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("₱${String.format("%.2f", item.price)}", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Smaller button size
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Text(text = "—", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                    Text(
                        text = item.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    // Smaller button size
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Default.Add, "Increase", tint = Color(0xFFAC0000), modifier = Modifier.size(20.dp))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = { onQuantityChange(0) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = "Remove", tint = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "₱${String.format("%.2f", item.price * item.quantity)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFAC0000),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun PriceSummaryRow(label: String, value: Double, isTotal: Boolean = false) {
    val quickBiteRed = Color(0xFFAC0000)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 18.sp else 16.sp,
            color = Color.Black
        )
        Text(
            text = "₱${String.format("%.2f", value)}",
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 18.sp else 16.sp,
            color = if (isTotal) quickBiteRed else Color.Black
        )
    }
}
