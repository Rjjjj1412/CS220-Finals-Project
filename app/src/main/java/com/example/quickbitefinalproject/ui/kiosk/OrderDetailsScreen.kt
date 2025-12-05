package com.example.quickbitefinalproject.ui.kiosk

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// --- Models ---
data class Order(
    var orderId: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("total") @set:PropertyName("total") var total: Double = 0.0,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "",
    @get:PropertyName("order_status") @set:PropertyName("order_status") var order_status: String = "Pending",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Any? = null,
    @get:PropertyName("items") @set:PropertyName("items") var items: List<Map<String, Any>> = emptyList(),
    @get:PropertyName("userAddress") @set:PropertyName("userAddress") var userAddress: String = "",
    @get:PropertyName("userName") @set:PropertyName("userName") var userName: String = "",
    @get:PropertyName("userPhone") @set:PropertyName("userPhone") var userPhone: String = "",
    @get:PropertyName("paymentMethod") @set:PropertyName("paymentMethod") var paymentMethod: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(navController: NavController, orderId: String? = null) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val quickBiteRed = Color(0xFFAC0000)

    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var ordersList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- Back Handler ---
    BackHandler(enabled = selectedOrder != null) {
        selectedOrder = null
    }

    // --- Fetch Data ---
    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            navController.navigate("user_auth") { popUpTo(0) }
            return@LaunchedEffect
        }

        try {
            val snapshot = db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val fetchedOrders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(orderId = doc.id)
            }
            ordersList = fetchedOrders

            if (orderId != null && selectedOrder == null) {
                selectedOrder = fetchedOrders.find { it.orderId == orderId }
            }

            isLoading = false
        } catch (e: Exception) {
            Log.e("OrderDetailsScreen", "Error fetching orders: ${e.message}", e)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedOrder != null) "Track Order" else "My Orders",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOrder != null) Color(0xFFAC0000) else quickBiteRed
                    )
                },
                navigationIcon = {
                    if (selectedOrder != null) {
                        IconButton(onClick = { selectedOrder = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to My Orders")
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
                .background(if (selectedOrder != null) Color.White else Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = quickBiteRed)
            } else {
                if (selectedOrder != null) {
                    // --- VIEW 2: TRACKING DETAILS ---
                    TrackingView(order = selectedOrder!!, quickBiteRed = quickBiteRed)
                } else {
                    // --- VIEW 1: ORDER LIST ---
                    if (ordersList.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("No orders yet.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ordersList) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = { selectedOrder = order },
                                    quickBiteRed = quickBiteRed
                                )
                            }
                        }
                    }
                }
            }

            // --- Floating Nav Bar ---
            if (selectedOrder == null) {
                FloatingBottomNavbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    selectedItem = 2, // "Orders" is at index 2
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate("main_menu")
                            1 -> navController.navigate("kiosk_cart")
                            2 -> { /* Already on this screen */ }
                            3 -> navController.navigate("user_profile")
                        }
                    }
                )
            }
        }
    }
}

// --- SUB-COMPOSABLES for ORDER LIST ---

@Composable
fun OrderCard(order: Order, onClick: () -> Unit, quickBiteRed: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order #${order.orderId.take(6).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "₱${String.format("%.2f", order.total)}",
                    fontWeight = FontWeight.Bold,
                    color = quickBiteRed,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val itemNames = order.items.joinToString(", ") { "${it["quantity"]}x ${it["name"]}" }
            Text(itemNames, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if(order.order_status == "Delivered") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.order_status,
                        color = if(order.order_status == "Delivered") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = quickBiteRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Track Order", fontSize = 12.sp)
                }
            }
        }
    }
}

// --- SUB-COMPOSABLES for TRACKING VIEW ---

@Composable
fun TrackingView(order: Order, quickBiteRed: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Order ID", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color =Color(0xFFAC0000))
        Text(
            text = "#${order.orderId.uppercase()}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.LightGray)

        // --- 1. Order Status Timeline ---
        Text("Order Status", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = quickBiteRed)
        Spacer(modifier = Modifier.height(16.dp))
        TrackingStep("Pending", "Order has been placed", order.order_status, isFirst = true)
        TrackingStep("Confirmed", "The restaurant is preparing your order", order.order_status, isFirst = false)
        TrackingStep("Processing", "Your order is being packed", order.order_status, isFirst = false)
        TrackingStep("Shipped", "Rider is on the way to you", order.order_status, isFirst = false)
        TrackingStep("Delivered", "Enjoy your meal!", order.order_status, isFirst = false, isLast = true)

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color.LightGray)

        // --- 2. Shipping Info ---
        Text("Shipping Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = quickBiteRed)
        Spacer(modifier = Modifier.height(10.dp))
        InfoTextField(label = "Deliver To", value = order.userName)
        InfoTextField(label = "Address", value = order.userAddress)
        InfoTextField(label = "Phone Number", value = order.userPhone)
        InfoTextField(label = "Payment Method", value = order.paymentMethod)

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color.LightGray)

        // --- 3. Order Summary ---
        Text("Order Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = quickBiteRed)
        Spacer(modifier = Modifier.height(10.dp))
        order.items.forEach { item ->
            val price = (item["price"] as? Double) ?: 0.0
            val qty = (item["quantity"] as? Long) ?: 1L
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${qty}x ${item["name"]}", fontSize = 14.sp, color = Color.Black)
                Text("₱${String.format("%.2f", price * qty)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        PriceSummaryRow("Subtotal", order.items.sumOf { ((it["price"] as? Double) ?: 0.0) * ((it["quantity"] as? Long) ?: 1L) })
        PriceSummaryRow("Delivery Fee", order.items.sumOf { ((it["deliveryFee"] as? Double) ?: 50.0) })
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        PriceSummaryRow("Total", order.total, isTotal = true)

        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun InfoTextField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        readOnly = true,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.LightGray,
            disabledLabelColor = Color.Gray
        ),
        enabled = false
    )
}

@Composable
fun TrackingStep(stepStatus: String, description: String, currentStatus: String, isFirst: Boolean, isLast: Boolean = false) {
    val quickBiteRed = Color(0xFFAC0000)

    val statuses = listOf("Pending", "Confirmed", "Processing", "Shipped", "Delivered")
    val currentIndex = statuses.indexOf(currentStatus).coerceAtLeast(0)
    val stepIndex = statuses.indexOf(stepStatus)
    val isActive = stepIndex <= currentIndex

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            if (!isFirst) {
                Box(modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (isActive) quickBiteRed else Color.LightGray))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isActive) quickBiteRed else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }

            if (!isLast) {
                Box(modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (stepIndex < currentIndex) quickBiteRed else Color.LightGray))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(stepStatus, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if(isActive) Color.Black else Color.Gray)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
