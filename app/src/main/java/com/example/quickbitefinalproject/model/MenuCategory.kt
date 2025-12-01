package com.example.quickbitefinalproject.model

import androidx.annotation.DrawableRes

data class MenuCategory(
    val id: String,
    val name: String,
    @DrawableRes val imageResId: Int
)
