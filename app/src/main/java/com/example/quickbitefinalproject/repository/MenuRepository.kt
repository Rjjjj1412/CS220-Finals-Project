package com.example.quickbitefinalproject.repository

import com.example.quickbitefinalproject.R
import com.example.quickbitefinalproject.model.MenuCategory
import com.example.quickbitefinalproject.model.MenuItem

object MenuRepository {

    private val categories = listOf(
        MenuCategory(
            id = "1",
            name = "Burgers",
            imageResId = R.drawable.placeholder_food
        ),
        MenuCategory(
            id = "2",
            name = "Drinks",
            imageResId = R.drawable.placeholder_food
        ),
        MenuCategory(
            id = "3",
            name = "Desserts",
            imageResId = R.drawable.placeholder_food
        )
    )

    private val items = listOf(
        MenuItem(
            id = "101",
            categoryId = "1",
            name = "Zinger Burger",
            description = "Crispy chicken burger",
            price = 5.99,
            imageResId = R.drawable.placeholder_food
        ),
        MenuItem(
            id = "102",
            categoryId = "1",
            name = "Beef Burger",
            description = "Juicy beef patty",
            price = 6.49,
            imageResId = R.drawable.placeholder_food
        ),
        MenuItem(
            id = "103",
            categoryId = "2",
            name = "Iced Tea",
            description = "Refreshing lemon tea",
            price = 1.99,
            imageResId = R.drawable.placeholder_food
        ),
        MenuItem(
            id = "104",
            categoryId = "3",
            name = "Ice Cream",
            description = "Vanilla cone",
            price = 0.99,
            imageResId = R.drawable.placeholder_food
        )
    )

    fun getCategories(): List<MenuCategory> = categories

    fun getItemsByCategory(categoryId: String): List<MenuItem> =
        items.filter { it.categoryId == categoryId }

    fun getItemById(id: String): MenuItem? =
        items.firstOrNull { it.id == id }
}
