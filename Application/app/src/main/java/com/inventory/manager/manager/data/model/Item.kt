package com.inventory.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val description: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stock: Int = 0,
    val unit: String = "pcs",
    val gstRate: Double = 18.0,
    val hsnCode: String = "",
    val barcode: String = "",
    val lowStockThreshold: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean get() = stock in 1..lowStockThreshold
    val isOutOfStock: Boolean get() = stock == 0
    val priceWithGst: Double get() = sellingPrice * (1 + gstRate / 100)
    val cgstRate: Double get() = gstRate / 2
    val sgstRate: Double get() = gstRate / 2
}
