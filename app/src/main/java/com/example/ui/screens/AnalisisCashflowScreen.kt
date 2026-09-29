package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CategoryBudgetEntity
import com.example.data.TransactionEntity
import com.example.ui.DanaFlowUiState
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.CurrencyFormatter
import java.util.Calendar

data class CategoryBreakdownItem(
    val categoryId: String,
    val categoryName: String,
    val spent: Long,
    val budgetLimit: Long,
    val percentageOfTotal: Float,
    val color: Color,
    val iconKey: String,
    val txCount: Int
)

@Composable
fun AnalisisCashflowScreen(
    uiState: DanaFlowUiState,
    onSelectPeriod: (String) -> Unit,
    onSaveCategoryBudget: (CategoryBudgetEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingBudget by remember { mutableStateOf<CategoryBudgetEntity?>(null) }

    val netCashflow = uiState.periodIncome - uiState.periodExpense
    val savingsRate = if (uiState.periodIncome > 0L) {
        ((netCashflow.toDouble() / uiState.periodIncome.toDouble()) * 100.0).toInt().coerceIn(-100, 100)
    } else {
        0
    }

    val expenseTxs = remember(uiState.periodTransactions) {
        uiState.periodTransactions.filter { it.type == "EXPENSE" }
    }
    val kebutuhanSpent = remember(expenseTxs) {
        expenseTxs.filter { it.needVsWant == "KEBUTUHAN" }.sumOf { it.amount }
    }
    val keinginanSpent = remember(expenseTxs) {
        expenseTxs.filter { it.needVsWant == "KEINGINAN" }.sumOf { it.amount }
    }

    val categoryBreakdowns = remember(expenseTxs, uiState.categoryBudgets, uiState.periodExpense) {
        val expenseBudgets = uiState.categoryBudgets.filter { it.isExpense }
        expenseBudgets.map { budget ->
            val matching = expenseTxs.filter { it.categoryId == budget.categoryId }
            val spent = matching.sumOf { it.amount }
            val share = if (uiState.periodExpense > 0L) {
                (spent.toFloat() / uiState.periodExpense.toFloat()) * 100f
            } else {
                0f
            }
            CategoryBreakdownItem(
                categoryId = budget.categoryId,
                categoryName = budget.categoryName,
                spent = spent,
                budgetLimit = budget.monthlyLimit,
                percentageOfTotal = share,
                color = CurrencyFormatter.getColorFromHex(budget.colorHex),
                iconKey = budget.iconKey,
                txCount = matching.size
            )
        }.sortedByDescending { it.spent }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("analisis_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Time Period Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Analisis Cashflow",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Evaluasi arus kas, struktur pengeluaran, dan batas anggaran secara real-time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val periods = listOf(
                        "MINGGU_INI" to "7 Hari Terakhir",
                        "BULAN_INI" to "30 Hari Terakhir",
                        "TIGA_BULAN" to "3 Bulan",
                        "SEMUA" to "Semua Waktu"
                    )
                    items(periods) { (code, label) ->
                        FilterChip(
                            selected = uiState.selectedPeriod == code,
                            onClick = { onSelectPeriod(code) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("period_chip_${code.lowercase()}")
                        )
                    }
                }
            }
        }

        // 2. Net Cashflow & Rasio Tabungan Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldDeep)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ARUS KAS BERSIH (NET CASHFLOW)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFA7F3D0)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.formatRupiah(netCashflow, uiState.isBalanceHidden),
                                style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = if (netCashflow >= 0) Color.White else Color(0xFFFDA4AF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (savingsRate >= 20) EmeraldMint.copy(alpha = 0.25f) else GoldAccent.copy(alpha = 0.25f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$savingsRate%",
                                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rasio Tabungan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFD1FAE5)
                                )
                            }
                        }
                    }

                    // Kebutuhan vs Keinginan Split Bar
                    val totalClassified = (kebutuhanSpent + keinginanSpent).coerceAtLeast(1L)
                    val kebutuhanPct = ((kebutuhanSpent * 100) / totalClassified).toInt()
                    val keinginanPct = 100 - kebutuhanPct

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kebutuhan Pokok: $kebutuhanPct%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF6EE7B7)
                            )
                            Text(
                                text = "Keinginan: $keinginanPct%",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccent
                            )
                        }
                        LinearProgressIndicator(
                            progress = { kebutuhanPct / 100f },
                            color = EmeraldMint,
                            trackColor = GoldAccent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }

        // 3. Custom Canvas Cashflow Trend Bar Chart
        item {
            CashflowTrendChartCard(
                transactions = uiState.periodTransactions,
                isBalanceHidden = uiState.isBalanceHidden
            )
        }

        // 4. Custom Canvas Donut Chart for Expense Distribution
        item {
            CategoryDonutChartCard(
                totalExpense = uiState.periodExpense,
                breakdowns = categoryBreakdowns.filter { it.spent > 0L },
                isBalanceHidden = uiState.isBalanceHidden
            )
        }

        // 5. Budget Management per Category
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kontrol Anggaran per Kategori",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Ketuk ikon pensil untuk menyesuaikan batas anggaran bulanan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(categoryBreakdowns, key = { it.categoryId }) { item ->
            CategoryBudgetCard(
                item = item,
                isBalanceHidden = uiState.isBalanceHidden,
                onEditClick = {
                    val entity = uiState.categoryBudgets.find { it.categoryId == item.categoryId }
                    if (entity != null) {
                        editingBudget = entity
                    }
                }
            )
        }

        item {
            Spacer(Modifier.height(76.dp))
        }
    }

    editingBudget?.let { budget ->
        EditCategoryBudgetDialog(
            budget = budget,
            onDismiss = { editingBudget = null },
            onSave = { updatedLimit ->
                onSaveCategoryBudget(budget.copy(monthlyLimit = updatedLimit))
                editingBudget = null
            }
        )
    }
}

@Composable
private fun CashflowTrendChartCard(
    transactions: List<TransactionEntity>,
    isBalanceHidden: Boolean
) {
    // Group into 5 recent buckets (days/segments)
    val buckets = remember(transactions) {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 3_600_000L
        (4 downTo 0).map { offset ->
            val start = now - (offset + 1) * 2 * dayMs
            val end = now - offset * 2 * dayMs
            val slice = transactions.filter { it.timestamp in start..end }
            val inc = slice.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exp = slice.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val label = if (offset == 0) "Kini" else "-${offset * 2}h"
            Triple(label, inc, exp)
        }
    }

    val maxVal = remember(buckets) {
        buckets.maxOfOrNull { maxOf(it.second, it.third) }?.coerceAtLeast(100_000L) ?: 100_000L
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Tren Pemasukan vs Pengeluaran", style = MaterialTheme.typography.titleSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendDot(color = IncomeGreen, label = "Masuk")
                    LegendDot(color = ExpenseCoral, label = "Keluar")
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val groupWidth = size.width / buckets.size
                val barWidth = (groupWidth * 0.28f).coerceAtMost(28f)
                val maxBarHeight = size.height - 24f

                // Horizontal guide lines
                listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                    val y = maxBarHeight * frac
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f
                    )
                }

                buckets.forEachIndexed { index, (_, inc, exp) ->
                    val centerX = index * groupWidth + groupWidth / 2f
                    val incRatio = (inc.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
                    val expRatio = (exp.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)

                    val incHeight = maxBarHeight * incRatio
                    val expHeight = maxBarHeight * expRatio

                    // Income bar
                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(centerX - barWidth - 3f, maxBarHeight - incHeight),
                        size = Size(barWidth, incHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Expense bar
                    drawRoundRect(
                        color = ExpenseCoral,
                        topLeft = Offset(centerX + 3f, maxBarHeight - expHeight),
                        size = Size(barWidth, expHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                buckets.forEach { (label, _, _) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CategoryDonutChartCard(
    totalExpense: Long,
    breakdowns: List<CategoryBreakdownItem>,
    isBalanceHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Distribusi Kategori Pengeluaran", style = MaterialTheme.typography.titleSmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier.size(136.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(124.dp)) {
                        val strokeWidth = 28f
                        if (breakdowns.isEmpty() || totalExpense <= 0L) {
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.35f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        } else {
                            var startAngle = -90f
                            breakdowns.forEach { item ->
                                val sweep = (item.percentageOfTotal / 100f) * 360f
                                drawArc(
                                    color = item.color,
                                    startAngle = startAngle,
                                    sweepAngle = (sweep - 3f).coerceAtLeast(4f),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweep
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCompactRupiah(totalExpense, isBalanceHidden),
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    breakdowns.take(4).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${item.percentageOfTotal.toInt()}%",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    item: CategoryBreakdownItem,
    isBalanceHidden: Boolean,
    onEditClick: () -> Unit
) {
    val ratio = if (item.budgetLimit > 0L) (item.spent.toFloat() / item.budgetLimit.toFloat()).coerceIn(0f, 1f) else 0f
    val budgetPct = if (item.budgetLimit > 0L) ((item.spent * 100) / item.budgetLimit).toInt() else 0
    val statusColor = when {
        budgetPct >= 100 -> ExpenseCoral
        budgetPct >= 80 -> GoldAccent
        else -> item.color
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.color.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CurrencyFormatter.getIconForKey(item.iconKey),
                            contentDescription = item.categoryName,
                            tint = item.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(item.categoryName, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${item.txCount} transaksi • ${item.percentageOfTotal.toInt()}% porsi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_budget_${item.categoryId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Atur Anggaran ${item.categoryName}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { ratio },
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Realisasi: ${CurrencyFormatter.formatRupiah(item.spent, isBalanceHidden)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (item.budgetLimit > 0L) {
                        "Anggaran: ${CurrencyFormatter.formatRupiah(item.budgetLimit, isBalanceHidden)} ($budgetPct%)"
                    } else {
                        "Belum ada batas"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun EditCategoryBudgetDialog(
    budget: CategoryBudgetEntity,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var limitInput by remember { mutableStateOf(budget.monthlyLimit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Anggaran ${budget.categoryName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tetapkan batas pengeluaran bulanan untuk kategori ini agar DanaFlow dapat memberi peringatan dini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Batas Bulanan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = limitInput.toLongOrNull() ?: 0L
                    onSave(parsed)
                },
                modifier = Modifier.testTag("save_budget_confirm_button")
            ) {
                Text("Simpan Anggaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
