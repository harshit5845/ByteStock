package com.inventory.manager.analytics

import com.inventory.manager.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

object PredictionEngine {

    private const val RESTOCK_HORIZON_DAYS = 30
    private const val DEAD_STOCK_DAYS = 30
    private const val HIGH_DEMAND_FACTOR = 1.5
    private const val LOW_DEMAND_FACTOR = 0.3

    fun buildSummary(records: List<SalesRecord>, windowDays: Int, currentStock: Map<Long, Int>): AnalyticsSummary {
        if (records.isEmpty()) return emptyAnalyticsSummary()
        val safeWindow = max(windowDays, 1)
        val grouped = records.groupBy { it.itemId }
        val avgDailySalesMap = grouped.mapValues { (_, recs) -> recs.sumOf { it.quantitySold }.toDouble() / safeWindow }
        val medianAvgDaily = median(avgDailySalesMap.values.toList())

        val productAnalyses = grouped.map { (itemId, recs) ->
            val totalUnits = recs.sumOf { it.quantitySold }
            val totalRevenue = recs.sumOf { it.revenueIncGst }
            val avgDaily = avgDailySalesMap[itemId] ?: 0.0
            val stock = currentStock[itemId] ?: 0
            val lastSaleTs = recs.maxOf { it.saleTimestamp }
            val daysSinceLast = ((System.currentTimeMillis() - lastSaleTs) / 86_400_000L).toInt()
            val demand = demandLabel(avgDaily, medianAvgDaily, daysSinceLast)
            val restock = max(0, (avgDaily * RESTOCK_HORIZON_DAYS).roundToInt() - stock)
            val daysOut = if (avgDaily > 0) (stock / avgDaily).roundToInt() else null
            ProductAnalysis(itemId, recs.first().itemName, recs.first().category,
                totalUnits, totalRevenue, avgDaily, demand, restock, daysOut)
        }

        val sortedByUnits = productAnalyses.sortedByDescending { it.totalUnitsSold }
        val slowProducts = productAnalyses.filter {
            it.demandLabel == DemandLabel.LOW_DEMAND || it.demandLabel == DemandLabel.DEAD_STOCK
        }.take(5)
        val totalRev = records.sumOf { it.revenueIncGst }.takeIf { it > 0 } ?: 1.0
        val catBreakdown = records.groupBy { it.category }
            .mapValues { (_, recs) -> (recs.sumOf { it.revenueIncGst } / totalRev * 100).roundTo(1) }

        return AnalyticsSummary(
            totalRevenue = records.sumOf { it.revenueIncGst },
            totalUnitsSold = records.sumOf { it.quantitySold },
            totalBillsAnalysed = records.map { it.billId }.distinct().size,
            topProducts = sortedByUnits.take(5),
            slowProducts = slowProducts,
            dailyTrend = buildDailyTrend(records, 30),
            weeklyTrend = buildWeeklyTrend(records, 12),
            monthlyTrend = buildMonthlyTrend(records, 12),
            categoryBreakdown = catBreakdown
        )
    }

    private fun demandLabel(avgDaily: Double, median: Double, daysSinceLast: Int): DemandLabel = when {
        daysSinceLast >= DEAD_STOCK_DAYS -> DemandLabel.DEAD_STOCK
        avgDaily >= median * HIGH_DEMAND_FACTOR -> DemandLabel.HIGH_DEMAND
        avgDaily <= median * LOW_DEMAND_FACTOR -> DemandLabel.LOW_DEMAND
        else -> DemandLabel.MODERATE_DEMAND
    }

    private fun buildDailyTrend(records: List<SalesRecord>, days: Int): List<SalesTrend> {
        val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        val cal = Calendar.getInstance()
        return (days - 1 downTo 0).map { offset ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val start = startOfDay(cal)
            val end = start + 86_400_000L
            val recs = records.filter { it.saleTimestamp in start until end }
            SalesTrend(fmt.format(Date(start)), recs.sumOf { it.quantitySold }, recs.sumOf { it.revenueIncGst }, start)
        }
    }

    private fun buildWeeklyTrend(records: List<SalesRecord>, weeks: Int): List<SalesTrend> {
        val cal = Calendar.getInstance()
        return (weeks - 1 downTo 0).map { offset ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.WEEK_OF_YEAR, -offset)
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val start = startOfDay(cal)
            val end = start + 7 * 86_400_000L
            val recs = records.filter { it.saleTimestamp in start until end }
            SalesTrend("Wk ${cal.get(Calendar.WEEK_OF_YEAR)}", recs.sumOf { it.quantitySold }, recs.sumOf { it.revenueIncGst }, start)
        }
    }

    private fun buildMonthlyTrend(records: List<SalesRecord>, months: Int): List<SalesTrend> {
        val fmt = SimpleDateFormat("MMM yy", Locale.getDefault())
        val cal = Calendar.getInstance()
        return (months - 1 downTo 0).map { offset ->
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MONTH, -offset)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = startOfDay(cal)
            cal.add(Calendar.MONTH, 1)
            val end = startOfDay(cal)
            val recs = records.filter { it.saleTimestamp in start until end }
            SalesTrend(fmt.format(Date(start)), recs.sumOf { it.quantitySold }, recs.sumOf { it.revenueIncGst }, start)
        }
    }

    private fun startOfDay(cal: Calendar): Long {
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun median(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val s = values.sorted()
        return if (s.size % 2 == 0) (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0 else s[s.size / 2]
    }

    private fun Double.roundTo(d: Int): Double {
        var m = 1.0; repeat(d) { m *= 10 }; return (this * m).roundToInt() / m
    }

    private fun emptyAnalyticsSummary() = AnalyticsSummary(0.0, 0, 0,
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyMap())
}
