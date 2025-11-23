package com.example.quickbitefinalproject.model

import androidx.annotation.DrawableRes

data class MenuItem(
    val id: String,
    val categoryId: String,        // <— IMPORTANT: categoryId, not category
    val name: String,
    val description: String,
    val price: Double,
    @DrawableRes val imageResId: Int
)
