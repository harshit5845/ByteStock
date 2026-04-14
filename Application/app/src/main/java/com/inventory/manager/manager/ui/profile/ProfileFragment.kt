package com.inventory.manager.ui.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.inventory.manager.InventoryApp
import com.inventory.manager.databinding.FragmentProfileBinding
import com.inventory.manager.viewmodel.BillingViewModel
import com.inventory.manager.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private lateinit var prefs: SharedPreferences

    private val invVM: InventoryViewModel by activityViewModels {
        InventoryViewModel.Factory((requireActivity().application as InventoryApp).inventoryRepository)
    }
    private val billVM: BillingViewModel by activityViewModels {
        val app = requireActivity().application as InventoryApp
        BillingViewModel.Factory(app.billRepository, app.inventoryRepository, app.salesRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireContext().getSharedPreferences("inventory_prefs", 0)
        loadProfile()
        setupSwitches()
        setupClickables()

        invVM.totalProductCount.observe(viewLifecycleOwner) { b.tvStatItems.text = it?.toString() ?: "0" }
        billVM.totalBillCount.observe(viewLifecycleOwner) { b.tvStatBills.text = it?.toString() ?: "0" }
        invVM.totalInventoryValue.observe(viewLifecycleOwner) { b.tvStatValue.text = "₹%.0f".format(it ?: 0.0) }
    }

    private fun loadProfile() {
        b.tvBusinessName.text = prefs.getString("business_name", "My Business") ?: "My Business"
        b.tvOwnerName.text = prefs.getString("owner_name", "") ?: ""
        b.tvGstin.text = prefs.getString("gstin", "GSTIN: Not Set") ?: ""
        b.tvPhone.text = prefs.getString("phone", "Not Set") ?: "Not Set"
        b.tvEmail.text = prefs.getString("email", "Not Set") ?: "Not Set"
        b.tvAddress.text = prefs.getString("address", "Not Set") ?: "Not Set"
        b.switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        b.switchNotifications.isChecked = prefs.getBoolean("notifications", true)
        b.switchLowStockAlert.isChecked = prefs.getBoolean("low_stock_alert", true)
    }

    private fun setupSwitches() {
        b.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("dark_mode", checked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        b.switchNotifications.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("notifications", checked).apply()
        }
        b.switchLowStockAlert.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("low_stock_alert", checked).apply()
        }
    }

    private fun setupClickables() {
        b.btnEditProfile.setOnClickListener { showEditProfileDialog() }

        b.layoutAbout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("About ByteStock")
                .setMessage("Version 1.0\n\nSmart GST Inventory Manager with AI-powered Sales Analytics.\n\nMade in India 🇮🇳")
                .setPositiveButton("OK", null).show()
        }
        b.layoutRateApp.setOnClickListener {
            Snackbar.make(b.root, "Thank you! ⭐", 2000).show()
        }
        b.layoutFeedback.setOnClickListener {
            Snackbar.make(b.root, "Feedback sent! 💬", 2000).show()
        }
        b.layoutPrivacyPolicy.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage("Your data is stored locally on your device only. We do not collect or share any personal information.")
                .setPositiveButton("OK", null).show()
        }
        b.layoutGstSettings.setOnClickListener { showGstSettingsDialog() }
        b.layoutLowStockThreshold.setOnClickListener { showThresholdDialog() }
        b.btnExportData.setOnClickListener {
            Snackbar.make(b.root, "Export feature coming soon!", 2000).show()
        }
        b.btnClearData.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("This will delete ALL inventory, bills, and sales data permanently. Continue?")
                .setPositiveButton("Clear") { _, _ ->
                    // Use viewLifecycleOwner.lifecycleScope - safe coroutine scope for fragments
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            (requireActivity().application as InventoryApp).salesRepository.clearAll()
                            Snackbar.make(b.root, "Sales data cleared.", 2000).show()
                        } catch (e: Exception) {
                            Snackbar.make(b.root, "Error: ${e.message}", 2000).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null).show()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(com.inventory.manager.R.layout.dialog_edit_profile, null)
        val etBiz   = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.inventory.manager.R.id.etBusinessName)
        val etOwner = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.inventory.manager.R.id.etOwnerName)
        val etGstin = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.inventory.manager.R.id.etGstin)
        val etPhone = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.inventory.manager.R.id.etPhone)
        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.inventory.manager.R.id.etEmail)
        val etAddr  = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.inventory.manager.R.id.etAddress)

        etBiz.setText(prefs.getString("business_name", ""))
        etOwner.setText(prefs.getString("owner_name", ""))
        etGstin.setText(prefs.getString("gstin", ""))
        etPhone.setText(prefs.getString("phone", ""))
        etEmail.setText(prefs.getString("email", ""))
        etAddr.setText(prefs.getString("address", ""))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit()
                    .putString("business_name", etBiz.text.toString())
                    .putString("owner_name", etOwner.text.toString())
                    .putString("gstin", etGstin.text.toString())
                    .putString("phone", etPhone.text.toString())
                    .putString("email", etEmail.text.toString())
                    .putString("address", etAddr.text.toString())
                    .apply()
                loadProfile()
                Snackbar.make(b.root, "Profile saved ✓", 2000).show()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showGstSettingsDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(prefs.getFloat("default_gst", 18f).toString())
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default GST Rate (%)")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit().putFloat("default_gst", input.text.toString().toFloatOrNull() ?: 18f).apply()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showThresholdDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("low_stock_threshold", 5).toString())
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Low Stock Threshold")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit().putInt("low_stock_threshold", input.text.toString().toIntOrNull() ?: 5).apply()
            }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
