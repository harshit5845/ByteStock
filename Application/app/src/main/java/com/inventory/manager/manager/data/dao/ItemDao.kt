package com.inventory.manager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.inventory.manager.data.model.Item

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): LiveData<List<Item>>

    @Query("SELECT * FROM items ORDER BY name ASC")
    suspend fun getAllItemsOnce(): List<Item>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): Item?

    @Query("SELECT * FROM items WHERE stock <= lowStockThreshold AND stock > 0 ORDER BY stock ASC")
    fun getLowStockItems(): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE stock = 0")
    fun getOutOfStockItems(): LiveData<List<Item>>

    @Query("SELECT COUNT(*) FROM items")
    fun getTotalProductCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM items WHERE stock <= lowStockThreshold AND stock > 0")
    fun getLowStockCount(): LiveData<Int>

    @Query("SELECT COALESCE(SUM(stock * sellingPrice), 0.0) FROM items")
    fun getTotalInventoryValue(): LiveData<Double>

    @Query("UPDATE items SET stock = stock + :delta WHERE id = :itemId")
    suspend fun updateStock(itemId: Long, delta: Int)

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItems(query: String): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchItemsOnce(query: String): List<Item>

    @Query("SELECT DISTINCT category FROM items ORDER BY category ASC")
    fun getAllCategories(): LiveData<List<String>>
}
