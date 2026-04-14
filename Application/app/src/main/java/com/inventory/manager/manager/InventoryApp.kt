package com.inventory.manager

import android.app.Application
import com.inventory.manager.data.database.AppDatabase
import com.inventory.manager.data.repository.BillRepository
import com.inventory.manager.data.repository.InventoryRepository
import com.inventory.manager.data.repository.SalesRepository

class InventoryApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val inventoryRepository by lazy { InventoryRepository(database.itemDao()) }
    val billRepository by lazy { BillRepository(database.billDao()) }
    val salesRepository by lazy { SalesRepository(database.salesDao()) }
}
