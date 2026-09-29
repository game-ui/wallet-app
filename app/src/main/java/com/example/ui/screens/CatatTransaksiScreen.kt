package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CategoryBudgetEntity
import com.example.ui.DanaFlowUiState
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TransferIndigo
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatatTransaksiScreen(
    uiState: DanaFlowUiState,
    onSaveTransaction: (
        existingId: Long,
        title: String,
        amount: Long,
        type: String,
        categoryId: String,
        categoryName: String,
        walletId: Long,
        walletName: String,
        targetWalletId: Long?,
        targetWalletName: String?,
        adminFee: Long,
        timestamp: Long,
        note: String,
        needVsWant: String
    ) -> Unit,
    onAddCustomCategory: (
        categoryId: String,
        categoryName: String,
        monthlyLimit: Long,
        iconKey: String,
        colorHex: Long,
        isExpense: Boolean
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editingTx = uiState.editingTransaction
    var txType by remember(editingTx, uiState.preselectedTxType) {
        mutableStateOf(editingTx?.type ?: uiState.preselectedTxType)
    }
    var amountString by remember(editingTx) {
        mutableStateOf(editingTx?.amount?.toString() ?: "50000")
    }
    var pendingOperator by remember { mutableStateOf<Char?>(null) }
    var firstOperand by remember { mutableLongStateOf(0L) }
    var showNumpad by remember { mutableStateOf(true) }

    val defaultSourceWallet = uiState.wallets.find { it.id == editingTx?.walletId }
        ?: uiState.wallets.find { it.isMain }
        ?: uiState.wallets.firstOrNull()
    var selectedWalletId by remember(editingTx, uiState.wallets) {
        mutableLongStateOf(defaultSourceWallet?.id ?: 0L)
    }

    val defaultTargetWallet = uiState.wallets.find { it.id == editingTx?.targetWalletId }
        ?: uiState.wallets.firstOrNull { it.id != selectedWalletId }
        ?: uiState.wallets.firstOrNull()
    var targetWalletId by remember(editingTx, uiState.wallets, selectedWalletId) {
        mutableLongStateOf(defaultTargetWallet?.id ?: 0L)
    }

    var adminFee by remember(editingTx) { mutableLongStateOf(editingTx?.adminFee ?: 0L) }

    val availableCategories = remember(txType, uiState.categoryBudgets) {
        when (txType) {
            "EXPENSE" -> uiState.categoryBudgets.filter { it.isExpense }
            "INCOME" -> uiState.categoryBudgets.filter { !it.isExpense && it.categoryId != "transfer" }
            else -> uiState.categoryBudgets.filter { it.categoryId == "transfer" }
        }
    }

    var selectedCategory by remember(txType, availableCategories, editingTx) {
        mutableStateOf(
            availableCategories.find { it.categoryId == editingTx?.categoryId }
                ?: availableCategories.firstOrNull()
                ?: CategoryBudgetEntity("food", "Makanan & Kopi", 2_500_000L, "restaurant", 0xFFF59E0B, true)
        )
    }

    LaunchedEffect(txType, availableCategories) {
        if (availableCategories.isNotEmpty() && availableCategories.none { it.categoryId == selectedCategory.categoryId }) {
            selectedCategory = availableCategories.first()
        }
    }

    var titleInput by remember(editingTx) { mutableStateOf(editingTx?.title ?: "") }
    var noteInput by remember(editingTx) { mutableStateOf(editingTx?.note ?: "") }
    var needVsWant by remember(editingTx, txType) {
        mutableStateOf(
            editingTx?.needVsWant ?: when (txType) {
                "INCOME" -> "PEMASUKAN"
                "TRANSFER" -> "TRANSFER"
                else -> "KEBUTUHAN"
            }
        )
    }
    var dayOffset by remember { mutableIntStateOf(0) } // 0 = Hari Ini, 1 = Kemarin, 2 = 2 Hari Lalu
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val activeColor = when (txType) {
        "INCOME" -> IncomeGreen
        "EXPENSE" -> ExpenseCoral
        else -> TransferIndigo
    }

    val currentAmount = amountString.toLongOrNull() ?: 0L

    fun evaluatePendingCalculation() {
        val op = pendingOperator ?: return
        val second = amountString.toLongOrNull() ?: 0L
        val res = when (op) {
            '+' -> (firstOperand + second).coerceAtLeast(0L)
            '-' -> (firstOperand - second).coerceAtLeast(0L)
            else -> second
        }
        amountString = res.toString()
        pendingOperator = null
        firstOperand = 0L
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("catat_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Segmented Transaction Type Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingTx == null) "Catat Transaksi" else "Ubah Transaksi",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (editingTx != null) {
                        TextButton(onClick = onCancel) {
                            Text("Batal Ubah")
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            Triple("EXPENSE", "Pengeluaran", "tab_expense"),
                            Triple("INCOME", "Pemasukan", "tab_income"),
                            Triple("TRANSFER", "Transfer", "tab_transfer")
                        )
                        tabs.forEach { (code, label, tag) ->
                            val selected = txType == code
                            val tabColor = when (code) {
                                "INCOME" -> IncomeGreen
                                "EXPENSE" -> ExpenseCoral
                                else -> TransferIndigo
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) tabColor else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable {
                                        txType = code
                                        needVsWant = when (code) {
                                            "INCOME" -> "PEMASUKAN"
                                            "TRANSFER" -> "TRANSFER"
                                            else -> "KEBUTUHAN"
                                        }
                                    }
                                    .testTag(tag)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Nominal Display & Quick Chips + Optional Built-in Calculator Numpad
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (pendingOperator != null) {
                                "Hitung: ${CurrencyFormatter.formatRupiah(firstOperand)} $pendingOperator ..."
                            } else {
                                "NOMINAL TRANSAKSI (IDR)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = activeColor.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { showNumpad = !showNumpad }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Kalkulator Numpad",
                                    tint = activeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (showNumpad) "Tutup Numpad" else "Buka Numpad",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = activeColor
                                )
                            }
                        }
                    }

                    Text(
                        text = CurrencyFormatter.formatRupiah(currentAmount),
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = activeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("nominal_display_text")
                    )

                    // Direct numeric field for accessibility / test automation & keyboard users
                    OutlinedTextField(
                        value = amountString,
                        onValueChange = { raw ->
                            amountString = raw.filter { it.isDigit() }.take(12)
                        },
                        label = { Text("Input Nominal Cepat (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input_field")
                    )

                    // Quick Add Nominal Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val quickAdds = listOf(
                            10_000L to "+10rb",
                            25_000L to "+25rb",
                            50_000L to "+50rb",
                            100_000L to "+100rb",
                            500_000L to "+500rb"
                        )
                        items(quickAdds) { (addAmt, label) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        val base = amountString.toLongOrNull() ?: 0L
                                        amountString = (base + addAmt).toString()
                                    }
                                    .testTag("quick_chip_${addAmt}")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Built-in Tactile Calculator Numpad
                    AnimatedVisibility(visible = showNumpad) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            val rows = listOf(
                                listOf("1", "2", "3", "C"),
                                listOf("4", "5", "6", "+"),
                                listOf("7", "8", "9", "-"),
                                listOf("0", "000", "⌫", "=")
                            )
                            rows.forEach { rowKeys ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowKeys.forEach { key ->
                                        val isActionKey = key in listOf("C", "+", "-", "=")
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (key == "=") {
                                                activeColor
                                            } else if (isActionKey) {
                                                activeColor.copy(alpha = 0.14f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clickable {
                                                    when (key) {
                                                        "C" -> {
                                                            amountString = "0"
                                                            pendingOperator = null
                                                            firstOperand = 0L
                                                        }
                                                        "⌫" -> {
                                                            amountString = if (amountString.length > 1) {
                                                                amountString.dropLast(1)
                                                            } else {
                                                                "0"
                                                            }
                                                        }
                                                        "+", "-" -> {
                                                            evaluatePendingCalculation()
                                                            firstOperand = amountString.toLongOrNull() ?: 0L
                                                            pendingOperator = key.first()
                                                            amountString = "0"
                                                        }
                                                        "=" -> evaluatePendingCalculation()
                                                        else -> {
                                                            val next = if (amountString == "0") key else amountString + key
                                                            if (next.length <= 12) {
                                                                amountString = next.trimStart('0').ifEmpty { "0" }
                                                            }
                                                        }
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (key == "⌫") {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                        contentDescription = "Hapus Digit",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = key,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontFamily = JetBrainsMonoFontFamily
                                                        ),
                                                        color = if (key == "=") Color.White else MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Pilih Dompet Asal (& Dompet Tujuan if TRANSFER)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (txType == "TRANSFER") "Dompet Asal (Sumber Dana)" else "Pilih Dompet",
                        style = MaterialTheme.typography.titleSmall
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.wallets, key = { it.id }) { wallet ->
                            val isSelected = wallet.id == selectedWalletId
                            val wColor = CurrencyFormatter.getColorFromHex(wallet.colorHex)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) wColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { selectedWalletId = wallet.id }
                                    .testTag("select_source_wallet_${wallet.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = CurrencyFormatter.getIconForKey(wallet.iconKey),
                                        contentDescription = wallet.name,
                                        tint = if (isSelected) Color.White else wColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = wallet.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatCompactRupiah(wallet.balance),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (txType == "TRANSFER") {
                        Text(
                            text = "Dompet Tujuan (Penerima)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.wallets.filter { it.id != selectedWalletId }, key = { it.id }) { wallet ->
                                val isSelected = wallet.id == targetWalletId
                                val wColor = CurrencyFormatter.getColorFromHex(wallet.colorHex)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) wColor else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { targetWalletId = wallet.id }
                                        .testTag("select_target_wallet_${wallet.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = CurrencyFormatter.getIconForKey(wallet.iconKey),
                                            contentDescription = wallet.name,
                                            tint = if (isSelected) Color.White else wColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = wallet.name,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = CurrencyFormatter.formatCompactRupiah(wallet.balance),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Biaya Admin Transfer",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0L to "Gratis", 1_000L to "Rp 1.000", 2_500L to "Rp 2.500", 6_500L to "Rp 6.500").forEach { (fee, label) ->
                                FilterChip(
                                    selected = adminFee == fee,
                                    onClick = { adminFee = fee },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Pilih Kategori (If EXPENSE or INCOME)
        if (txType != "TRANSFER") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pilih Kategori", style = MaterialTheme.typography.titleSmall)
                            TextButton(onClick = { showAddCategoryDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Kategori Baru", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Kategori Baru")
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableCategories.forEach { cat ->
                                val isSelected = cat.categoryId == selectedCategory.categoryId
                                val cColor = CurrencyFormatter.getColorFromHex(cat.colorHex)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) cColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) cColor else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .testTag("category_chip_${cat.categoryId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = CurrencyFormatter.getIconForKey(cat.iconKey),
                                            contentDescription = cat.categoryName,
                                            tint = cColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = cat.categoryName,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Detail Transaksi, Prioritas Kebutuhan/Keinginan & Tanggal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Transaksi (mis. Makan Siang, Kopi, Gaji)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_title_input")
                    )

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Catatan Tambahan (Opsional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_note_input")
                    )

                    if (txType == "EXPENSE") {
                        Text(
                            text = "Klasifikasi Pengeluaran",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("KEBUTUHAN" to "Kebutuhan Pokok", "KEINGINAN" to "Keinginan / Gaya Hidup", "TABUNGAN" to "Investasi / Amal").forEach { (code, label) ->
                                FilterChip(
                                    selected = needVsWant == code,
                                    onClick = { needVsWant = code },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = activeColor.copy(alpha = 0.15f),
                                        selectedLabelColor = activeColor
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "Waktu Transaksi",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Hari Ini", 1 to "Kemarin", 2 to "2 Hari Lalu").forEach { (offset, label) ->
                            FilterChip(
                                selected = dayOffset == offset,
                                onClick = { dayOffset = offset },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        // 6. Save Transaction Button
        item {
            Button(
                onClick = {
                    evaluatePendingCalculation()
                    val finalAmount = (amountString.toLongOrNull() ?: 0L).coerceAtLeast(1_000L)
                    val sourceWallet = uiState.wallets.find { it.id == selectedWalletId } ?: uiState.wallets.firstOrNull()
                    val targetWallet = uiState.wallets.find { it.id == targetWalletId }
                    if (sourceWallet != null) {
                        val catId = if (txType == "TRANSFER") "transfer" else selectedCategory.categoryId
                        val catName = if (txType == "TRANSFER") "Transfer Antar Dompet" else selectedCategory.categoryName
                        val cleanTitle = titleInput.trim().ifEmpty {
                            if (txType == "TRANSFER" && targetWallet != null) {
                                "Transfer ke ${targetWallet.name}"
                            } else {
                                catName
                            }
                        }
                        val txTime = editingTx?.timestamp
                            ?: (System.currentTimeMillis() - dayOffset * 24 * 3_600_000L)

                        onSaveTransaction(
                            editingTx?.id ?: 0L,
                            cleanTitle,
                            finalAmount,
                            txType,
                            catId,
                            catName,
                            sourceWallet.id,
                            sourceWallet.name,
                            targetWallet?.id,
                            targetWallet?.name,
                            adminFee,
                            txTime,
                            noteInput,
                            needVsWant
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_transaction_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (editingTx == null) "Simpan ke Dompet Offline" else "Simpan Perubahan Transaksi",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(72.dp))
        }
    }

    if (showAddCategoryDialog) {
        AddCustomCategoryDialog(
            isExpense = txType == "EXPENSE",
            onDismiss = { showAddCategoryDialog = false },
            onSave = { id, name, limit, iconKey, colorHex, isExp ->
                onAddCustomCategory(id, name, limit, iconKey, colorHex, isExp)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun AddCustomCategoryDialog(
    isExpense: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, String, Long, Boolean) -> Unit
) {
    var catName by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf(if (isExpense) "1000000" else "0") }
    var selectedColor by remember { mutableLongStateOf(0xFF10B981L) }

    val colors = listOf(0xFF10B981L, 0xFFF59E0BL, 0xFF3B82F6L, 0xFFEC4899L, 0xFF8B5CF6L, 0xFFF43F5EL)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kategori Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = catName,
                    onValueChange = { catName = it },
                    label = { Text("Nama Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isExpense) {
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                        label = { Text("Batas Anggaran Bulanan (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CurrencyFormatter.getColorFromHex(hex))
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = catName.trim().ifEmpty { "Kategori Baru" }
                    val id = clean.lowercase().replace(Regex("[^a-z0-9]"), "_").take(18) + "_" + (System.currentTimeMillis() % 1000)
                    val limit = limitText.toLongOrNull() ?: 0L
                    onSave(id, clean, limit, "category", selectedColor, isExpense)
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
