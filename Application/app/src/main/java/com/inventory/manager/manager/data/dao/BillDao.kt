package com.inventory.manager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.inventory.manager.data.model.Bill
import com.inventory.manager.data.model.BillItem

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItem>)

    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    fun getAllBills(): LiveData<List<Bill>>

    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    suspend fun getAllBillsOnce(): List<Bill>

    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun getBillItems(billId: Long): List<BillItem>

    @Query("SELECT COUNT(*) FROM bills")
    fun getTotalBillCount(): LiveData<Int>

    @Query("SELECT * FROM bills ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentBills(limit: Int = 5): LiveData<List<Bill>>

    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBill(billId: Long)

    @Query("DELETE FROM bill_items WHERE billId = :billId")
    suspend fun deleteBillItems(billId: Long)
}
