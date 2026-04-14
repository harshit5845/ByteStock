package com.inventory.manager.viewmodel

import androidx.lifecycle.*
import com.inventory.manager.data.model.Item
import com.inventory.manager.data.repository.InventoryRepository
import kotlinx.coroutines.launch

class InventoryViewModel(private val repo: InventoryRepository) : ViewModel() {

    val allItems: LiveData<List<Item>> = repo.allItems
    val lowStockCount: LiveData<Int> = repo.lowStockCount
    val totalProductCount: LiveData<Int> = repo.totalProductCount
    val totalInventoryValue: LiveData<Double> = repo.totalInventoryValue
    val allCategories: LiveData<List<String>> = repo.allCategories

    private val _searchQuery = MutableLiveData("")
    private val _selectedCategory = MutableLiveData<String?>(null)

    val filteredItems: LiveData<List<Item>> = MediatorLiveData<List<Item>>().apply {
        fun update() {
            val items = allItems.value ?: return
            val q = _searchQuery.value.orEmpty().lowercase()
            val cat = _selectedCategory.value
            value = items.filter { item ->
                (q.isBlank() || item.name.lowercase().contains(q) || item.category.lowercase().contains(q)) &&
                (cat == null || item.category == cat)
            }
        }
        addSource(allItems) { update() }
        addSource(_searchQuery) { update() }
        addSource(_selectedCategory) { update() }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setCategory(cat: String?) { _selectedCategory.value = cat }

    fun insert(item: Item) = viewModelScope.launch { repo.insert(item) }
    fun update(item: Item) = viewModelScope.launch { repo.update(item) }
    fun delete(item: Item) = viewModelScope.launch { repo.delete(item) }
    fun adjustStock(itemId: Long, delta: Int) = viewModelScope.launch { repo.updateStock(itemId, delta) }

    suspend fun searchItemsOnce(q: String) = repo.searchItemsOnce(q)
    suspend fun getAllItemsOnce() = repo.getAllItemsOnce()

    class Factory(private val repo: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = InventoryViewModel(repo) as T
    }
}
