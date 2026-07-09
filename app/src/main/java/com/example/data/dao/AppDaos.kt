package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import com.example.data.model.SaleItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SalesTarget
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE stock <= minStockThreshold ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<Product>>
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SaleTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(item: SaleItem)

    @Query("SELECT * FROM sale_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<SaleTransaction>>

    @Query("SELECT * FROM sale_items WHERE transactionId = :transactionId")
    fun getSaleItemsForTransaction(transactionId: Int): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<SaleTransaction>>
}

@Dao
interface TargetDao {
    @Query("SELECT * FROM sales_targets WHERE dateString = :dateString LIMIT 1")
    fun getTargetForDate(dateString: String): Flow<SalesTarget?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: SalesTarget)

    @Query("SELECT * FROM sales_targets")
    fun getAllTargets(): Flow<List<SalesTarget>>
}
