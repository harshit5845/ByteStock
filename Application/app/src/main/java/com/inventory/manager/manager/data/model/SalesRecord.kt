package com.inventory.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_records")
data class SalesRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val itemName: String,
    val category: String,
    val quantitySold: Int,
    val sellingPricePerUnit: Double,
    val gstRate: Double,
    val revenueExGst: Double,
    val revenueIncGst: Double,
    val saleTimestamp: Long = System.currentTimeMillis(),
    val billId: Long = 0
)
