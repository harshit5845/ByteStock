package com.inventory.manager.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.inventory.manager.InventoryApp
import com.inventory.manager.data.model.AnalyticsSummary
import com.inventory.manager.databinding.FragmentAnalyticsBinding
import com.inventory.manager.viewmodel.AnalyticsViewModel

class AnalyticsFragment : Fragment() {

    private var _b: FragmentAnalyticsBinding? = null
    private val b get() = _b!!

    private val viewModel: AnalyticsViewModel by activityViewModels {
        val app = requireActivity().application as InventoryApp
        AnalyticsViewModel.Factory(app.salesRepository, app.inventoryRepository)
    }

    private lateinit var topAdapter: ProductAnalysisAdapter
    private lateinit var slowAdapter: ProductAnalysisAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topAdapter  = ProductAnalysisAdapter(showRestock = false)
        slowAdapter = ProductAnalysisAdapter(showRestock = true)
        b.rvTopProducts.layoutManager  = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.rvSlowProducts.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.rvTopProducts.adapter  = topAdapter
        b.rvSlowProducts.adapter = slowAdapter

        setupWindowChips()
        observeViewModel()
        b.btnRefresh.setOnClickListener { viewModel.refresh() }
    }

    private fun setupWindowChips() {
        val windows = listOf(7 to "7 Days", 30 to "30 Days", 90 to "3 Months", 365 to "1 Year")
        windows.forEachIndexed { idx, (days, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = idx == 1
                setOnCheckedChangeListener { _, checked -> if (checked) viewModel.setWindow(days) }
            }
            b.chipGroupWindow.addView(chip)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            b.progressBar.visibility  = if (loading) View.VISIBLE else View.GONE
            b.scrollContent.visibility = if (loading) View.GONE   else View.VISIBLE
        }
        viewModel.summary.observe(viewLifecycleOwner) { it?.let { s -> bindSummary(s) } }
    }

    private fun bindSummary(s: AnalyticsSummary) {
        b.tvTotalRevenue.text = "₹%.2f".format(s.totalRevenue)
        b.tvTotalUnits.text   = s.totalUnitsSold.toString()
        b.tvTotalBills.text   = s.totalBillsAnalysed.toString()

        topAdapter.submitList(s.topProducts)
        slowAdapter.submitList(s.slowProducts)

        b.tvNoTopProducts.visibility  = if (s.topProducts.isEmpty())  View.VISIBLE else View.GONE
        b.tvNoSlowProducts.visibility = if (s.slowProducts.isEmpty()) View.VISIBLE else View.GONE

        b.barChartDaily.setData(s.dailyTrend)
        b.barChartWeekly.setData(s.weeklyTrend)
        b.barChartMonthly.setData(s.monthlyTrend)

        bindCategoryBreakdown(s.categoryBreakdown)
    }

    private fun bindCategoryBreakdown(breakdown: Map<String, Double>) {
        b.llCategories.removeAllViews()
        val colors = listOf("#6200EE","#03DAC5","#FF6D00","#D50000","#00C853","#2962FF","#AA00FF")
        breakdown.entries.sortedByDescending { it.value }.forEachIndexed { i, (cat, pct) ->
            val row = layoutInflater.inflate(
                com.inventory.manager.R.layout.item_category_row, b.llCategories, false
            )
            row.findViewById<View>(com.inventory.manager.R.id.viewColor)
                .setBackgroundColor(Color.parseColor(colors[i % colors.size]))
            row.findViewById<android.widget.TextView>(com.inventory.manager.R.id.tvCatName).text = cat
            row.findViewById<android.widget.TextView>(com.inventory.manager.R.id.tvCatPct).text = "%.1f%%".format(pct)
            b.llCategories.addView(row)
        }
        b.tvNoCategoryData.visibility = if (breakdown.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
