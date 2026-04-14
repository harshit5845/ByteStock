package com.inventory.manager.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.inventory.manager.InventoryApp
import com.inventory.manager.R
import com.inventory.manager.databinding.FragmentHomeBinding
import com.inventory.manager.viewmodel.BillingViewModel
import com.inventory.manager.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    private val invVM: InventoryViewModel by activityViewModels {
        InventoryViewModel.Factory((requireActivity().application as InventoryApp).inventoryRepository)
    }
    private val billVM: BillingViewModel by activityViewModels {
        val app = requireActivity().application as InventoryApp
        BillingViewModel.Factory(app.billRepository, app.inventoryRepository, app.salesRepository)
    }

    private lateinit var recentBillAdapter: RecentBillAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        b.tvGreeting.text = when { h < 12 -> "Good Morning! ☀️"; h < 17 -> "Good Afternoon! 🌤️"; else -> "Good Evening! 🌙" }
        b.tvDate.text = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())

        recentBillAdapter = RecentBillAdapter()
        b.rvRecentBills.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.rvRecentBills.adapter = recentBillAdapter

        invVM.totalProductCount.observe(viewLifecycleOwner) { b.tvTotalItems.text = it?.toString() ?: "0" }
        invVM.lowStockCount.observe(viewLifecycleOwner) { count ->
            b.tvLowStock.text = count?.toString() ?: "0"
            val c2 = count ?: 0
            if (c2 > 0) {
                b.cardAlerts.visibility = View.VISIBLE
                b.tvAlertMessage.text = "⚠️ $c2 item(s) running low on stock!"
            } else b.cardAlerts.visibility = View.GONE
        }
        invVM.totalInventoryValue.observe(viewLifecycleOwner) { b.tvInventoryValue.text = "₹%.0f".format(it ?: 0.0) }
        billVM.totalBillCount.observe(viewLifecycleOwner) { b.tvTodayBills.text = it?.toString() ?: "0" }
        billVM.recentBills.observe(viewLifecycleOwner) {
            recentBillAdapter.submitList(it)
            b.tvNoRecentBills.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
        b.btnQuickAddItem.setOnClickListener { findNavController().navigate(R.id.inventoryFragment) }
        b.btnCreateBill.setOnClickListener { findNavController().navigate(R.id.billingFragment) }
        b.btnViewAlerts.setOnClickListener { findNavController().navigate(R.id.inventoryFragment) }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
