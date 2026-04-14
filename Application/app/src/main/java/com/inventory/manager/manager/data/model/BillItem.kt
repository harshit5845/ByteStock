package com.inventory.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long = 0,
    val itemId: Long,
    val itemName: String,
    val category: String = "",
    val quantity: Int,
    val sellingPrice: Double,
    val gstRate: Double,
    val cgst: Double = sellingPrice * quantity * (gstRate / 2) / 100,
    val sgst: Double = sellingPrice * quantity * (gstRate / 2) / 100,
    val lineTotal: Double = sellingPrice * quantity + cgst + sgst
)
