package com.example.quickbitefinalproject.model

data class Order(
    val id: Long,
    val items: List<CartItem>,
    val totalPrice: Double,
    val timestamp: Long
)
