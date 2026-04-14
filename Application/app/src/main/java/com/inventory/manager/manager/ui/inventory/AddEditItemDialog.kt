package com.inventory.manager.ui.inventory

import android.os.Bundle; import android.text.*; import android.view.*
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.inventory.manager.data.model.Item
import com.inventory.manager.databinding.DialogAddEditItemBinding

class AddEditItemDialog(private val item: Item?, private val onSave: (Item) -> Unit) : BottomSheetDialogFragment() {
    private var _b: DialogAddEditItemBinding? = null
    private val b get() = _b!!

    private val gstRates = listOf(0.0, 0.1, 0.25, 1.0, 1.5, 3.0, 5.0, 7.5, 12.0, 18.0, 28.0)
    private val units = listOf("pcs","kg","g","l","ml","box","pack","dozen","pair","strip","bag","bottle")
    private val categories = listOf("Food & Grocery","Electronics","Clothing","Medicine","Stationery","Home & Kitchen","Sports","Beauty","Toys","Automotive","Books","Other")

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = DialogAddEditItemBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        b.spinnerCategory.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, categories))
        b.spinnerGst.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, gstRates.map { "${it}%" }))
        b.spinnerUnit.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, units))

        item?.let { populate(it) } ?: run {
            b.spinnerCategory.setText(categories[0], false)
            b.spinnerGst.setText("18.0%", false)
            b.spinnerUnit.setText(units[0], false)
        }
        b.tvTitle.text = if (item == null) "Add New Item" else "Edit Item"

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateGstPreview() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        }
        b.etSellingPrice.addTextChangedListener(watcher)
        b.spinnerGst.addTextChangedListener(watcher)

        b.btnSave.setOnClickListener { saveItem() }
        b.btnCancel.setOnClickListener { dismiss() }
    }

    private fun populate(item: Item) {
        b.etItemName.setText(item.name)
        b.etDescription.setText(item.description)
        b.etPurchasePrice.setText(item.purchasePrice.toString())
        b.etSellingPrice.setText(item.sellingPrice.toString())
        b.etStock.setText(item.stock.toString())
        b.etHsnCode.setText(item.hsnCode)
        b.etBarcode.setText(item.barcode)
        b.etLowStockThreshold.setText(item.lowStockThreshold.toString())
        b.spinnerCategory.setText(item.category, false)
        b.spinnerGst.setText("${item.gstRate}%", false)
        b.spinnerUnit.setText(item.unit, false)
    }

    private fun updateGstPreview() {
        val price = b.etSellingPrice.text.toString().toDoubleOrNull() ?: return
        val gstStr = b.spinnerGst.text.toString().replace("%", "").trim()
        val gst = gstStr.toDoubleOrNull() ?: 18.0
        val cgst = price * (gst / 2) / 100
        b.tvGstPreview.visibility = View.VISIBLE
        b.tvGstPreview.text = "CGST: ₹%.2f | SGST: ₹%.2f | Total: ₹%.2f".format(cgst, cgst, price + cgst * 2)
    }

    private fun saveItem() {
        val name = b.etItemName.text.toString().trim()
        if (name.isEmpty()) { b.etItemName.error = "Required"; return }
        val gstStr = b.spinnerGst.text.toString().replace("%", "").trim()
        val saved = Item(
            name = name,
            category = b.spinnerCategory.text.toString().ifEmpty { categories[0] },
            description = b.etDescription.text.toString().trim(),
            purchasePrice = b.etPurchasePrice.text.toString().toDoubleOrNull() ?: 0.0,
            sellingPrice = b.etSellingPrice.text.toString().toDoubleOrNull() ?: 0.0,
            stock = b.etStock.text.toString().toIntOrNull() ?: 0,
            unit = b.spinnerUnit.text.toString().ifEmpty { units[0] },
            gstRate = gstStr.toDoubleOrNull() ?: 18.0,
            hsnCode = b.etHsnCode.text.toString().trim(),
            barcode = b.etBarcode.text.toString().trim(),
            lowStockThreshold = b.etLowStockThreshold.text.toString().toIntOrNull() ?: 5
        )
        onSave(saved); dismiss()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
