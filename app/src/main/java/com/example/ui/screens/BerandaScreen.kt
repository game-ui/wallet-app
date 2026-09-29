package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import com.example.ui.DanaFlowUiState
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TransferIndigo
import com.example.util.CurrencyFormatter

@Composable
fun BerandaScreen(
    uiState: DanaFlowUiState,
    onToggleBalanceVisibility: () -> Unit,
    onSelectWalletFilter: (Long?) -> Unit,
    onSelectTypeFilter: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onQuickAddClick: (String) -> Unit,
    onAddOrEditWallet: (WalletEntity?) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onOpenProfileSettings: () -> Unit,
    onNavigateToAnalisis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("beranda_screen"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DanaFlow",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onOpenProfileSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Hero Combined Balance Card
        item {
            HeroCombinedBalanceCard(
                totalBalance = activeWallet?.balance ?: uiState.totalCombinedBalance,
                activeWalletName = activeWallet?.name,
                periodIncome = uiState.periodIncome,
                periodExpense = uiState.periodExpense,
                isBalanceHidden = uiState.isBalanceHidden,
                onToggleVisibility = onToggleBalanceVisibility,
                onQuickExpense = { onQuickAddClick("EXPENSE") },
                onQuickIncome = { onQuickAddClick("INCOME") },
                onQuickTransfer = { onQuickAddClick("TRANSFER") },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // 3. Multi-Dompet Horizontal Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Multi-Dompet Saya (${uiState.wallets.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (activeWallet != null) "Filter aktif: ${activeWallet.name} (ketuk untuk reset)" else "Ketuk dompet untuk filter transaksi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = { onAddOrEditWallet(null) },
                        modifier = Modifier.testTag("add_wallet_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Dompet", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Dompet Baru")
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.wallets, key = { it.id }) { wallet ->
                        WalletCarouselCard(
                            wallet = wallet,
                            isSelected = uiState.selectedWalletId == wallet.id,
                            isBalanceHidden = uiState.isBalanceHidden,
                            onClick = { onSelectWalletFilter(wallet.id) },
                            onEditClick = { onAddOrEditWallet(wallet) }
                        )
                    }

                    item {
                        AddWalletCardPlaceholder(onClick = { onAddOrEditWallet(null) })
                    }
                }
            }
        }

        // 4. Monthly Budget Pulse Card
        if (uiState.totalMonthlyBudget > 0L) {
            item {
                MonthlyBudgetPulseCard(
                    spent = uiState.periodExpense,
                    budgetLimit = uiState.totalMonthlyBudget,
                    isBalanceHidden = uiState.isBalanceHidden,
                    onClick = onNavigateToAnalisis,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // 5. Recent Transactions Header + Search & Filter Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Transaksi (${uiState.filteredTransactions.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (uiState.selectedWalletId != null) {
                        TextButton(onClick = { onSelectWalletFilter(null) }) {
                            Text("Semua Dompet")
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Cari makan, kopi, gaji, catatan...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cari Transaksi")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus Pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_transaction_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filterOptions = listOf(
                        "ALL" to "Semua",
                        "EXPENSE" to "Pengeluaran",
                        "INCOME" to "Pemasukan",
                        "TRANSFER" to "Transfer"
                    )
                    items(filterOptions) { (code, label) ->
                        FilterChip(
                            selected = uiState.selectedType == code,
                            onClick = { onSelectTypeFilter(code) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_chip_${code.lowercase()}")
                        )
                    }
                }
            }
        }

        // 6. Transaction List or Empty State
        if (uiState.filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Belum Ada Transaksi yang Cocok",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Ketuk tombol '+ Catat' untuk menyimpan pengeluaran, pemasukan, atau transfer antar dompet secara offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.filteredTransactions, key = { it.id }) { tx ->
                TransactionRowCard(
                    transaction = tx,
                    isBalanceHidden = uiState.isBalanceHidden,
                    onClick = { onTransactionClick(tx) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroCombinedBalanceCard(
    totalBalance: Long,
    activeWalletName: String?,
    periodIncome: Long,
    periodExpense: Long,
    isBalanceHidden: Boolean,
    onToggleVisibility: () -> Unit,
    onQuickExpense: () -> Unit,
    onQuickIncome: () -> Unit,
    onQuickTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (activeWalletName != null) "SALDO ${activeWalletName.uppercase()}" else "TOTAL SALDO GABUNGAN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatRupiah(totalBalance, isBalanceHidden),
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("toggle_balance_visibility")
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Sembunyikan atau Tampilkan Saldo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Income vs Expense Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Pemasukan",
                            tint = IncomeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pemasukan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCompactRupiah(periodIncome, isBalanceHidden),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ExpenseCoral.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Pengeluaran",
                            tint = ExpenseCoral,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pengeluaran",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCompactRupiah(periodExpense, isBalanceHidden),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = ExpenseCoral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Action Buttons inside Balance Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroQuickButton(
                    label = "- Pengeluaran",
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onQuickExpense,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_expense_button")
                )
                HeroQuickButton(
                    label = "+ Pemasukan",
                    bgColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onQuickIncome,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_income_button")
                )
                HeroQuickButton(
                    label = "⇄ Transfer",
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onQuickTransfer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_transfer_button")
                )
            }
        }
    }
}

@Composable
private fun HeroQuickButton(
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WalletCarouselCard(
    wallet: WalletEntity,
    isSelected: Boolean,
    isBalanceHidden: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val baseColor = CurrencyFormatter.getColorFromHex(wallet.colorHex)

    Card(
        modifier = Modifier
            .width(215.dp)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) GoldAccent else Color.Transparent,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .testTag("wallet_card_${wallet.id}"),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(baseColor, baseColor.copy(alpha = 0.80f))
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CurrencyFormatter.getIconForKey(wallet.iconKey),
                                contentDescription = wallet.type,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (wallet.isMain) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldAccent
                            ) {
                                Text(
                                    text = "UTAMA",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF1C1917),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah Dompet ${wallet.name}",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = wallet.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${wallet.type} • ${wallet.accountNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = CurrencyFormatter.formatRupiah(wallet.balance, isBalanceHidden),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AddWalletCardPlaceholder(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .width(148.dp)
            .height(142.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Dompet Baru",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tambah Dompet",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MonthlyBudgetPulseCard(
    spent: Long,
    budgetLimit: Long,
    isBalanceHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (budgetLimit > 0) (spent.toFloat() / budgetLimit.toFloat()).coerceIn(0f, 1f) else 0f
    val pct = if (budgetLimit > 0) ((spent * 100) / budgetLimit).toInt() else 0
    val barColor = when {
        pct >= 90 -> ExpenseCoral
        pct >= 75 -> GoldAccent
        else -> IncomeGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kesehatan Anggaran Bulanan",
                    style = MaterialTheme.typography.titleSmall
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = barColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$pct% Terpakai",
                        style = MaterialTheme.typography.labelMedium,
                        color = barColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terpakai: ${CurrencyFormatter.formatRupiah(spent, isBalanceHidden)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Batas: ${CurrencyFormatter.formatRupiah(budgetLimit, isBalanceHidden)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TransactionRowCard(
    transaction: TransactionEntity,
    isBalanceHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (transaction.type) {
        "INCOME" -> IncomeGreen
        "EXPENSE" -> ExpenseCoral
        else -> TransferIndigo
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "TRANSFER") {
                        Icons.AutoMirrored.Filled.CompareArrows
                    } else {
                        CurrencyFormatter.getIconForKey(transaction.categoryId)
                    },
                    contentDescription = transaction.categoryName,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                val subtitle = if (transaction.type == "TRANSFER" && transaction.targetWalletName != null) {
                    "${transaction.walletName} → ${transaction.targetWalletName}"
                } else {
                    "${transaction.categoryName} • ${transaction.walletName}"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = CurrencyFormatter.formatDateTime(transaction.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatSignedRupiah(transaction.amount, transaction.type, isBalanceHidden),
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = transaction.needVsWant,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
