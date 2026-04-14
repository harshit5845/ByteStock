package com.inventory.manager.data.repository

import androidx.lifecycle.LiveData
import com.inventory.manager.data.dao.BillDao
import com.inventory.manager.data.model.Bill
import com.inventory.manager.data.model.BillItem
import com.inventory.manager.data.model.BillWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillRepository(private val billDao: BillDao) {

    val allBills: LiveData<List<Bill>> = billDao.getAllBills()
    val totalBillCount: LiveData<Int> = billDao.getTotalBillCount()
    val recentBills: LiveData<List<Bill>> = billDao.getRecentBills(5)

    suspend fun saveBill(bill: Bill, items: List<BillItem>): Long = withContext(Dispatchers.IO) {
        val billId = billDao.insertBill(bill)
        val itemsWithBillId = items.map { it.copy(billId = billId) }
        billDao.insertBillItems(itemsWithBillId)
        billId
    }

    suspend fun getBillWithItems(billId: Long): BillWithItems? = withContext(Dispatchers.IO) {
        val bills = billDao.getAllBillsOnce()
        val bill = bills.find { it.id == billId } ?: return@withContext null
        val items = billDao.getBillItems(billId)
        BillWithItems(bill, items)
    }

    suspend fun getAllBillsWithItems(): List<BillWithItems> = withContext(Dispatchers.IO) {
        val bills = billDao.getAllBillsOnce()
        bills.map { bill ->
            BillWithItems(bill, billDao.getBillItems(bill.id))
        }
    }
}
