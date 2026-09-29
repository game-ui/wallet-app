package com.example.data

import kotlinx.coroutines.flow.Flow

class DanaFlowRepository(private val dao: DanaFlowDao) {

    val allWallets: Flow<List<WalletEntity>> = dao.getAllWallets()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allCategoryBudgets: Flow<List<CategoryBudgetEntity>> = dao.getAllCategoryBudgets()
    val allExportReports: Flow<List<ExportReportEntity>> = dao.getAllExportReports()

    suspend fun ensureSeedDataIfNeeded() {
        if (dao.getWalletCount() > 0) return
        seedDefaultData()
    }

    suspend fun resetToDefaultDemoData() {
        dao.clearAllTransactions()
        dao.clearAllWallets()
        dao.clearAllBudgets()
        seedDefaultData()
    }

    private suspend fun seedDefaultData() {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 24 * hour

        val defaultBudgets = listOf(
            CategoryBudgetEntity("food", "Makanan & Kopi", 2_500_000L, "restaurant", 0xFFF59E0B, true),
            CategoryBudgetEntity("transport", "Transportasi & BBM", 1_200_000L, "commute", 0xFF3B82F6, true),
            CategoryBudgetEntity("bills", "Tagihan & Internet", 1_800_000L, "bolt", 0xFF8B5CF6, true),
            CategoryBudgetEntity("shopping", "Belanja & Kebutuhan", 1_500_000L, "shopping", 0xFFEC4899, true),
            CategoryBudgetEntity("entertainment", "Hiburan & Langganan", 800_000L, "movie", 0xFFF43F5E, true),
            CategoryBudgetEntity("health", "Kesehatan & Olahraga", 750_000L, "health", 0xFF14B8A6, true),
            CategoryBudgetEntity("education", "Edukasi & Buku", 600_000L, "school", 0xFF6366F1, true),
            CategoryBudgetEntity("salary", "Gaji & Tunjangan", 0L, "work", 0xFF059669, false),
            CategoryBudgetEntity("freelance", "Proyek & Freelance", 0L, "laptop", 0xFF10B981, false),
            CategoryBudgetEntity("investment", "Dividen & Investasi", 0L, "trending_up", 0xFFD97706, false),
            CategoryBudgetEntity("transfer", "Transfer Antar Dompet", 0L, "swap", 0xFF4F46E5, false)
        )
        dao.insertCategoryBudgets(defaultBudgets)

        val wallets = listOf(
            WalletEntity(
                name = "BCA Prioritas",
                type = "BANK",
                accountNumber = "•••• 4829",
                balance = 12_450_000L,
                colorHex = 0xFF065F46,
                iconKey = "bank",
                isMain = true
            ),
            WalletEntity(
                name = "GoPay & QRIS",
                type = "EWALLET",
                accountNumber = "0812 •••• 901",
                balance = 1_385_000L,
                colorHex = 0xFF0284C7,
                iconKey = "ewallet",
                isMain = false
            ),
            WalletEntity(
                name = "Dompet Tunai",
                type = "CASH",
                accountNumber = "Uang Fisik Harian",
                balance = 640_000L,
                colorHex = 0xFFD97706,
                iconKey = "cash",
                isMain = false
            ),
            WalletEntity(
                name = "Tabungan Darurat",
                type = "SAVINGS",
                accountNumber = "Deposito •••• 771",
                balance = 15_000_000L,
                colorHex = 0xFF4F46E5,
                iconKey = "savings",
                isMain = false
            )
        )
        val walletIds = dao.insertWallets(wallets)
        val bcaId = walletIds.getOrElse(0) { 1L }
        val gopayId = walletIds.getOrElse(1) { 2L }
        val cashId = walletIds.getOrElse(2) { 3L }
        val savingsId = walletIds.getOrElse(3) { 4L }

        val sampleTransactions = listOf(
            TransactionEntity(
                title = "Kopi Kenangan & Croissant",
                amount = 54_000L,
                type = "EXPENSE",
                categoryId = "food",
                categoryName = "Makanan & Kopi",
                walletId = gopayId,
                walletName = "GoPay & QRIS",
                timestamp = now - (2 * hour),
                note = "Meeting pagi via QRIS",
                needVsWant = "KEINGINAN"
            ),
            TransactionEntity(
                title = "Makan Siang Nasi Padang Sederhana",
                amount = 68_000L,
                type = "EXPENSE",
                categoryId = "food",
                categoryName = "Makanan & Kopi",
                walletId = cashId,
                walletName = "Dompet Tunai",
                timestamp = now - (6 * hour),
                note = "Makan siang kantor",
                needVsWant = "KEBUTUHAN"
            ),
            TransactionEntity(
                title = "Isi Bensin Pertamax & Parkir",
                amount = 165_000L,
                type = "EXPENSE",
                categoryId = "transport",
                categoryName = "Transportasi & BBM",
                walletId = gopayId,
                walletName = "GoPay & QRIS",
                timestamp = now - (1 * day + 3 * hour),
                note = "Full tank mingguan",
                needVsWant = "KEBUTUHAN"
            ),
            TransactionEntity(
                title = "Proyek Desain UI/UX Aplikasi",
                amount = 3_750_000L,
                type = "INCOME",
                categoryId = "freelance",
                categoryName = "Proyek & Freelance",
                walletId = bcaId,
                walletName = "BCA Prioritas",
                timestamp = now - (2 * day),
                note = "Pelunasan termin 2 klien Jakarta",
                needVsWant = "PEMASUKAN"
            ),
            TransactionEntity(
                title = "Tagihan Listrik PLN & WiFi Biznet",
                amount = 745_000L,
                type = "EXPENSE",
                categoryId = "bills",
                categoryName = "Tagihan & Internet",
                walletId = bcaId,
                walletName = "BCA Prioritas",
                timestamp = now - (3 * day),
                note = "Tagihan rutin bulanan rumah",
                needVsWant = "KEBUTUHAN"
            ),
            TransactionEntity(
                title = "Top Up GoPay dari BCA",
                amount = 1_000_000L,
                type = "TRANSFER",
                categoryId = "transfer",
                categoryName = "Transfer Antar Dompet",
                walletId = bcaId,
                walletName = "BCA Prioritas",
                targetWalletId = gopayId,
                targetWalletName = "GoPay & QRIS",
                adminFee = 1_000L,
                timestamp = now - (4 * day),
                note = "Alokasi saldo QRIS mingguan",
                needVsWant = "TRANSFER"
            ),
            TransactionEntity(
                title = "Belanja Bulanan Superindo",
                amount = 620_000L,
                type = "EXPENSE",
                categoryId = "shopping",
                categoryName = "Belanja & Kebutuhan",
                walletId = bcaId,
                walletName = "BCA Prioritas",
                timestamp = now - (5 * day),
                note = "Stok dapur, beras, buah & sayur",
                needVsWant = "KEBUTUHAN"
            ),
            TransactionEntity(
                title = "Langganan Spotify Duo & Netflix",
                amount = 241_000L,
                type = "EXPENSE",
                categoryId = "entertainment",
                categoryName = "Hiburan & Langganan",
                walletId = gopayId,
                walletName = "GoPay & QRIS",
                timestamp = now - (6 * day),
                note = "Auto-debit hiburan bulanan",
                needVsWant = "KEINGINAN"
            ),
            TransactionEntity(
                title = "Alokasi Dana Darurat Bulanan",
                amount = 2_500_000L,
                type = "TRANSFER",
                categoryId = "transfer",
                categoryName = "Transfer Antar Dompet",
                walletId = bcaId,
                walletName = "BCA Prioritas",
                targetWalletId = savingsId,
                targetWalletName = "Tabungan Darurat",
                adminFee = 0L,
                timestamp = now - (7 * day),
                note = "Tabungan otomatis setiap gajian",
                needVsWant = "TABUNGAN"
            ),
            TransactionEntity(
                title = "Gaji Bulanan & Tunjangan Kinerja",
                amount = 14_500_000L,
                type = "INCOME",
                categoryId = "salary",
                categoryName = "Gaji & Tunjangan",
                walletId = bcaId,
                walletName = "BCA Prioritas",
                timestamp = now - (9 * day),
                note = "Payroll bulanan bersih",
                needVsWant = "PEMASUKAN"
            ),
            TransactionEntity(
                title = "Vitamin & Cek Kesehatan Kimia Farma",
                amount = 285_000L,
                type = "EXPENSE",
                categoryId = "health",
                categoryName = "Kesehatan & Olahraga",
                walletId = cashId,
                walletName = "Dompet Tunai",
                timestamp = now - (11 * day),
                note = "Multivitamin harian",
                needVsWant = "KEBUTUHAN"
            )
        )
        dao.insertTransactionsRaw(sampleTransactions)
    }

    suspend fun saveWallet(wallet: WalletEntity): Long {
        return if (wallet.id == 0L) {
            dao.insertWallet(wallet)
        } else {
            dao.updateWallet(wallet)
            wallet.id
        }
    }

    suspend fun deleteWallet(walletId: Long) {
        dao.deleteTransactionsForWallet(walletId)
        dao.deleteWalletById(walletId)
    }

    suspend fun recordTransaction(tx: TransactionEntity): Long {
        return dao.recordTransactionAndSyncWallets(tx)
    }

    suspend fun updateTransaction(tx: TransactionEntity) {
        dao.updateTransactionAndSyncWallets(tx)
    }

    suspend fun deleteTransaction(txId: Long) {
        dao.deleteTransactionAndRestoreWallets(txId)
    }

    suspend fun saveCategoryBudget(budget: CategoryBudgetEntity) {
        dao.insertCategoryBudget(budget)
    }

    suspend fun saveExportReport(report: ExportReportEntity): Long {
        return dao.insertExportReport(report)
    }

    suspend fun deleteExportReport(reportId: Long) {
        dao.deleteExportReportById(reportId)
    }
}
