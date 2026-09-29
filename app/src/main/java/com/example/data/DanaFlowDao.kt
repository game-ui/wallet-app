package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DanaFlowDao {

    // Wallets
    @Query("SELECT * FROM wallets ORDER BY isMain DESC, id ASC")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :walletId LIMIT 1")
    suspend fun getWalletById(walletId: Long): WalletEntity?

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>): List<Long>

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWalletById(walletId: Long)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :txId LIMIT 1")
    suspend fun getTransactionById(txId: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionRaw(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionsRaw(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :txId")
    suspend fun deleteTransactionRaw(txId: Long)

    @Query("DELETE FROM transactions WHERE walletId = :walletId OR targetWalletId = :walletId")
    suspend fun deleteTransactionsForWallet(walletId: Long)

    // Category Budgets
    @Query("SELECT * FROM category_budgets ORDER BY isExpense DESC, monthlyLimit DESC")
    fun getAllCategoryBudgets(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudget(budget: CategoryBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudgets(budgets: List<CategoryBudgetEntity>)

    // Export Reports
    @Query("SELECT * FROM export_reports ORDER BY createdAt DESC")
    fun getAllExportReports(): Flow<List<ExportReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExportReport(report: ExportReportEntity): Long

    @Query("DELETE FROM export_reports WHERE id = :reportId")
    suspend fun deleteExportReportById(reportId: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("DELETE FROM wallets")
    suspend fun clearAllWallets()

    @Query("DELETE FROM category_budgets")
    suspend fun clearAllBudgets()

    // Atomic Transaction Recording with Wallet Balance Sync
    @Transaction
    suspend fun recordTransactionAndSyncWallets(tx: TransactionEntity): Long {
        val sourceWallet = getWalletById(tx.walletId)
        if (sourceWallet != null) {
            val delta = when (tx.type) {
                "INCOME" -> tx.amount
                "EXPENSE" -> -tx.amount
                "TRANSFER" -> -(tx.amount + tx.adminFee)
                else -> 0L
            }
            updateWallet(sourceWallet.copy(balance = sourceWallet.balance + delta))
        }

        if (tx.type == "TRANSFER" && tx.targetWalletId != null) {
            val targetWallet = getWalletById(tx.targetWalletId)
            if (targetWallet != null) {
                updateWallet(targetWallet.copy(balance = targetWallet.balance + tx.amount))
            }
        }
        return insertTransactionRaw(tx)
    }

    @Transaction
    suspend fun deleteTransactionAndRestoreWallets(txId: Long) {
        val existing = getTransactionById(txId) ?: return
        val sourceWallet = getWalletById(existing.walletId)
        if (sourceWallet != null) {
            val reverseDelta = when (existing.type) {
                "INCOME" -> -existing.amount
                "EXPENSE" -> existing.amount
                "TRANSFER" -> existing.amount + existing.adminFee
                else -> 0L
            }
            updateWallet(sourceWallet.copy(balance = sourceWallet.balance + reverseDelta))
        }

        if (existing.type == "TRANSFER" && existing.targetWalletId != null) {
            val targetWallet = getWalletById(existing.targetWalletId)
            if (targetWallet != null) {
                updateWallet(targetWallet.copy(balance = targetWallet.balance - existing.amount))
            }
        }
        deleteTransactionRaw(txId)
    }

    @Transaction
    suspend fun updateTransactionAndSyncWallets(updated: TransactionEntity) {
        deleteTransactionAndRestoreWallets(updated.id)
        recordTransactionAndSyncWallets(updated)
    }
}
