package com.example.quickbitefinalproject.model

data class ProductItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val stock: Int = 0,
    val isAvailable: Boolean = true
)
