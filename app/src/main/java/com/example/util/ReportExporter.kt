package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.CategoryBudgetEntity
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportResult(
    val file: File,
    val fileSizeKb: Int,
    val reportTitle: String
)

object ReportExporter {

    fun generatePdfReport(
        context: Context,
        userName: String,
        periodLabel: String,
        walletFilterLabel: String,
        wallets: List<WalletEntity>,
        transactions: List<TransactionEntity>,
        categoryBudgets: List<CategoryBudgetEntity>,
        includeExecutiveSummary: Boolean = true,
        includeWalletBreakdown: Boolean = true,
        includeCategoryAnalysis: Boolean = true,
        includeTransactionTable: Boolean = true
    ): ExportResult {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in PostScript points
        val pageHeight = 842 // Standard A4 height in PostScript points

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#D1FAE5")
            textSize = 10f
        }
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#065F46")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#0F172A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#334155")
            textSize = 9.5f
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#64748B")
            textSize = 8.5f
        }
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        // Draw Header Banner
        boxPaint.color = AndroidColor.parseColor("#064E3B")
        canvas.drawRoundRect(RectF(32f, 28f, (pageWidth - 32).toFloat(), 112f), 14f, 14f, boxPaint)

        canvas.drawText("DANAFLOW — LAPORAN KEUANGAN OFFLINE", 48f, 56f, titlePaint)
        val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        canvas.drawText("Periode: $periodLabel  |  Dompet: $walletFilterLabel", 48f, 76f, subtitlePaint)
        canvas.drawText("Dicetak secara lokal (100% Offline) pada $dateStr", 48f, 94f, subtitlePaint)

        var yPos = 136f

        fun startNewPageIfNeeded(requiredHeight: Float) {
            if (yPos + requiredHeight > pageHeight - 48f) {
                canvas.drawText(
                    "DanaFlow Offline Financial Statement • Halaman $pageNumber",
                    32f,
                    (pageHeight - 22).toFloat(),
                    mutedPaint
                )
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 44f
            }
        }

        val totalBalance = wallets.sumOf { it.balance }
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netCashflow = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((netCashflow.toDouble() / totalIncome.toDouble()) * 100).toInt() else 0

        // 1. Executive Summary
        if (includeExecutiveSummary) {
            canvas.drawText("1. RINGKASAN EKSEKUTIF ARUS KAS", 32f, yPos, sectionTitlePaint)
            yPos += 12f

            val cardWidth = (pageWidth - 64f - 24f) / 3f
            val cards = listOf(
                Triple("TOTAL PEMASUKAN", CurrencyFormatter.formatRupiah(totalIncome), "#ECFDF5" to "#059669"),
                Triple("TOTAL PENGELUARAN", CurrencyFormatter.formatRupiah(totalExpense), "#FFF1F2" to "#E11D48"),
                Triple("ARUS KAS BERSIH", "${CurrencyFormatter.formatRupiah(netCashflow)} ($savingsRate%)", "#EFF6FF" to "#1D4ED8")
            )

            cards.forEachIndexed { index, (label, value, colors) ->
                val left = 32f + index * (cardWidth + 12f)
                boxPaint.color = AndroidColor.parseColor(colors.first)
                canvas.drawRoundRect(RectF(left, yPos, left + cardWidth, yPos + 54f), 8f, 8f, boxPaint)

                canvas.drawText(label, left + 10f, yPos + 18f, mutedPaint)
                val valPaint = Paint(bodyBoldPaint).apply {
                    color = AndroidColor.parseColor(colors.second)
                    textSize = 11f
                }
                canvas.drawText(value, left + 10f, yPos + 38f, valPaint)
            }
            yPos += 74f
        }

        // 2. Multi-Wallet Breakdown
        if (includeWalletBreakdown && wallets.isNotEmpty()) {
            startNewPageIfNeeded(40f + wallets.size * 22f)
            canvas.drawText("2. POSISI SALDO MULTI-DOMPET (TOTAL: ${CurrencyFormatter.formatRupiah(totalBalance)})", 32f, yPos, sectionTitlePaint)
            yPos += 10f

            boxPaint.color = AndroidColor.parseColor("#F1F5F9")
            canvas.drawRect(32f, yPos, (pageWidth - 32).toFloat(), yPos + 20f, boxPaint)
            canvas.drawText("Nama Dompet", 40f, yPos + 14f, bodyBoldPaint)
            canvas.drawText("Tipe", 220f, yPos + 14f, bodyBoldPaint)
            canvas.drawText("Nomor / Ket.", 310f, yPos + 14f, bodyBoldPaint)
            canvas.drawText("Saldo Saat Ini", 450f, yPos + 14f, bodyBoldPaint)
            yPos += 20f

            wallets.forEach { wallet ->
                canvas.drawText(wallet.name, 40f, yPos + 14f, bodyPaint)
                canvas.drawText(wallet.type, 220f, yPos + 14f, bodyPaint)
                canvas.drawText(wallet.accountNumber, 310f, yPos + 14f, mutedPaint)
                canvas.drawText(CurrencyFormatter.formatRupiah(wallet.balance), 450f, yPos + 14f, bodyBoldPaint)
                yPos += 20f
                canvas.drawLine(32f, yPos, (pageWidth - 32).toFloat(), yPos, linePaint)
            }
            yPos += 18f
        }

        // 3. Category Expense & Budget Breakdown
        if (includeCategoryAnalysis) {
            val expenseByCategory = transactions
                .filter { it.type == "EXPENSE" }
                .groupBy { it.categoryId to it.categoryName }
                .map { (catPair, list) ->
                    val sum = list.sumOf { it.amount }
                    val budget = categoryBudgets.find { it.categoryId == catPair.first }?.monthlyLimit ?: 0L
                    Triple(catPair.second, sum, budget)
                }
                .sortedByDescending { it.second }

            if (expenseByCategory.isNotEmpty()) {
                startNewPageIfNeeded(44f + expenseByCategory.size * 20f)
                canvas.drawText("3. ANALISIS PENGELUARAN PER KATEGORI & ANGGARAN", 32f, yPos, sectionTitlePaint)
                yPos += 10f

                boxPaint.color = AndroidColor.parseColor("#F1F5F9")
                canvas.drawRect(32f, yPos, (pageWidth - 32).toFloat(), yPos + 20f, boxPaint)
                canvas.drawText("Kategori", 40f, yPos + 14f, bodyBoldPaint)
                canvas.drawText("Realisasi", 230f, yPos + 14f, bodyBoldPaint)
                canvas.drawText("Batas Anggaran", 350f, yPos + 14f, bodyBoldPaint)
                canvas.drawText("Porsi / Status", 470f, yPos + 14f, bodyBoldPaint)
                yPos += 20f

                expenseByCategory.forEach { (catName, spent, limit) ->
                    val sharePct = if (totalExpense > 0) ((spent * 100) / totalExpense) else 0
                    val budgetStatus = if (limit > 0) "${(spent * 100) / limit}% dari batas" else "$sharePct% total"
                    canvas.drawText(catName, 40f, yPos + 14f, bodyPaint)
                    canvas.drawText(CurrencyFormatter.formatRupiah(spent), 230f, yPos + 14f, bodyBoldPaint)
                    canvas.drawText(if (limit > 0) CurrencyFormatter.formatRupiah(limit) else "-", 350f, yPos + 14f, mutedPaint)
                    canvas.drawText(budgetStatus, 470f, yPos + 14f, bodyPaint)
                    yPos += 20f
                    canvas.drawLine(32f, yPos, (pageWidth - 32).toFloat(), yPos, linePaint)
                }
                yPos += 18f
            }
        }

        // 4. Full Transaction History Table
        if (includeTransactionTable) {
            startNewPageIfNeeded(56f)
            canvas.drawText("4. RINCIAN TRANSAKSI (${transactions.size} Transaksi)", 32f, yPos, sectionTitlePaint)
            yPos += 10f

            boxPaint.color = AndroidColor.parseColor("#F1F5F9")
            canvas.drawRect(32f, yPos, (pageWidth - 32).toFloat(), yPos + 20f, boxPaint)
            canvas.drawText("Tanggal", 38f, yPos + 14f, bodyBoldPaint)
            canvas.drawText("Deskripsi Transaksi", 110f, yPos + 14f, bodyBoldPaint)
            canvas.drawText("Kategori / Dompet", 290f, yPos + 14f, bodyBoldPaint)
            canvas.drawText("Nominal", 455f, yPos + 14f, bodyBoldPaint)
            yPos += 20f

            transactions.forEach { tx ->
                startNewPageIfNeeded(24f)
                val shortDate = CurrencyFormatter.formatShortDate(tx.timestamp)
                val titleTrunc = if (tx.title.length > 28) tx.title.take(26) + "…" else tx.title
                val walletDesc = if (tx.type == "TRANSFER" && tx.targetWalletName != null) {
                    "${tx.walletName} → ${tx.targetWalletName}"
                } else {
                    "${tx.categoryName} • ${tx.walletName}"
                }
                val walletTrunc = if (walletDesc.length > 26) walletDesc.take(24) + "…" else walletDesc

                canvas.drawText(shortDate, 38f, yPos + 14f, mutedPaint)
                canvas.drawText(titleTrunc, 110f, yPos + 14f, bodyPaint)
                canvas.drawText(walletTrunc, 290f, yPos + 14f, mutedPaint)

                val amountPaint = Paint(bodyBoldPaint).apply {
                    color = when (tx.type) {
                        "INCOME" -> AndroidColor.parseColor("#059669")
                        "EXPENSE" -> AndroidColor.parseColor("#E11D48")
                        else -> AndroidColor.parseColor("#4F46E5")
                    }
                }
                canvas.drawText(CurrencyFormatter.formatSignedRupiah(tx.amount, tx.type), 455f, yPos + 14f, amountPaint)
                yPos += 20f
                canvas.drawLine(32f, yPos, (pageWidth - 32).toFloat(), yPos, linePaint)
            }
        }

        // Footer on final page
        canvas.drawText(
            "DanaFlow Offline Financial Statement • Halaman $pageNumber",
            32f,
            (pageHeight - 22).toFloat(),
            mutedPaint
        )
        pdfDocument.finishPage(page)

        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val safePeriod = periodLabel.replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = "Laporan_DanaFlow_${safePeriod}_${System.currentTimeMillis() % 100000}.pdf"
        val outFile = File(reportsDir, fileName)

        FileOutputStream(outFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val sizeKb = (outFile.length() / 1024L).toInt().coerceAtLeast(1)
        return ExportResult(
            file = outFile,
            fileSizeKb = sizeKb,
            reportTitle = "Laporan PDF - $periodLabel"
        )
    }

    fun generateCsvReport(
        context: Context,
        periodLabel: String,
        transactions: List<TransactionEntity>
    ): ExportResult {
        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val safePeriod = periodLabel.replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = "Transaksi_DanaFlow_${safePeriod}_${System.currentTimeMillis() % 100000}.csv"
        val outFile = File(reportsDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("ID,Tanggal,Judul,Tipe,Kategori,Dompet Asal,Dompet Tujuan,Nominal (Rp),Biaya Admin (Rp),Klasifikasi,Catatan")
        transactions.forEach { tx ->
            val dateStr = CurrencyFormatter.formatShortDate(tx.timestamp)
            val cleanTitle = tx.title.replace(",", " ")
            val cleanNote = tx.note.replace(",", " ")
            sb.appendLine(
                "${tx.id},$dateStr,$cleanTitle,${tx.type},${tx.categoryName},${tx.walletName},${tx.targetWalletName ?: "-"},${tx.amount},${tx.adminFee},${tx.needVsWant},$cleanNote"
            )
        }
        outFile.writeText(sb.toString())
        val sizeKb = (outFile.length() / 1024L).toInt().coerceAtLeast(1)
        return ExportResult(
            file = outFile,
            fileSizeKb = sizeKb,
            reportTitle = "Data CSV - $periodLabel"
        )
    }

    fun generateJsonBackup(
        userName: String,
        wallets: List<WalletEntity>,
        transactions: List<TransactionEntity>,
        budgets: List<CategoryBudgetEntity>
    ): String {
        val root = JSONObject()
        root.put("app", "DanaFlow Offline")
        root.put("version", 1)
        root.put("userName", userName)
        root.put("exportedAt", System.currentTimeMillis())

        val walletsArr = JSONArray()
        wallets.forEach { w ->
            walletsArr.put(
                JSONObject().apply {
                    put("id", w.id)
                    put("name", w.name)
                    put("type", w.type)
                    put("accountNumber", w.accountNumber)
                    put("balance", w.balance)
                    put("colorHex", w.colorHex)
                    put("iconKey", w.iconKey)
                    put("isMain", w.isMain)
                }
            )
        }
        root.put("wallets", walletsArr)

        val txArr = JSONArray()
        transactions.forEach { tx ->
            txArr.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("title", tx.title)
                    put("amount", tx.amount)
                    put("type", tx.type)
                    put("categoryId", tx.categoryId)
                    put("categoryName", tx.categoryName)
                    put("walletId", tx.walletId)
                    put("walletName", tx.walletName)
                    put("timestamp", tx.timestamp)
                    put("note", tx.note)
                    put("needVsWant", tx.needVsWant)
                }
            )
        }
        root.put("transactions", txArr)
        root.put("budgetCount", budgets.size)
        return root.toString(2)
    }

    fun shareExportedFile(context: Context, filePath: String, format: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = if (format.equals("PDF", ignoreCase = true)) "application/pdf" else "text/csv"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan DanaFlow (${file.name})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Bagikan Laporan DanaFlow").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }
}
