package com.inventory.manager.data.model

data class ProductAnalysis(
    val itemId: Long,
    val itemName: String,
    val category: String,
    val totalUnitsSold: Int,
    val totalRevenue: Double,
    val avgDailySales: Double,
    val demandLabel: DemandLabel,
    val restockSuggestion: Int,
    val daysUntilStockOut: Int?
)

enum class DemandLabel(val displayName: String, val emoji: String) {
    HIGH_DEMAND("High Demand", "🔥"),
    MODERATE_DEMAND("Moderate", "📈"),
    LOW_DEMAND("Low Demand", "📉"),
    DEAD_STOCK("Dead Stock", "💀")
}

data class SalesTrend(
    val label: String,
    val totalUnits: Int,
    val totalRevenue: Double,
    val timestamp: Long
)

data class AnalyticsSummary(
    val totalRevenue: Double,
    val totalUnitsSold: Int,
    val totalBillsAnalysed: Int,
    val topProducts: List<ProductAnalysis>,
    val slowProducts: List<ProductAnalysis>,
    val dailyTrend: List<SalesTrend>,
    val weeklyTrend: List<SalesTrend>,
    val monthlyTrend: List<SalesTrend>,
    val categoryBreakdown: Map<String, Double>,
    val generatedAt: Long = System.currentTimeMillis()
)
