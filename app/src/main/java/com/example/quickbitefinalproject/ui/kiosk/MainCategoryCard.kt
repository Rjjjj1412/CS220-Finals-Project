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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickbitefinalproject.R
import com.example.quickbitefinalproject.model.MenuCategory
import com.example.quickbitefinalproject.repository.MenuRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavController) {
    val quickBiteRed = Color(0xFFAC0000)
    val categories = remember { MenuRepository.getCategories() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QuickBite Kiosk",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = { navController.navigate("admin_login") }) {
                        Text(
                            text = "Admin",
                            color = quickBiteRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Logo + subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    MainCategoryCard(
                        category = category,
                        onClick = { navController.navigate("kiosk_menu/${category.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainCategoryCard(
    category: MenuCategory,
    onClick: () -> Unit
) {
    val quickBiteRed = Color(0xFFAC0000)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3EA)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Image(
                painter = painterResource(id = category.imageResId),
                contentDescription = category.name,
                modifier = Modifier.size(70.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = category.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = quickBiteRed
            )
        }
    }
}
