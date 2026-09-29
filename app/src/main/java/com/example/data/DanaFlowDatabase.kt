package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        CategoryBudgetEntity::class,
        ExportReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DanaFlowDatabase : RoomDatabase() {
    abstract fun danaFlowDao(): DanaFlowDao

    companion object {
        @Volatile
        private var INSTANCE: DanaFlowDatabase? = null

        fun getInstance(context: Context): DanaFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DanaFlowDatabase::class.java,
                    "danaflow_offline.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
