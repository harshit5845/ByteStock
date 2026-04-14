package com.inventory.manager.data.repository

import com.inventory.manager.analytics.PredictionEngine
import com.inventory.manager.data.dao.SalesDao
import com.inventory.manager.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SalesRepository(private val salesDao: SalesDao) {

    suspend fun recordBillSales(billId: Long, items: List<BillItem>, timestamp: Long) {
        withContext(Dispatchers.IO) {
            val records = items.map { bi ->
                val exGst = bi.sellingPrice * bi.quantity
                val incGst = exGst * (1 + bi.gstRate / 100)
                SalesRecord(
                    itemId = bi.itemId,
                    itemName = bi.itemName,
                    category = bi.category,
                    quantitySold = bi.quantity,
                    sellingPricePerUnit = bi.sellingPrice,
                    gstRate = bi.gstRate,
                    revenueExGst = exGst,
                    revenueIncGst = incGst,
                    saleTimestamp = timestamp,
                    billId = billId
                )
            }
            salesDao.insertSalesRecords(records)
        }
    }

    suspend fun getAnalyticsSummary(windowDays: Int = 30, currentStock: Map<Long, Int> = emptyMap()): AnalyticsSummary =
        withContext(Dispatchers.Default) {
            val fromTs = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(windowDays.toLong())
            val records = salesDao.getSalesFrom(fromTs)
            PredictionEngine.buildSummary(records, windowDays, currentStock)
        }

    suspend fun clearAll() = withContext(Dispatchers.IO) { salesDao.deleteAll() }
}
