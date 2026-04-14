package com.inventory.manager.viewmodel

import androidx.lifecycle.*
import com.inventory.manager.data.model.Bill
import com.inventory.manager.data.model.BillItem
import com.inventory.manager.data.model.BillWithItems
import com.inventory.manager.data.repository.BillRepository
import com.inventory.manager.data.repository.InventoryRepository
import com.inventory.manager.data.repository.SalesRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BillingViewModel(
    private val billRepo: BillRepository,
    private val inventoryRepo: InventoryRepository,
    private val salesRepo: SalesRepository
) : ViewModel() {

    val allBills: LiveData<List<Bill>> = billRepo.allBills
    val recentBills: LiveData<List<Bill>> = billRepo.recentBills
    val totalBillCount: LiveData<Int> = billRepo.totalBillCount

    private val _currentBillItems = MutableLiveData<MutableList<BillItem>>(mutableListOf())
    val currentBillItems: LiveData<MutableList<BillItem>> = _currentBillItems

    private val _discount = MutableLiveData(0.0)
    val discount: LiveData<Double> = _discount

    private val _billSaved = MutableLiveData<Boolean>(false)
    val billSaved: LiveData<Boolean> = _billSaved

    val subtotal: LiveData<Double> = MediatorLiveData<Double>().apply {
        addSource(_currentBillItems) { value = it.sumOf { bi -> bi.sellingPrice * bi.quantity } }
    }
    val totalCgst: LiveData<Double> = MediatorLiveData<Double>().apply {
        addSource(_currentBillItems) { value = it.sumOf { bi -> bi.cgst } }
    }
    val totalSgst: LiveData<Double> = MediatorLiveData<Double>().apply {
        addSource(_currentBillItems) { value = it.sumOf { bi -> bi.sgst } }
    }
    val grandTotal: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun calc() {
            val sub = subtotal.value ?: 0.0
            val cgst = totalCgst.value ?: 0.0
            val sgst = totalSgst.value ?: 0.0
            val disc = _discount.value ?: 0.0
            value = sub + cgst + sgst - disc
        }
        addSource(subtotal) { calc() }
        addSource(totalCgst) { calc() }
        addSource(totalSgst) { calc() }
        addSource(_discount) { calc() }
    }

    fun addItem(item: com.inventory.manager.data.model.Item, quantity: Int = 1) {
        val list = _currentBillItems.value ?: mutableListOf()
        val existing = list.indexOfFirst { it.itemId == item.id }
        if (existing >= 0) {
            val old = list[existing]
            val newQty = old.quantity + quantity
            list[existing] = makeBillItem(item, newQty)
        } else {
            list.add(makeBillItem(item, quantity))
        }
        _currentBillItems.value = list
    }

    fun updateQuantity(index: Int, qty: Int) {
        val list = _currentBillItems.value ?: return
        if (qty <= 0) list.removeAt(index) else {
            val old = list[index]
            list[index] = makeBillItem2(old, qty)
        }
        _currentBillItems.value = list
    }

    fun removeItem(index: Int) {
        val list = _currentBillItems.value ?: return
        list.removeAt(index)
        _currentBillItems.value = list
    }

    fun setDiscount(d: Double) { _discount.value = d }

    fun clearBill() {
        _currentBillItems.value = mutableListOf()
        _discount.value = 0.0
        _billSaved.value = false
    }

    fun saveBill(paymentMode: String) = viewModelScope.launch {
        val items = _currentBillItems.value ?: return@launch
        if (items.isEmpty()) return@launch
        val now = System.currentTimeMillis()
        val billNumber = "INV-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date(now))}"
        val bill = Bill(
            billNumber = billNumber,
            subtotal = subtotal.value ?: 0.0,
            totalCgst = totalCgst.value ?: 0.0,
            totalSgst = totalSgst.value ?: 0.0,
            discount = _discount.value ?: 0.0,
            grandTotal = grandTotal.value ?: 0.0,
            paymentMode = paymentMode,
            createdAt = now
        )
        val billId = billRepo.saveBill(bill, items)
        // Deduct stock
        items.forEach { bi -> inventoryRepo.updateStock(bi.itemId, -bi.quantity) }
        // Record sales for analytics
        salesRepo.recordBillSales(billId, items, now)
        _billSaved.value = true
        clearBill()
    }

    suspend fun getBillWithItems(billId: Long): BillWithItems? = billRepo.getBillWithItems(billId)

    private fun makeBillItem(item: com.inventory.manager.data.model.Item, qty: Int): BillItem {
        val cgst = item.sellingPrice * qty * (item.gstRate / 2) / 100
        val sgst = cgst
        return BillItem(itemId = item.id, itemName = item.name, category = item.category,
            quantity = qty, sellingPrice = item.sellingPrice, gstRate = item.gstRate,
            cgst = cgst, sgst = sgst, lineTotal = item.sellingPrice * qty + cgst + sgst)
    }

    private fun makeBillItem2(old: BillItem, qty: Int): BillItem {
        val cgst = old.sellingPrice * qty * (old.gstRate / 2) / 100
        val sgst = cgst
        return old.copy(quantity = qty, cgst = cgst, sgst = sgst,
            lineTotal = old.sellingPrice * qty + cgst + sgst)
    }

    class Factory(
        private val billRepo: BillRepository,
        private val inventoryRepo: InventoryRepository,
        private val salesRepo: SalesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            BillingViewModel(billRepo, inventoryRepo, salesRepo) as T
    }
}
