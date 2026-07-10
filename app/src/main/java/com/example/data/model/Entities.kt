package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String = "",
    val stock: Int = 0,
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val category: String = "Umum",
    val minStockThreshold: Int = 5,
    val imageUrls: String = ""
) {
    val isLowStock: Boolean get() = stock <= minStockThreshold
    val profitPerUnit: Double get() = sellingPrice - purchasePrice
}

@Entity(tableName = "sale_transactions")
data class SaleTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val totalProfit: Double = 0.0,
    val paymentMethod: String = "Tunai"
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val purchasePrice: Double,
    val sellingPrice: Double
) {
    val totalAmount: Double get() = sellingPrice * quantity
    val totalProfit: Double get() = (sellingPrice - purchasePrice) * quantity
}

@Entity(tableName = "sales_targets")
data class SalesTarget(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val targetAmount: Double = 0.0
)
