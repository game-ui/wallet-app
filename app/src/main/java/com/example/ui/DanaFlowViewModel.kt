package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryBudgetEntity
import com.example.data.DanaFlowRepository
import com.example.data.ExportReportEntity
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import com.example.util.ReportExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppDestination {
    BERANDA,
    CATAT,
    ANALISIS,
    LAPORAN
}

data class FilterState(
    val currentTab: AppDestination = AppDestination.BERANDA,
    val selectedWalletId: Long? = null,
    val selectedType: String = "ALL", // "ALL", "EXPENSE", "INCOME", "TRANSFER"
    val searchQuery: String = "",
    val selectedPeriod: String = "BULAN_INI", // "MINGGU_INI", "BULAN_INI", "TIGA_BULAN", "SEMUA"
    val isBalanceHidden: Boolean = false,
    val isDarkTheme: Boolean = false,
    val userName: String = "Bima Pradana",
    val userSubtitle: String = "Dompet Offline Lokal",
    val preselectedTxType: String = "EXPENSE",
    val editingTransaction: TransactionEntity? = null,
    val statusMessage: String? = null
)

data class DanaFlowUiState(
    val currentTab: AppDestination = AppDestination.BERANDA,
    val wallets: List<WalletEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val periodTransactions: List<TransactionEntity> = emptyList(),
    val categoryBudgets: List<CategoryBudgetEntity> = emptyList(),
    val exportReports: List<ExportReportEntity> = emptyList(),
    val selectedWalletId: Long? = null,
    val selectedType: String = "ALL",
    val searchQuery: String = "",
    val selectedPeriod: String = "BULAN_INI",
    val isBalanceHidden: Boolean = false,
    val isDarkTheme: Boolean = false,
    val userName: String = "Bima Pradana",
    val userSubtitle: String = "Dompet Offline Lokal",
    val preselectedTxType: String = "EXPENSE",
    val editingTransaction: TransactionEntity? = null,
    val statusMessage: String? = null,
    val totalCombinedBalance: Long = 0L,
    val periodIncome: Long = 0L,
    val periodExpense: Long = 0L,
    val periodTransfer: Long = 0L,
    val totalMonthlyBudget: Long = 0L
)

class DanaFlowViewModel(
    private val repository: DanaFlowRepository,
    private val appContext: Context
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("danaflow_offline_prefs", Context.MODE_PRIVATE)

    private val filterState = MutableStateFlow(
        FilterState(
            isBalanceHidden = prefs.getBoolean("hide_balance", false),
            isDarkTheme = prefs.getBoolean("dark_theme", false),
            userName = prefs.getString("user_name", "Bima Pradana") ?: "Bima Pradana",
            userSubtitle = prefs.getString("user_subtitle", "Mode Privat & Offline") ?: "Mode Privat & Offline"
        )
    )

    init {
        viewModelScope.launch {
            repository.ensureSeedDataIfNeeded()
        }
    }

    val uiState: StateFlow<DanaFlowUiState> = combine(
        repository.allWallets,
        repository.allTransactions,
        repository.allCategoryBudgets,
        repository.allExportReports,
        filterState
    ) { wallets, transactions, budgets, reports, filter ->
        val now = System.currentTimeMillis()
        val dayMs = 24 * 3_600_000L
        val cutoff = when (filter.selectedPeriod) {
            "MINGGU_INI" -> now - 7 * dayMs
            "BULAN_INI" -> now - 30 * dayMs
            "TIGA_BULAN" -> now - 90 * dayMs
            else -> 0L
        }

        val periodTxs = transactions.filter { tx ->
            val matchesTime = tx.timestamp >= cutoff
            val matchesWallet = filter.selectedWalletId == null ||
                tx.walletId == filter.selectedWalletId ||
                tx.targetWalletId == filter.selectedWalletId
            matchesTime && matchesWallet
        }

        val filteredTxs = periodTxs.filter { tx ->
            val matchesType = filter.selectedType == "ALL" || tx.type == filter.selectedType
            val q = filter.searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                tx.title.lowercase().contains(q) ||
                tx.categoryName.lowercase().contains(q) ||
                tx.walletName.lowercase().contains(q) ||
                tx.note.lowercase().contains(q)
            matchesType && matchesQuery
        }

        val totalBalance = wallets.sumOf { it.balance }
        val income = periodTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = periodTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val transfer = periodTxs.filter { it.type == "TRANSFER" }.sumOf { it.amount }
        val totalBudget = budgets.filter { it.isExpense }.sumOf { it.monthlyLimit }

        DanaFlowUiState(
            currentTab = filter.currentTab,
            wallets = wallets,
            allTransactions = transactions,
            filteredTransactions = filteredTxs,
            periodTransactions = periodTxs,
            categoryBudgets = budgets,
            exportReports = reports,
            selectedWalletId = filter.selectedWalletId,
            selectedType = filter.selectedType,
            searchQuery = filter.searchQuery,
            selectedPeriod = filter.selectedPeriod,
            isBalanceHidden = filter.isBalanceHidden,
            isDarkTheme = filter.isDarkTheme,
            userName = filter.userName,
            userSubtitle = filter.userSubtitle,
            preselectedTxType = filter.preselectedTxType,
            editingTransaction = filter.editingTransaction,
            statusMessage = filter.statusMessage,
            totalCombinedBalance = totalBalance,
            periodIncome = income,
            periodExpense = expense,
            periodTransfer = transfer,
            totalMonthlyBudget = totalBudget
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DanaFlowUiState()
    )

    fun selectTab(tab: AppDestination) {
        filterState.update {
            it.copy(
                currentTab = tab,
                editingTransaction = if (tab != AppDestination.CATAT) null else it.editingTransaction
            )
        }
    }

    fun openCatatWithType(type: String) {
        filterState.update {
            it.copy(
                currentTab = AppDestination.CATAT,
                preselectedTxType = type,
                editingTransaction = null
            )
        }
    }

    fun startEditingTransaction(tx: TransactionEntity) {
        filterState.update {
            it.copy(
                currentTab = AppDestination.CATAT,
                preselectedTxType = tx.type,
                editingTransaction = tx
            )
        }
    }

    fun selectWalletFilter(walletId: Long?) {
        filterState.update {
            val next = if (it.selectedWalletId == walletId) null else walletId
            it.copy(selectedWalletId = next)
        }
    }

    fun setWalletFilterDirect(walletId: Long?) {
        filterState.update { it.copy(selectedWalletId = walletId) }
    }

    fun selectTypeFilter(type: String) {
        filterState.update { it.copy(selectedType = type) }
    }

    fun updateSearchQuery(query: String) {
        filterState.update { it.copy(searchQuery = query) }
    }

    fun selectPeriod(period: String) {
        filterState.update { it.copy(selectedPeriod = period) }
    }

    fun toggleBalanceVisibility() {
        filterState.update { state ->
            val next = !state.isBalanceHidden
            prefs.edit().putBoolean("hide_balance", next).apply()
            state.copy(isBalanceHidden = next)
        }
    }

    fun toggleDarkTheme() {
        filterState.update { state ->
            val next = !state.isDarkTheme
            prefs.edit().putBoolean("dark_theme", next).apply()
            state.copy(isDarkTheme = next)
        }
    }

    fun updateUserProfile(name: String, subtitle: String) {
        val cleanName = name.trim().ifEmpty { "Pengguna Offline" }
        val cleanSub = subtitle.trim().ifEmpty { "Mode Privat & Offline" }
        prefs.edit()
            .putString("user_name", cleanName)
            .putString("user_subtitle", cleanSub)
            .apply()
        filterState.update {
            it.copy(
                userName = cleanName,
                userSubtitle = cleanSub,
                statusMessage = "Profil offline berhasil diperbarui"
            )
        }
    }

    fun clearStatusMessage() {
        filterState.update { it.copy(statusMessage = null) }
    }

    fun saveWallet(
        id: Long = 0L,
        name: String,
        type: String,
        accountNumber: String,
        balance: Long,
        colorHex: Long,
        iconKey: String,
        isMain: Boolean
    ) {
        viewModelScope.launch {
            val entity = WalletEntity(
                id = id,
                name = name.trim(),
                type = type,
                accountNumber = accountNumber.trim().ifEmpty { "Dompet Lokal" },
                balance = balance,
                colorHex = colorHex,
                iconKey = iconKey,
                isMain = isMain
            )
            repository.saveWallet(entity)
            filterState.update {
                it.copy(statusMessage = if (id == 0L) "Dompet '${entity.name}' ditambahkan" else "Dompet '${entity.name}' diperbarui")
            }
        }
    }

    fun deleteWallet(wallet: WalletEntity) {
        viewModelScope.launch {
            repository.deleteWallet(wallet.id)
            filterState.update {
                it.copy(
                    selectedWalletId = if (it.selectedWalletId == wallet.id) null else it.selectedWalletId,
                    statusMessage = "Dompet '${wallet.name}' telah dihapus"
                )
            }
        }
    }

    fun saveTransaction(
        existingId: Long = 0L,
        title: String,
        amount: Long,
        type: String,
        categoryId: String,
        categoryName: String,
        walletId: Long,
        walletName: String,
        targetWalletId: Long? = null,
        targetWalletName: String? = null,
        adminFee: Long = 0L,
        timestamp: Long = System.currentTimeMillis(),
        note: String = "",
        needVsWant: String = "KEBUTUHAN"
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                id = existingId,
                title = title.trim().ifEmpty { categoryName },
                amount = amount,
                type = type,
                categoryId = categoryId,
                categoryName = categoryName,
                walletId = walletId,
                walletName = walletName,
                targetWalletId = if (type == "TRANSFER") targetWalletId else null,
                targetWalletName = if (type == "TRANSFER") targetWalletName else null,
                adminFee = if (type == "TRANSFER") adminFee else 0L,
                timestamp = timestamp,
                note = note.trim(),
                needVsWant = needVsWant
            )
            if (existingId == 0L) {
                repository.recordTransaction(tx)
            } else {
                repository.updateTransaction(tx)
            }
            filterState.update {
                it.copy(
                    currentTab = AppDestination.BERANDA,
                    editingTransaction = null,
                    statusMessage = if (existingId == 0L) "Transaksi berhasil dicatat ke $walletName" else "Transaksi berhasil diperbarui"
                )
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx.id)
            filterState.update {
                it.copy(
                    editingTransaction = null,
                    statusMessage = "Transaksi '${tx.title}' dihapus & saldo dompet disesuaikan"
                )
            }
        }
    }

    fun saveCategoryBudget(
        categoryId: String,
        categoryName: String,
        monthlyLimit: Long,
        iconKey: String,
        colorHex: Long,
        isExpense: Boolean = true
    ) {
        viewModelScope.launch {
            repository.saveCategoryBudget(
                CategoryBudgetEntity(
                    categoryId = categoryId,
                    categoryName = categoryName.trim(),
                    monthlyLimit = monthlyLimit,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    isExpense = isExpense
                )
            )
            filterState.update {
                it.copy(statusMessage = "Anggaran '$categoryName' disimpan")
            }
        }
    }

    fun generateAndSavePdfReport(
        periodLabel: String,
        walletFilterLabel: String,
        transactionsToExport: List<TransactionEntity>,
        includeExecutiveSummary: Boolean,
        includeWalletBreakdown: Boolean,
        includeCategoryAnalysis: Boolean,
        includeTransactionTable: Boolean,
        shareImmediately: Boolean
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val result = withContext(Dispatchers.IO) {
                ReportExporter.generatePdfReport(
                    context = appContext,
                    userName = state.userName,
                    periodLabel = periodLabel,
                    walletFilterLabel = walletFilterLabel,
                    wallets = state.wallets,
                    transactions = transactionsToExport,
                    categoryBudgets = state.categoryBudgets,
                    includeExecutiveSummary = includeExecutiveSummary,
                    includeWalletBreakdown = includeWalletBreakdown,
                    includeCategoryAnalysis = includeCategoryAnalysis,
                    includeTransactionTable = includeTransactionTable
                )
            }
            val totalInc = transactionsToExport.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExp = transactionsToExport.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            repository.saveExportReport(
                ExportReportEntity(
                    reportTitle = result.reportTitle,
                    periodLabel = periodLabel,
                    walletFilterLabel = walletFilterLabel,
                    format = "PDF",
                    filePath = result.file.absolutePath,
                    fileSizeKb = result.fileSizeKb,
                    totalIncome = totalInc,
                    totalExpense = totalExp,
                    transactionCount = transactionsToExport.size
                )
            )
            if (shareImmediately) {
                ReportExporter.shareExportedFile(appContext, result.file.absolutePath, "PDF")
            }
            filterState.update {
                it.copy(statusMessage = "PDF '${result.file.name}' (${result.fileSizeKb} KB) berhasil dibuat secara offline")
            }
        }
    }

    fun generateAndSaveCsvReport(
        periodLabel: String,
        walletFilterLabel: String,
        transactionsToExport: List<TransactionEntity>,
        shareImmediately: Boolean
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                ReportExporter.generateCsvReport(
                    context = appContext,
                    periodLabel = periodLabel,
                    transactions = transactionsToExport
                )
            }
            val totalInc = transactionsToExport.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExp = transactionsToExport.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            repository.saveExportReport(
                ExportReportEntity(
                    reportTitle = result.reportTitle,
                    periodLabel = periodLabel,
                    walletFilterLabel = walletFilterLabel,
                    format = "CSV",
                    filePath = result.file.absolutePath,
                    fileSizeKb = result.fileSizeKb,
                    totalIncome = totalInc,
                    totalExpense = totalExp,
                    transactionCount = transactionsToExport.size
                )
            )
            if (shareImmediately) {
                ReportExporter.shareExportedFile(appContext, result.file.absolutePath, "CSV")
            }
            filterState.update {
                it.copy(statusMessage = "File CSV '${result.file.name}' berhasil disimpan secara offline")
            }
        }
    }

    fun deleteExportReport(reportId: Long) {
        viewModelScope.launch {
            repository.deleteExportReport(reportId)
            filterState.update {
                it.copy(statusMessage = "Riwayat laporan dihapus")
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToDefaultDemoData()
            filterState.update {
                it.copy(
                    selectedWalletId = null,
                    selectedType = "ALL",
                    searchQuery = "",
                    statusMessage = "Data contoh multi-dompet berhasil dipulihkan"
                )
            }
        }
    }

    class Factory(
        private val repository: DanaFlowRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DanaFlowViewModel(repository, appContext) as T
        }
    }
}
