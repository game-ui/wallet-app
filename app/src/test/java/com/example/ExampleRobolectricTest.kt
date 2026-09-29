package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CategoryBudgetEntity
import com.example.data.TransactionEntity
import com.example.data.WalletEntity
import com.example.util.CurrencyFormatter
import com.example.util.ReportExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DanaFlow", appName)
    }

    @Test
    fun `currency formatter formats rupiah and masked values`() {
        assertEquals("Rp 12.450.000", CurrencyFormatter.formatRupiah(12_450_000L, hidden = false))
        assertEquals("Rp ••••••••", CurrencyFormatter.formatRupiah(12_450_000L, hidden = true))
        assertEquals("+Rp 500.000", CurrencyFormatter.formatSignedRupiah(500_000L, "INCOME"))
        assertEquals("-Rp 68.000", CurrencyFormatter.formatSignedRupiah(68_000L, "EXPENSE"))
    }

    @Test
    fun `offline pdf and csv exporter generates real files`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val wallets = listOf(
            WalletEntity(1L, "BCA Utama", "BANK", "4829", 5_000_000L, 0xFF065F46, "bank", true)
        )
        val txs = listOf(
            TransactionEntity(
                id = 1L,
                title = "Makan Siang",
                amount = 45_000L,
                type = "EXPENSE",
                categoryId = "food",
                categoryName = "Makanan & Kopi",
                walletId = 1L,
                walletName = "BCA Utama"
            )
        )
        val budgets = listOf(
            CategoryBudgetEntity("food", "Makanan & Kopi", 1_500_000L, "restaurant", 0xFFF59E0B, true)
        )

        val pdfResult = ReportExporter.generatePdfReport(
            context = context,
            userName = "Bima Pradana",
            periodLabel = "30 Hari Terakhir",
            walletFilterLabel = "Semua Dompet",
            wallets = wallets,
            transactions = txs,
            categoryBudgets = budgets
        )
        assertTrue(pdfResult.file.exists())
        assertTrue(pdfResult.file.length() > 0L)

        val csvResult = ReportExporter.generateCsvReport(
            context = context,
            periodLabel = "30 Hari Terakhir",
            transactions = txs
        )
        assertTrue(csvResult.file.exists())
        assertTrue(csvResult.file.readText().contains("Makan Siang"))
    }
}
