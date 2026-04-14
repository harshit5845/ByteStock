package com.inventory.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billNumber: String,
    val subtotal: Double,
    val totalCgst: Double,
    val totalSgst: Double,
    val discount: Double = 0.0,
    val grandTotal: Double,
    val paymentMode: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class BillWithItems(
    val bill: Bill,
    val items: List<BillItem>
)
