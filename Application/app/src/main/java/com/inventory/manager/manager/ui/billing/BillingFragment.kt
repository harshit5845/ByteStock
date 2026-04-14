package com.inventory.manager.ui.billing

import android.os.Bundle; import android.text.*; import android.view.*
import androidx.fragment.app.Fragment; import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.inventory.manager.InventoryApp
import com.inventory.manager.data.model.Bill
import com.inventory.manager.data.model.Item
import com.inventory.manager.databinding.FragmentBillingBinding
import com.inventory.manager.viewmodel.BillingViewModel
import com.inventory.manager.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat; import java.util.*

class BillingFragment : Fragment() {
    private var _b: FragmentBillingBinding? = null
    private val b get() = _b!!

    private val billVM: BillingViewModel by activityViewModels {
        val app = requireActivity().application as InventoryApp
        BillingViewModel.Factory(app.billRepository, app.inventoryRepository, app.salesRepository)
    }
    private val invVM: InventoryViewModel by activityViewModels {
        InventoryViewModel.Factory((requireActivity().application as InventoryApp).inventoryRepository)
    }

    private lateinit var billItemAdapter: BillItemAdapter
    private lateinit var searchAdapter: SearchItemAdapter
    private lateinit var historyAdapter: BillHistoryAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentBillingBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs(); setupBillAdapters(); setupSearch(); observeData(); setupButtons()
    }

    private fun setupTabs() {
        b.tabLayout.addTab(b.tabLayout.newTab().setText("New Bill"))
        b.tabLayout.addTab(b.tabLayout.newTab().setText("History"))
        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) { b.layoutNewBill.visibility = View.VISIBLE; b.layoutBillHistory.visibility = View.GONE }
                else { b.layoutNewBill.visibility = View.GONE; b.layoutBillHistory.visibility = View.VISIBLE }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBillAdapters() {
        b.rvBillItems.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.rvSearchResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.rvBillHistory.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        billItemAdapter = BillItemAdapter(
            onQtyChange = { idx, qty -> billVM.updateQuantity(idx, qty) },
            onRemove = { idx -> billVM.removeItem(idx) }
        )
        b.rvBillItems.adapter = billItemAdapter

        historyAdapter = BillHistoryAdapter { showBillDetails(it) }
        b.rvBillHistory.adapter = historyAdapter
    }

    private fun setupSearch() {
        searchAdapter = SearchItemAdapter { item -> addItemToBill(item) }
        b.rvSearchResults.adapter = searchAdapter
        b.etSearchItem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                if (q.length >= 2) {
                    invVM.allItems.value?.filter { it.name.contains(q, true) || it.category.contains(q, true) }
                        ?.also { searchAdapter.submitList(it); b.rvSearchResults.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE }
                } else { b.rvSearchResults.visibility = View.GONE }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })
    }

    private fun addItemToBill(item: Item) {
        if (item.isOutOfStock) { Snackbar.make(b.root, "${item.name} is out of stock!", 2000).show(); return }
        billVM.addItem(item)
        b.etSearchItem.setText(""); b.rvSearchResults.visibility = View.GONE
    }

    private fun observeData() {
        billVM.currentBillItems.observe(viewLifecycleOwner) { items ->
            billItemAdapter.submitList(items.toList())
            b.tvEmptyBill.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            b.rvBillItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            b.layoutTotals.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
        billVM.subtotal.observe(viewLifecycleOwner) { b.tvSubtotal.text = "₹%.2f".format(it) }
        val gstObs = androidx.lifecycle.Observer<Double> {
            val cgst = billVM.totalCgst.value ?: 0.0
            val sgst = billVM.totalSgst.value ?: 0.0
            b.tvTotalGst.text = "₹%.2f".format(cgst + sgst)
        }
        billVM.totalCgst.observe(viewLifecycleOwner, gstObs)
        billVM.totalSgst.observe(viewLifecycleOwner, gstObs)
        billVM.grandTotal.observe(viewLifecycleOwner) { b.tvGrandTotal.text = "₹%.2f".format(it) }
        billVM.allBills.observe(viewLifecycleOwner) { bills ->
            historyAdapter.submitList(bills)
            b.tvNoHistory.visibility = if (bills.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
        billVM.billSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) Snackbar.make(b.root, "Bill saved! ✓", 2000).show()
        }
        b.etDiscount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { billVM.setDiscount(s.toString().toDoubleOrNull() ?: 0.0) }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, bef: Int, c: Int) {}
        })
    }

    private fun setupButtons() {
        b.btnSaveBill.setOnClickListener {
            if ((billVM.currentBillItems.value ?: emptyList<Any>()).isEmpty()) {
                Snackbar.make(b.root, "Add items first!", 2000).show(); return@setOnClickListener
            }
            val modes = arrayOf("Cash","UPI","Card","Net Banking","Cheque","Credit"); var sel = 0
            MaterialAlertDialogBuilder(requireContext()).setTitle("Payment Mode")
                .setSingleChoiceItems(modes, 0) { _, w -> sel = w }
                .setPositiveButton("Save Bill") { _, _ -> billVM.saveBill(modes[sel]) }
                .setNegativeButton("Cancel", null).show()
        }
        b.btnClearBill.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle("Clear Bill")
                .setMessage("Remove all items?")
                .setPositiveButton("Clear") { _, _ -> billVM.clearBill() }
                .setNegativeButton("Cancel", null).show()
        }
    }

    private fun showBillDetails(bill: Bill) {
        lifecycleScope.launch {
            val bwi = billVM.getBillWithItems(bill.id) ?: return@launch
            val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val details = buildString {
                appendLine("${bwi.bill.billNumber}"); appendLine("Date: ${fmt.format(Date(bwi.bill.createdAt))}")
                appendLine("Payment: ${bwi.bill.paymentMode}"); appendLine()
                bwi.items.forEach { appendLine("• ${it.itemName} × ${it.quantity} = ₹%.2f".format(it.lineTotal)) }
                appendLine(); appendLine("Subtotal: ₹%.2f".format(bwi.bill.subtotal))
                appendLine("GST: ₹%.2f".format(bwi.bill.totalCgst + bwi.bill.totalSgst))
                if (bwi.bill.discount > 0) appendLine("Discount: -₹%.2f".format(bwi.bill.discount))
                appendLine("Grand Total: ₹%.2f".format(bwi.bill.grandTotal))
            }
            MaterialAlertDialogBuilder(requireContext()).setTitle("Bill Details").setMessage(details)
                .setPositiveButton("OK", null).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
