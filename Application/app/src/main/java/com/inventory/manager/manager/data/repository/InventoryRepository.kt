package com.inventory.manager.data.repository

import androidx.lifecycle.LiveData
import com.inventory.manager.data.dao.ItemDao
import com.inventory.manager.data.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(private val itemDao: ItemDao) {

    val allItems: LiveData<List<Item>> = itemDao.getAllItems()
    val lowStockItems: LiveData<List<Item>> = itemDao.getLowStockItems()
    val totalProductCount: LiveData<Int> = itemDao.getTotalProductCount()
    val lowStockCount: LiveData<Int> = itemDao.getLowStockCount()
    val totalInventoryValue: LiveData<Double> = itemDao.getTotalInventoryValue()
    val allCategories: LiveData<List<String>> = itemDao.getAllCategories()

    suspend fun insert(item: Item): Long = withContext(Dispatchers.IO) { itemDao.insert(item) }
    suspend fun update(item: Item) = withContext(Dispatchers.IO) { itemDao.update(item) }
    suspend fun delete(item: Item) = withContext(Dispatchers.IO) { itemDao.delete(item) }
    suspend fun getItemById(id: Long) = withContext(Dispatchers.IO) { itemDao.getItemById(id) }
    suspend fun getAllItemsOnce() = withContext(Dispatchers.IO) { itemDao.getAllItemsOnce() }
    suspend fun updateStock(itemId: Long, delta: Int) = withContext(Dispatchers.IO) { itemDao.updateStock(itemId, delta) }

    fun searchItems(query: String): LiveData<List<Item>> = itemDao.searchItems(query)
    suspend fun searchItemsOnce(query: String) = withContext(Dispatchers.IO) { itemDao.searchItemsOnce(query) }
}
