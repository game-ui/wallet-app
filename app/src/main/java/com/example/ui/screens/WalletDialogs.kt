package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TransferIndigo
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOrEditWalletDialog(
    initialWallet: WalletEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        type: String,
        accountNumber: String,
        balance: Long,
        colorHex: Long,
        iconKey: String,
        isMain: Boolean
    ) -> Unit,
    onDelete: ((WalletEntity) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialWallet?.name ?: "") }
    var type by remember { mutableStateOf(initialWallet?.type ?: "BANK") }
    var accountNumber by remember { mutableStateOf(initialWallet?.accountNumber ?: "") }
    var balanceText by remember { mutableStateOf(initialWallet?.balance?.toString() ?: "0") }
    var selectedColor by remember { mutableLongStateOf(initialWallet?.colorHex ?: 0xFF065F46L) }
    var isMain by remember { mutableStateOf(initialWallet?.isMain ?: false) }

    val walletTypes = listOf(
        Triple("BANK", "Rekening Bank", "bank"),
        Triple("EWALLET", "E-Wallet / QRIS", "ewallet"),
        Triple("CASH", "Dompet Tunai", "cash"),
        Triple("SAVINGS", "Tabungan / Investasi", "savings")
    )

    val colorChoices = listOf(
        0xFF065F46L, // Deep Emerald
        0xFF0284C7L, // Ocean Blue
        0xFFD97706L, // Warm Gold
        0xFF4F46E5L, // Royal Indigo
        0xFFBE123CL, // Crimson Rose
        0xFF0F172AL, // Midnight Slate
        0xFF7C3AEDL  // Violet
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialWallet == null) "Tambah Dompet Baru" else "Kelola Dompet",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Dompet (mis. BCA, GoPay, Tunai)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_name_input")
                )

                Text(
                    text = "Jenis Dompet",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    walletTypes.forEach { (typeCode, label, _) ->
                        FilterChip(
                            selected = type == typeCode,
                            onClick = { type = typeCode },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("No. Rekening / Ket. (mis. •••• 4829)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        balanceText = input.filter { it.isDigit() }
                    },
                    label = { Text("Saldo Saat Ini (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_balance_input")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(500_000L to "+500rb", 1_000_000L to "+1Jt", 5_000_000L to "+5Jt").forEach { (addVal, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = balanceText.toLongOrNull() ?: 0L
                                    balanceText = (current + addVal).toString()
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = "Warna Kartu Dompet",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorChoices.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CurrencyFormatter.getColorFromHex(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Warna terpilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Jadikan Dompet Utama", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Dipilih otomatis saat mencatat transaksi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isMain, onCheckedChange = { isMain = it })
                }

                if (initialWallet != null && onDelete != null) {
                    HorizontalDivider()
                    OutlinedButton(
                        onClick = {
                            onDelete(initialWallet)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Dompet")
                        Spacer(Modifier.width(8.dp))
                        Text("Hapus Dompet Ini")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim().ifEmpty { "Dompet Baru" }
                    val iconKey = walletTypes.find { it.first == type }?.third ?: "wallet"
                    val parsedBalance = balanceText.toLongOrNull() ?: 0L
                    onSave(
                        initialWallet?.id ?: 0L,
                        cleanName,
                        type,
                        accountNumber.trim().ifEmpty { "Rekening Lokal" },
                        parsedBalance,
                        selectedColor,
                        iconKey,
                        isMain
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_wallet_confirm_button")
            ) {
                Text("Simpan Dompet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    val accentColor = when (transaction.type) {
        "INCOME" -> IncomeGreen
        "EXPENSE" -> ExpenseCoral
        else -> TransferIndigo
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CurrencyFormatter.getIconForKey(transaction.categoryId),
                        contentDescription = transaction.categoryName,
                        tint = accentColor
                    )
                }
                Column {
                    Text(transaction.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = transaction.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = CurrencyFormatter.formatSignedRupiah(transaction.amount, transaction.type),
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        if (transaction.adminFee > 0L) {
                            Text(
                                text = "+ Biaya Admin ${CurrencyFormatter.formatRupiah(transaction.adminFee)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                DetailRow("Waktu Transaksi", CurrencyFormatter.formatDateTime(transaction.timestamp))
                DetailRow(
                    "Dompet",
                    if (transaction.type == "TRANSFER" && transaction.targetWalletName != null) {
                        "${transaction.walletName} → ${transaction.targetWalletName}"
                    } else {
                        transaction.walletName
                    }
                )
                DetailRow("Klasifikasi", transaction.needVsWant)
                if (transaction.note.isNotBlank()) {
                    DetailRow("Catatan", transaction.note)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onDelete(transaction)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_transaction_button")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus")
                }
                Button(
                    onClick = {
                        onEdit(transaction)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("edit_transaction_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Ubah", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ubah")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProfileAndOfflineBackupDialog(
    userName: String,
    userSubtitle: String,
    isDarkTheme: Boolean,
    jsonBackupPreview: String,
    onDismiss: () -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onToggleDarkTheme: () -> Unit,
    onResetDemoData: () -> Unit
) {
    var showBackupJson by remember { mutableStateOf(false) }
    var copiedToast by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = "Mode Offline",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Pengaturan & Backup Offline", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Seluruh data dompet, transaksi, anggaran, dan PDF disimpan 100% secara lokal di perangkat Anda (Room SQLite) tanpa koneksi internet atau server eksternal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = "Tema Gelap")
                        Text("Mode Gelap (Dark Mode)", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = isDarkTheme, onCheckedChange = { onToggleDarkTheme() })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showBackupJson = !showBackupJson },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (showBackupJson) "Sembunyikan JSON" else "Lihat Backup JSON")
                    }
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("DanaFlow Backup", jsonBackupPreview))
                            copiedToast = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Salin JSON", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (copiedToast) "Tersalin!" else "Salin JSON")
                    }
                }

                if (showBackupJson) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = jsonBackupPreview.take(800) + if (jsonBackupPreview.length > 800) "\n... (tersalin lengkap ke clipboard)" else "",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        onResetDemoData()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = "Reset Data Contoh", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pulihkan Data Contoh Awal")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Selesai")
            }
        }
    )
}
