package com.example.quickbitefinalproject.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.quickbitefinalproject.model.CartItem
import com.example.quickbitefinalproject.model.MenuItem

object CartRepository {

    // Backing state list for Compose
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    fun addToCart(item: MenuItem) {
        val index = _cartItems.indexOfFirst { it.menuItem.id == item.id }
        if (index >= 0) {
            val existing = _cartItems[index]
            _cartItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            _cartItems.add(CartItem(menuItem = item, quantity = 1))
        }
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        val index = _cartItems.indexOfFirst { it.menuItem.id == itemId }
        if (index >= 0) {
            if (newQuantity <= 0) {
                _cartItems.removeAt(index)
            } else {
                val existing = _cartItems[index]
                _cartItems[index] = existing.copy(quantity = newQuantity)
            }
        }
    }

    fun removeItem(itemId: String) {
        _cartItems.removeAll { it.menuItem.id == itemId }
    }

    fun clearCart() {
        _cartItems.clear()
    }

    fun getCartTotal(): Double =
        _cartItems.sumOf { it.totalPrice }
}
