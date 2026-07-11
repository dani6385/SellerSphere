package com.example.data.repository

import com.example.data.dao.ProductDao
import com.example.data.dao.TargetDao
import com.example.data.dao.TransactionDao
import com.example.data.model.Product
import com.example.data.model.SaleItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SalesTarget
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val targetDao: TargetDao
) {
    // Product operations
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)
    suspend fun getProductBySku(sku: String): Product? = productDao.getProductBySku(sku)

    // Transaction operations
    val allTransactions: Flow<List<SaleTransaction>> = transactionDao.getAllTransactions()
    val allSaleItems: Flow<List<SaleItem>> = transactionDao.getAllSaleItems()

    suspend fun insertTransaction(transaction: SaleTransaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun insertSaleItem(item: SaleItem) =
        transactionDao.insertSaleItem(item)

    fun getSaleItemsForTransaction(transactionId: Int): Flow<List<SaleItem>> =
        transactionDao.getSaleItemsForTransaction(transactionId)

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<SaleTransaction>> =
        transactionDao.getTransactionsBetween(startTime, endTime)

    // Target operations
    fun getTargetForDate(dateString: String): Flow<SalesTarget?> =
        targetDao.getTargetForDate(dateString)

    suspend fun insertTarget(target: SalesTarget) =
        targetDao.insertTarget(target)

    val allTargets: Flow<List<SalesTarget>> = targetDao.getAllTargets()
}
