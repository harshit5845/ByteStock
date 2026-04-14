package com.inventory.manager.ui.inventory

import android.graphics.Color; import android.view.*
import androidx.recyclerview.widget.DiffUtil; import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventory.manager.data.model.Item
import com.inventory.manager.databinding.ItemInventoryBinding

class InventoryAdapter(
    private val onEdit: (Item) -> Unit,
    private val onDelete: (Item) -> Unit,
    private val onStockUpdate: (Item) -> Unit
) : ListAdapter<Item, InventoryAdapter.VH>(DIFF) {
    inner class VH(private val b: ItemInventoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Item) {
            b.tvItemName.text = item.name
            b.tvCategory.text = item.category
            b.tvPrice.text = "₹%.2f".format(item.sellingPrice)
            b.tvStock.text = "${item.stock} ${item.unit}"
            b.tvGst.text = "GST ${item.gstRate}%"
            when {
                item.isOutOfStock -> { b.tvStockStatus.text = "Out of Stock ❌"; b.tvStockStatus.setTextColor(Color.parseColor("#D32F2F")) }
                item.isLowStock  -> { b.tvStockStatus.text = "Low Stock ⚠️"; b.tvStockStatus.setTextColor(Color.parseColor("#F57C00")) }
                else             -> { b.tvStockStatus.text = "In Stock ✓"; b.tvStockStatus.setTextColor(Color.parseColor("#388E3C")) }
            }
            b.btnEdit.setOnClickListener { onEdit(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
            b.btnStockUpdate.setOnClickListener { onStockUpdate(item) }
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemInventoryBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    companion object { val DIFF = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(a: Item, b: Item) = a.id == b.id
        override fun areContentsTheSame(a: Item, b: Item) = a == b
    }}
}
