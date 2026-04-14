package com.inventory.manager.ui.inventory

import android.os.Bundle; import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment; import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inventory.manager.InventoryApp
import com.inventory.manager.data.model.Item
import com.inventory.manager.databinding.FragmentInventoryBinding
import com.inventory.manager.viewmodel.InventoryViewModel

class InventoryFragment : Fragment() {
    private var _b: FragmentInventoryBinding? = null
    private val b get() = _b!!

    private val vm: InventoryViewModel by activityViewModels {
        InventoryViewModel.Factory((requireActivity().application as InventoryApp).inventoryRepository)
    }
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentInventoryBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = InventoryAdapter(
            onEdit = { showAddEditDialog(it) },
            onDelete = { confirmDelete(it) },
            onStockUpdate = { showStockUpdateDialog(it) }
        )
        b.rvInventory.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        b.rvInventory.adapter = adapter

        b.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean { vm.setSearchQuery(q ?: ""); return true }
        })

        vm.filteredItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            b.tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            b.tvItemCount.text = "${items.size} items"
        }
        vm.allCategories.observe(viewLifecycleOwner) { setupCategoryChips(it) }
        b.fabAddItem.setOnClickListener { showAddEditDialog(null) }
    }

    private fun setupCategoryChips(categories: List<String>) {
        b.chipGroupCategories.removeAllViews()
        val all = Chip(requireContext()).apply { text = "All"; isCheckable = true; isChecked = true
            setOnCheckedChangeListener { _, c -> if (c) vm.setCategory(null) } }
        b.chipGroupCategories.addView(all)
        categories.forEach { cat ->
            val chip = Chip(requireContext()).apply { text = cat; isCheckable = true
                setOnCheckedChangeListener { _, c -> if (c) vm.setCategory(cat) } }
            b.chipGroupCategories.addView(chip)
        }
    }

    private fun showAddEditDialog(item: Item?) {
        AddEditItemDialog(item) { saved ->
            if (item == null) vm.insert(saved) else vm.update(saved.copy(id = item.id))
        }.show(childFragmentManager, "AddEditItem")
    }

    private fun confirmDelete(item: Item) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Delete Item")
            .setMessage("Delete \"${item.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> vm.delete(item) }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showStockUpdateDialog(item: Item) {
        val view = layoutInflater.inflate(com.inventory.manager.R.layout.dialog_update_stock, null)
        val rbAdd = view.findViewById<android.widget.RadioButton>(com.inventory.manager.R.id.rbAdd)
        val etQty = view.findViewById<com.google.android.material.textfield.TextInputEditText>(com.inventory.manager.R.id.etQuantity)
        MaterialAlertDialogBuilder(requireContext()).setTitle("Update Stock — ${item.name}")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val qty = etQty.text.toString().toIntOrNull() ?: 0
                val sign = if (rbAdd.isChecked) 1 else -1
                if (qty > 0) vm.adjustStock(item.id, sign * qty)
            }.setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
