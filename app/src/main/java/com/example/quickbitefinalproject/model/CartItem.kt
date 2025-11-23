package com.example.quickbitefinalproject.model

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val totalPrice: Double get() = menuItem.price * quantity
}
