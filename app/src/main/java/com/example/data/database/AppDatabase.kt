package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ProductDao
import com.example.data.dao.TargetDao
import com.example.data.dao.TransactionDao
import com.example.data.model.Product
import com.example.data.model.SaleItem
import com.example.data.model.SaleTransaction
import com.example.data.model.SalesTarget

@Database(
    entities = [
        Product::class,
        SaleTransaction::class,
        SaleItem::class,
        SalesTarget::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun targetDao(): TargetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sellersphere_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
