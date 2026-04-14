package com.inventory.manager.data.dao

import androidx.room.*
import com.inventory.manager.data.model.SalesRecord

@Dao
interface SalesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesRecords(records: List<SalesRecord>)

    @Query("SELECT * FROM sales_records ORDER BY saleTimestamp DESC")
    suspend fun getAllSalesRecordsOnce(): List<SalesRecord>

    @Query("SELECT * FROM sales_records WHERE saleTimestamp >= :fromTimestamp ORDER BY saleTimestamp ASC")
    suspend fun getSalesFrom(fromTimestamp: Long): List<SalesRecord>

    @Query("SELECT COALESCE(SUM(revenueIncGst), 0) FROM sales_records WHERE saleTimestamp >= :fromTimestamp")
    suspend fun getTotalRevenue(fromTimestamp: Long): Double

    @Query("SELECT COALESCE(SUM(quantitySold), 0) FROM sales_records WHERE saleTimestamp >= :fromTimestamp")
    suspend fun getTotalUnitsSold(fromTimestamp: Long): Int

    @Query("SELECT COUNT(DISTINCT billId) FROM sales_records WHERE saleTimestamp >= :fromTimestamp")
    suspend fun getDistinctBillCount(fromTimestamp: Long): Int

    @Query("DELETE FROM sales_records")
    suspend fun deleteAll()
}
