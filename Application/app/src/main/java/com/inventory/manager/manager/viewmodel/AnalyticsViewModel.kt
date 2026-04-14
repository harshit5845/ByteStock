package com.inventory.manager.viewmodel

import androidx.lifecycle.*
import com.inventory.manager.data.model.AnalyticsSummary
import com.inventory.manager.data.repository.InventoryRepository
import com.inventory.manager.data.repository.SalesRepository
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val salesRepo: SalesRepository,
    private val inventoryRepo: InventoryRepository
) : ViewModel() {

    private val _windowDays = MutableLiveData(30)
    val windowDays: LiveData<Int> = _windowDays

    private val _summary = MutableLiveData<AnalyticsSummary?>()
    val summary: LiveData<AnalyticsSummary?> = _summary

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init { refresh() }

    fun setWindow(days: Int) { _windowDays.value = days; refresh() }

    fun refresh() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val stockMap = inventoryRepo.getAllItemsOnce().associate { it.id to it.stock }
            _summary.value = salesRepo.getAnalyticsSummary(_windowDays.value ?: 30, stockMap)
        } finally {
            _isLoading.value = false
        }
    }

    class Factory(
        private val salesRepo: SalesRepository,
        private val inventoryRepo: InventoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            AnalyticsViewModel(salesRepo, inventoryRepo) as T
    }
}
