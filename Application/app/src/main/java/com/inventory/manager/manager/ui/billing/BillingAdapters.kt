package com.inventory.manager.ui.billing

import android.view.*; import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter; import androidx.recyclerview.widget.RecyclerView
import com.inventory.manager.data.model.Bill; import com.inventory.manager.data.model.BillItem; import com.inventory.manager.data.model.Item
import com.inventory.manager.databinding.ItemBillItemBinding
import com.inventory.manager.databinding.ItemBillHistoryBinding
import com.inventory.manager.databinding.ItemSearchResultBinding
import java.text.SimpleDateFormat; import java.util.*

class BillItemAdapter(private val onQtyChange: (Int, Int) -> Unit, private val onRemove: (Int) -> Unit)
    : ListAdapter<BillItem, BillItemAdapter.VH>(DIFF) {
    inner class VH(private val b: ItemBillItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: BillItem, pos: Int) {
            b.tvItemName.text = item.itemName
            b.tvUnitPrice.text = "₹%.2f × ${item.quantity}".format(item.sellingPrice)
            b.tvGstRate.text = "GST ${item.gstRate}%"
            b.tvCgst.text = "CGST: ₹%.2f".format(item.cgst)
            b.tvSgst.text = "SGST: ₹%.2f".format(item.sgst)
            b.tvQuantity.text = item.quantity.toString()
            b.tvItemTotal.text = "₹%.2f".format(item.lineTotal)
            b.btnIncrement.setOnClickListener { onQtyChange(pos, item.quantity + 1) }
            b.btnDecrement.setOnClickListener { if (item.quantity > 1) onQtyChange(pos, item.quantity - 1) else onRemove(pos) }
            b.btnRemove.setOnClickListener { onRemove(pos) }
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(ItemBillItemBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos), pos)
    companion object { val DIFF = object : DiffUtil.ItemCallback<BillItem>() {
        override fun areItemsTheSame(a: BillItem, b: BillItem) = a.itemId == b.itemId
        override fun areContentsTheSame(a: BillItem, b: BillItem) = a == b
    }}
}

class SearchItemAdapter(private val onAdd: (Item) -> Unit) : ListAdapter<Item, SearchItemAdapter.VH>(DIFF) {
    inner class VH(private val b: ItemSearchResultBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Item) {
            b.tvName.text = item.name; b.tvCategory.text = item.category
            b.tvPrice.text = "₹%.2f".format(item.sellingPrice); b.tvStock.text = "${item.stock} ${item.unit}"
            b.root.setOnClickListener { onAdd(item) }
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(ItemSearchResultBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    companion object { val DIFF = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(a: Item, b: Item) = a.id == b.id
        override fun areContentsTheSame(a: Item, b: Item) = a == b
    }}
}

class BillHistoryAdapter(private val onClick: (Bill) -> Unit) : ListAdapter<Bill, BillHistoryAdapter.VH>(DIFF) {
    inner class VH(private val b: ItemBillHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(bill: Bill) {
            b.tvBillNumber.text = bill.billNumber
            b.tvAmount.text = "₹%.2f".format(bill.grandTotal)
            b.tvCustomer.text = "Bill #${bill.id}"
            b.tvGstAmount.text = "GST: ₹%.2f".format(bill.totalCgst + bill.totalSgst)
            b.tvDate.text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.createdAt))
            b.tvItemCount.text = bill.paymentMode
            b.tvPaymentMode.text = bill.paymentMode
            b.root.setOnClickListener { onClick(bill) }
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(ItemBillHistoryBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    companion object { val DIFF = object : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(a: Bill, b: Bill) = a.id == b.id
        override fun areContentsTheSame(a: Bill, b: Bill) = a == b
    }}
}
