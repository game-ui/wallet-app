package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DanaFlowDatabase
import com.example.data.DanaFlowRepository
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import com.example.ui.AppDestination
import com.example.ui.DanaFlowViewModel
import com.example.ui.screens.AddOrEditWalletDialog
import com.example.ui.screens.AnalisisCashflowScreen
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.CatatTransaksiScreen
import com.example.ui.screens.LaporanEksporPdfScreen
import com.example.ui.screens.ProfileAndOfflineBackupDialog
import com.example.ui.screens.TransactionDetailDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ReportExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current.applicationContext
            val database = remember { DanaFlowDatabase.getInstance(context) }
            val repository = remember { DanaFlowRepository(database.danaFlowDao()) }
            val viewModel: DanaFlowViewModel = viewModel(
                factory = DanaFlowViewModel.Factory(repository, context)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
                DanaFlowApp(viewModel = viewModel)
            }
        }
    }
}

private data class NavItem(
    val destination: AppDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun DanaFlowApp(viewModel: DanaFlowViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showWalletDialog by remember { mutableStateOf(false) }
    var editingWallet by remember { mutableStateOf<WalletEntity?>(null) }
    var inspectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Handle system Back on secondary tabs
    if (uiState.currentTab != AppDestination.BERANDA) {
        BackHandler {
            viewModel.selectTab(AppDestination.BERANDA)
        }
    }

    val navItems = remember {
        listOf(
            NavItem(
                destination = AppDestination.BERANDA,
                label = "Beranda",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_beranda"
            ),
            NavItem(
                destination = AppDestination.CATAT,
                label = "Catat",
                selectedIcon = Icons.Filled.AddCircle,
                unselectedIcon = Icons.Outlined.AddCircleOutline,
                testTag = "nav_catat"
            ),
            NavItem(
                destination = AppDestination.ANALISIS,
                label = "Analisis",
                selectedIcon = Icons.Filled.Analytics,
                unselectedIcon = Icons.Outlined.Analytics,
                testTag = "nav_analisis"
            ),
            NavItem(
                destination = AppDestination.LAPORAN,
                label = "Laporan PDF",
                selectedIcon = Icons.Filled.PictureAsPdf,
                unselectedIcon = Icons.Outlined.PictureAsPdf,
                testTag = "nav_laporan"
            )
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (uiState.currentTab == AppDestination.BERANDA) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openCatatWithType("EXPENSE") },
                        icon = { Icon(Icons.Filled.Add, contentDescription = "Catat Transaksi Baru") },
                        text = { Text("Catat") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_catat_transaksi")
                    )
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = uiState.currentTab == item.destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = uiState.currentTab == item.destination
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                when (uiState.currentTab) {
                    AppDestination.BERANDA -> {
                        BerandaScreen(
                            uiState = uiState,
                            onToggleBalanceVisibility = viewModel::toggleBalanceVisibility,
                            onSelectWalletFilter = viewModel::selectWalletFilter,
                            onSelectTypeFilter = viewModel::selectTypeFilter,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onQuickAddClick = viewModel::openCatatWithType,
                            onAddOrEditWallet = { wallet ->
                                editingWallet = wallet
                                showWalletDialog = true
                            },
                            onTransactionClick = { tx ->
                                inspectedTransaction = tx
                            },
                            onOpenProfileSettings = {
                                showProfileDialog = true
                            },
                            onNavigateToAnalisis = {
                                viewModel.selectTab(AppDestination.ANALISIS)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AppDestination.CATAT -> {
                        CatatTransaksiScreen(
                            uiState = uiState,
                            onSaveTransaction = { existingId, title, amount, type, catId, catName, walletId, walletName, targetId, targetName, fee, ts, note, needVsWant ->
                                viewModel.saveTransaction(
                                    existingId = existingId,
                                    title = title,
                                    amount = amount,
                                    type = type,
                                    categoryId = catId,
                                    categoryName = catName,
                                    walletId = walletId,
                                    walletName = walletName,
                                    targetWalletId = targetId,
                                    targetWalletName = targetName,
                                    adminFee = fee,
                                    timestamp = ts,
                                    note = note,
                                    needVsWant = needVsWant
                                )
                            },
                            onAddCustomCategory = { id, name, limit, iconKey, colorHex, isExpense ->
                                viewModel.saveCategoryBudget(id, name, limit, iconKey, colorHex, isExpense)
                            },
                            onCancel = {
                                viewModel.selectTab(AppDestination.BERANDA)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AppDestination.ANALISIS -> {
                        AnalisisCashflowScreen(
                            uiState = uiState,
                            onSelectPeriod = viewModel::selectPeriod,
                            onSaveCategoryBudget = { budget ->
                                viewModel.saveCategoryBudget(
                                    categoryId = budget.categoryId,
                                    categoryName = budget.categoryName,
                                    monthlyLimit = budget.monthlyLimit,
                                    iconKey = budget.iconKey,
                                    colorHex = budget.colorHex,
                                    isExpense = budget.isExpense
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AppDestination.LAPORAN -> {
                        LaporanEksporPdfScreen(
                            uiState = uiState,
                            onSelectPeriod = viewModel::selectPeriod,
                            onSelectWalletFilter = viewModel::setWalletFilterDirect,
                            onExportPdf = { periodLabel, walletLabel, txs, incSum, incWal, incCat, incTx, share ->
                                viewModel.generateAndSavePdfReport(
                                    periodLabel = periodLabel,
                                    walletFilterLabel = walletLabel,
                                    transactionsToExport = txs,
                                    includeExecutiveSummary = incSum,
                                    includeWalletBreakdown = incWal,
                                    includeCategoryAnalysis = incCat,
                                    includeTransactionTable = incTx,
                                    shareImmediately = share
                                )
                            },
                            onExportCsv = { periodLabel, walletLabel, txs, share ->
                                viewModel.generateAndSaveCsvReport(
                                    periodLabel = periodLabel,
                                    walletFilterLabel = walletLabel,
                                    transactionsToExport = txs,
                                    shareImmediately = share
                                )
                            },
                            onDeleteReport = viewModel::deleteExportReport,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showWalletDialog) {
        AddOrEditWalletDialog(
            initialWallet = editingWallet,
            onDismiss = {
                showWalletDialog = false
                editingWallet = null
            },
            onSave = { id, name, type, accountNumber, balance, colorHex, iconKey, isMain ->
                viewModel.saveWallet(id, name, type, accountNumber, balance, colorHex, iconKey, isMain)
            },
            onDelete = { wallet ->
                viewModel.deleteWallet(wallet)
            }
        )
    }

    inspectedTransaction?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            onDismiss = { inspectedTransaction = null },
            onEdit = { toEdit ->
                viewModel.startEditingTransaction(toEdit)
            },
            onDelete = { toDelete ->
                viewModel.deleteTransaction(toDelete)
            }
        )
    }

    if (showProfileDialog) {
        val jsonPreview = remember(uiState.wallets, uiState.allTransactions, uiState.categoryBudgets, uiState.userName) {
            ReportExporter.generateJsonBackup(
                userName = uiState.userName,
                wallets = uiState.wallets,
                transactions = uiState.allTransactions,
                budgets = uiState.categoryBudgets
            )
        }
        ProfileAndOfflineBackupDialog(
            userName = uiState.userName,
            userSubtitle = uiState.userSubtitle,
            isDarkTheme = uiState.isDarkTheme,
            jsonBackupPreview = jsonPreview,
            onDismiss = { showProfileDialog = false },
            onSaveProfile = viewModel::updateUserProfile,
            onToggleDarkTheme = viewModel::toggleDarkTheme,
            onResetDemoData = viewModel::resetDemoData
        )
    }
}
