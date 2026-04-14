package com.inventory.manager.ui.analytics

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventory.manager.data.model.DemandLabel
import com.inventory.manager.data.model.ProductAnalysis
import com.inventory.manager.databinding.ItemProductAnalysisBinding

class ProductAnalysisAdapter(private val showRestock: Boolean = false)
    : ListAdapter<ProductAnalysis, ProductAnalysisAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemProductAnalysisBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: ProductAnalysis) {
            b.tvProductName.text = "${p.demandLabel.emoji} ${p.itemName}"
            b.tvCategory.text = p.category
            b.tvUnits.text = "${p.totalUnitsSold} units"
            b.tvRevenue.text = "₹%.0f".format(p.totalRevenue)
            b.tvAvgDaily.text = "~%.1f/day".format(p.avgDailySales)
            b.chipDemand.text = p.demandLabel.displayName
            val (bg, fg) = demandColors(p.demandLabel)
            b.chipDemand.chipBackgroundColor = ColorStateList.valueOf(bg)
            b.chipDemand.setTextColor(fg)
            if (showRestock && p.restockSuggestion > 0) {
                b.tvRestock.visibility = View.VISIBLE
                b.tvRestock.text = "📦 Restock: +${p.restockSuggestion} units"
            } else b.tvRestock.visibility = View.GONE
            p.daysUntilStockOut?.let { days ->
                b.tvStockout.visibility = View.VISIBLE
                b.tvStockout.text = when {
                    days <= 0  -> "⛔ Out of stock now"
                    days <= 7  -> "⚠️ Stockout in $days days"
                    else       -> "Stock lasts ~$days days"
                }
                b.tvStockout.setTextColor(if (days <= 7) Color.parseColor("#C62828") else Color.parseColor("#555555"))
            } ?: run { b.tvStockout.visibility = View.GONE }
        }

        private fun demandColors(label: DemandLabel): Pair<Int, Int> = when (label) {
            DemandLabel.HIGH_DEMAND     -> Color.parseColor("#E65100") to Color.WHITE
            DemandLabel.MODERATE_DEMAND -> Color.parseColor("#1565C0") to Color.WHITE
            DemandLabel.LOW_DEMAND      -> Color.parseColor("#F9A825") to Color.BLACK
            DemandLabel.DEAD_STOCK      -> Color.parseColor("#616161") to Color.WHITE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductAnalysisBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ProductAnalysis>() {
            override fun areItemsTheSame(a: ProductAnalysis, b: ProductAnalysis) = a.itemId == b.itemId
            override fun areContentsTheSame(a: ProductAnalysis, b: ProductAnalysis) = a == b
        }
    }
}
