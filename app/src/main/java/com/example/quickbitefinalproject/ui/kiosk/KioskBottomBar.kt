package com.example.quickbitefinalproject.ui.kiosk

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingBottomNavbar(
    modifier: Modifier = Modifier,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val quickBiteRed = Color(0xFFAC0000)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp) // You might want to increase this slightly (e.g., 75.dp) if it feels cramped
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(35.dp)),
        shape = RoundedCornerShape(35.dp),
        colors = CardDefaults.cardColors(containerColor = quickBiteRed)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavbarItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedItem == 0,
                onClick = { onItemSelected(0) }
            )
            NavbarItem(
                icon = Icons.Default.ShoppingCart,
                label = "Cart",
                isSelected = selectedItem == 1,
                onClick = { onItemSelected(1) }
            )
            NavbarItem(
                icon = Icons.Default.List,
                label = "Orders",
                isSelected = selectedItem == 2,
                onClick = { onItemSelected(2) }
            )
            NavbarItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = selectedItem == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}

@Composable
fun NavbarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Selected item is White, Unselected is faint beige
    val contentColor = if (isSelected) Color.White else Color(0xFFFFEEDA).copy(alpha = 0.7f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp) // Adjusted padding
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        // Removed "if (isSelected)" so text always shows
        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
