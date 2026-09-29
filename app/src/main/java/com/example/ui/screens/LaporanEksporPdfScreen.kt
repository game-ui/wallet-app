package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ExportReportEntity
import com.example.data.TransactionEntity
import com.example.ui.DanaFlowUiState
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.CurrencyFormatter
import com.example.util.ReportExporter

@Composable
fun LaporanEksporPdfScreen(
    uiState: DanaFlowUiState,
    onSelectPeriod: (String) -> Unit,
    onSelectWalletFilter: (Long?) -> Unit,
    onExportPdf: (
        periodLabel: String,
        walletFilterLabel: String,
        transactionsToExport: List<TransactionEntity>,
        includeExecutiveSummary: Boolean,
        includeWalletBreakdown: Boolean,
        includeCategoryAnalysis: Boolean,
        includeTransactionTable: Boolean,
        shareImmediately: Boolean
    ) -> Unit,
    onExportCsv: (
        periodLabel: String,
        walletFilterLabel: String,
        transactionsToExport: List<TransactionEntity>,
        shareImmediately: Boolean
    ) -> Unit,
    onDeleteReport: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var includeSummary by remember { mutableStateOf(true) }
    var includeWallets by remember { mutableStateOf(true) }
    var includeCategories by remember { mutableStateOf(true) }
    var includeTransactions by remember { mutableStateOf(true) }

    val periodLabel = when (uiState.selectedPeriod) {
        "MINGGU_INI" -> "7 Hari Terakhir"
        "BULAN_INI" -> "30 Hari Terakhir"
        "TIGA_BULAN" -> "3 Bulan Terakhir"
        else -> "Semua Waktu"
    }

    val selectedWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
    val walletFilterLabel = selectedWallet?.name ?: "Semua Dompet"
    val netFlow = uiState.periodIncome - uiState.periodExpense

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("laporan_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Laporan & Ekspor PDF",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Cetak laporan keuangan PDF profesional atau spreadsheet CSV langsung dari database lokal Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Filter Periode & Dompet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1. Pilih Periode & Cakupan Dompet", style = MaterialTheme.typography.titleSmall)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val periods = listOf(
                            "MINGGU_INI" to "7 Hari",
                            "BULAN_INI" to "30 Hari",
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
                                )
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.selectedWalletId == null,
                                onClick = { onSelectWalletFilter(null) },
                                label = { Text("Semua Dompet (${uiState.wallets.size})") }
                            )
                        }
                        items(uiState.wallets, key = { it.id }) { wallet ->
                            FilterChip(
                                selected = uiState.selectedWalletId == wallet.id,
                                onClick = { onSelectWalletFilter(wallet.id) },
                                label = { Text(wallet.name) }
                            )
                        }
                    }

                    HorizontalDivider()

                    Text("2. Komponen Dokumen PDF", style = MaterialTheme.typography.titleSmall)

                    ReportSectionToggleRow(
                        label = "Ringkasan Eksekutif Arus Kas",
                        checked = includeSummary,
                        onCheckedChange = { includeSummary = it }
                    )
                    ReportSectionToggleRow(
                        label = "Tabel Posisi Saldo Multi-Dompet",
                        checked = includeWallets,
                        onCheckedChange = { includeWallets = it }
                    )
                    ReportSectionToggleRow(
                        label = "Analisis Kategori & Batas Anggaran",
                        checked = includeCategories,
                        onCheckedChange = { includeCategories = it }
                    )
                    ReportSectionToggleRow(
                        label = "Rincian Riwayat Transaksi (${uiState.periodTransactions.size} baris)",
                        checked = includeTransactions,
                        onCheckedChange = { includeTransactions = it }
                    )
                }
            }
        }

        // 3. Live Paper Statement Preview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldDeep)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PRATINJAU DOKUMEN LAPORAN",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFA7F3D0)
                                )
                                Text(
                                    text = "DanaFlow Financial Statement",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$periodLabel • $walletFilterLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD1FAE5)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF6EE7B7),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "A4 PDF",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PreviewRow(
                            label = "Total Saldo Multi-Dompet",
                            value = CurrencyFormatter.formatRupiah(uiState.totalCombinedBalance, uiState.isBalanceHidden),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        PreviewRow(
                            label = "Total Pemasukan ($periodLabel)",
                            value = "+${CurrencyFormatter.formatRupiah(uiState.periodIncome, uiState.isBalanceHidden)}",
                            valueColor = IncomeGreen
                        )
                        PreviewRow(
                            label = "Total Pengeluaran ($periodLabel)",
                            value = "-${CurrencyFormatter.formatRupiah(uiState.periodExpense, uiState.isBalanceHidden)}",
                            valueColor = ExpenseCoral
                        )
                        HorizontalDivider()
                        PreviewRow(
                            label = "Arus Kas Bersih (Net Cashflow)",
                            value = CurrencyFormatter.formatRupiah(netFlow, uiState.isBalanceHidden),
                            valueColor = if (netFlow >= 0) IncomeGreen else ExpenseCoral
                        )
                        PreviewRow(
                            label = "Jumlah Transaksi Tercatat",
                            value = "${uiState.periodTransactions.size} transaksi",
                            valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Primary Export Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        onExportPdf(
                            periodLabel,
                            walletFilterLabel,
                            uiState.periodTransactions,
                            includeSummary,
                            includeWallets,
                            includeCategories,
                            includeTransactions,
                            true
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("export_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Cetak & Bagikan Laporan PDF",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onExportPdf(
                                periodLabel,
                                walletFilterLabel,
                                uiState.periodTransactions,
                                includeSummary,
                                includeWallets,
                                includeCategories,
                                includeTransactions,
                                false
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_pdf_only_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan PDF")
                    }

                    OutlinedButton(
                        onClick = {
                            onExportCsv(
                                periodLabel,
                                walletFilterLabel,
                                uiState.periodTransactions,
                                false
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_csv_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan CSV")
                    }
                }
            }
        }

        // 5. Saved Export Reports History (from Room DB)
        item {
            Text(
                text = "Arsip Laporan Tersimpan (${uiState.exportReports.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (uiState.exportReports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Belum Ada Arsip Laporan",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Setiap kali Anda mengekspor PDF atau CSV, file akan tersimpan secara offline di perangkat dan muncul di daftar ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.exportReports, key = { it.id }) { report ->
                SavedReportItemCard(
                    report = report,
                    onShareClick = {
                        ReportExporter.shareExportedFile(context, report.filePath, report.format)
                    },
                    onDeleteClick = { onDeleteReport(report.id) }
                )
            }
        }

        item {
            Spacer(Modifier.height(76.dp))
        }
    }
}

@Composable
private fun ReportSectionToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PreviewRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SavedReportItemCard(
    report: ExportReportEntity,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isPdf = report.format.equals("PDF", ignoreCase = true)
    val badgeColor = if (isPdf) ExpenseCoral else IncomeGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_report_${report.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                    contentDescription = report.format,
                    tint = badgeColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.reportTitle,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${report.walletFilterLabel} • ${report.transactionCount} transaksi • ${report.fileSizeKb} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatDateTime(report.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Bagikan Laporan",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.testTag("delete_report_${report.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Hapus Laporan",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
