package com.example.quickbitefinalproject.ui.kiosk

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R
import com.example.quickbitefinalproject.model.MenuItem
import com.example.quickbitefinalproject.repository.MenuRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    categoryId: String
) {
    val quickBiteRed = Color(0xFFAC0000)

    // Get chosen category + items from repository
    val category = remember {
        MenuRepository.getCategories().firstOrNull { it.id == categoryId }
    }
    val items = remember {
        MenuRepository.getItemsByCategory(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category?.name ?: "Menu",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = quickBiteRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MenuItemCard(
                    item = item,
                    onAddToCart = {
                        // For now just go to cart.
                        // Later you can hook this into a real CartRepository / ViewModel.
                        navController.navigate("kiosk_cart")
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    onAddToCart: () -> Unit
) {
    val quickBiteRed = Color(0xFFAC0000)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8D9)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = item.name,
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = Color(0xFF646464),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₱${"%.2f".format(item.price)}",
                    fontWeight = FontWeight.SemiBold,
                    color = quickBiteRed
                )
                Text(
                    text = "Add",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = quickBiteRed,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onAddToCart() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
