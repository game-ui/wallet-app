package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {
    private val indoLocale = Locale("id", "ID")

    fun formatRupiah(amount: Long, hidden: Boolean = false): String {
        if (hidden) return "Rp ••••••••"
        val formatter = NumberFormat.getNumberInstance(indoLocale)
        val prefix = if (amount < 0) "-Rp " else "Rp "
        return prefix + formatter.format(abs(amount))
    }

    fun formatSignedRupiah(amount: Long, type: String, hidden: Boolean = false): String {
        if (hidden) return "Rp ••••••"
        val formatter = NumberFormat.getNumberInstance(indoLocale)
        val sign = when (type) {
            "INCOME" -> "+Rp "
            "EXPENSE" -> "-Rp "
            else -> "Rp "
        }
        return sign + formatter.format(abs(amount))
    }

    fun formatCompactRupiah(amount: Long, hidden: Boolean = false): String {
        if (hidden) return "Rp ••••"
        val absVal = abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            absVal >= 1_000_000_000L -> String.format(indoLocale, "%sRp %.1f M", sign, absVal / 1_000_000_000.0)
            absVal >= 1_000_000L -> String.format(indoLocale, "%sRp %.1f Jt", sign, absVal / 1_000_000.0)
            absVal >= 1_000L -> String.format(indoLocale, "%sRp %d Rb", sign, absVal / 1_000L)
            else -> "${sign}Rp $absVal"
        }
    }

    fun formatDateTime(timestamp: Long): String {
        val nowCal = Calendar.getInstance()
        val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val timeFmt = SimpleDateFormat("HH:mm", indoLocale)
        val dateFmt = SimpleDateFormat("dd MMM yyyy", indoLocale)

        val sameYear = nowCal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR)
        val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)
        val txDay = txCal.get(Calendar.DAY_OF_YEAR)

        return when {
            sameYear && nowDay == txDay -> "Hari ini • ${timeFmt.format(Date(timestamp))}"
            sameYear && nowDay - txDay == 1 -> "Kemarin • ${timeFmt.format(Date(timestamp))}"
            else -> "${dateFmt.format(Date(timestamp))} • ${timeFmt.format(Date(timestamp))}"
        }
    }

    fun formatShortDate(timestamp: Long): String {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", indoLocale)
        return dateFmt.format(Date(timestamp))
    }

    fun formatDayMonth(timestamp: Long): String {
        val dateFmt = SimpleDateFormat("dd MMM", indoLocale)
        return dateFmt.format(Date(timestamp))
    }

    fun getIconForKey(iconKey: String): ImageVector {
        return when (iconKey.lowercase()) {
            "bank" -> Icons.Filled.AccountBalance
            "ewallet" -> Icons.Filled.QrCode2
            "cash" -> Icons.Filled.Payments
            "savings" -> Icons.Filled.Savings
            "restaurant", "food" -> Icons.Filled.Restaurant
            "fastfood" -> Icons.Filled.Fastfood
            "commute", "transport" -> Icons.Filled.Commute
            "bolt", "bills" -> Icons.Filled.Bolt
            "shopping" -> Icons.Filled.ShoppingBag
            "movie", "entertainment" -> Icons.Filled.Movie
            "health" -> Icons.Filled.LocalHospital
            "school", "education" -> Icons.Filled.School
            "work", "salary" -> Icons.Filled.Work
            "laptop", "freelance" -> Icons.Filled.Computer
            "trending_up", "investment" -> Icons.AutoMirrored.Filled.TrendingUp
            "swap", "transfer" -> Icons.AutoMirrored.Filled.CompareArrows
            "wallet" -> Icons.Filled.AccountBalanceWallet
            else -> Icons.Filled.Category
        }
    }

    fun getColorFromHex(hex: Long): Color {
        val argb = if ((hex and 0xFF000000L) == 0L) (hex or 0xFF000000L) else hex
        return Color(argb)
    }

    fun getGreetingForHour(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }
}
