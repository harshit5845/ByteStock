package com.inventory.manager.ui.home

import android.view.LayoutInflater; import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil; import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventory.manager.data.model.Bill
import com.inventory.manager.databinding.ItemRecentBillBinding
import java.text.SimpleDateFormat; import java.util.*

class RecentBillAdapter : ListAdapter<Bill, RecentBillAdapter.VH>(DIFF) {
    inner class VH(private val b: ItemRecentBillBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(bill: Bill) {
            b.tvBillNumber.text = bill.billNumber
            b.tvBillDate.text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(bill.createdAt))
            b.tvBillAmount.text = "₹%.2f".format(bill.grandTotal)
            b.tvPaymentMode.text = bill.paymentMode
        }
    }
    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemRecentBillBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    companion object { val DIFF = object : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(a: Bill, b: Bill) = a.id == b.id
        override fun areContentsTheSame(a: Bill, b: Bill) = a == b
    }}
}
