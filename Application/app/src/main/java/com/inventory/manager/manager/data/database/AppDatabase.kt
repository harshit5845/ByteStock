package com.inventory.manager.data.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inventory.manager.data.dao.BillDao
import com.inventory.manager.data.dao.ItemDao
import com.inventory.manager.data.dao.SalesDao
import com.inventory.manager.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Item::class, Bill::class, BillItem::class, SalesRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun billDao(): BillDao
    abstract fun salesDao(): SalesDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                try { seedDatabase(context) } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private suspend fun seedDatabase(context: Context) {
            val dao = getInstance(context).itemDao()
            listOf(
                Item(name = "Basmati Rice 5kg", category = "Food & Grocery",
                    purchasePrice = 280.0, sellingPrice = 350.0, stock = 50,
                    unit = "bag", gstRate = 5.0, hsnCode = "1006", lowStockThreshold = 10),
                Item(name = "Toor Dal 1kg", category = "Food & Grocery",
                    purchasePrice = 110.0, sellingPrice = 140.0, stock = 4,
                    unit = "kg", gstRate = 5.0, hsnCode = "0713", lowStockThreshold = 5),
                Item(name = "Paracetamol 500mg Strip", category = "Medicine",
                    purchasePrice = 12.0, sellingPrice = 18.0, stock = 120,
                    unit = "strip", gstRate = 12.0, hsnCode = "3004", lowStockThreshold = 20),
                Item(name = "USB-C Cable 1m", category = "Electronics",
                    purchasePrice = 80.0, sellingPrice = 150.0, stock = 0,
                    unit = "pcs", gstRate = 18.0, hsnCode = "8544", lowStockThreshold = 5),
                Item(name = "Cotton T-Shirt (M)", category = "Clothing",
                    purchasePrice = 180.0, sellingPrice = 350.0, stock = 25,
                    unit = "pcs", gstRate = 5.0, hsnCode = "6109", lowStockThreshold = 5)
            ).forEach { dao.insert(it) }
        }
    }
}
