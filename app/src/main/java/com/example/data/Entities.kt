package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "BANK", "EWALLET", "CASH", "SAVINGS"
    val accountNumber: String,
    val balance: Long,
    val colorHex: Long,
    val iconKey: String,
    val isMain: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: String, // "EXPENSE", "INCOME", "TRANSFER"
    val categoryId: String,
    val categoryName: String,
    val walletId: Long,
    val walletName: String,
    val targetWalletId: Long? = null,
    val targetWalletName: String? = null,
    val adminFee: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val needVsWant: String = "KEBUTUHAN" // "KEBUTUHAN", "KEINGINAN", "TABUNGAN", "PEMASUKAN", "TRANSFER"
)

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey val categoryId: String,
    val categoryName: String,
    val monthlyLimit: Long,
    val iconKey: String,
    val colorHex: Long,
    val isExpense: Boolean = true
)

@Entity(tableName = "export_reports")
data class ExportReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportTitle: String,
    val periodLabel: String,
    val walletFilterLabel: String,
    val format: String, // "PDF" or "CSV"
    val filePath: String,
    val fileSizeKb: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val transactionCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)
