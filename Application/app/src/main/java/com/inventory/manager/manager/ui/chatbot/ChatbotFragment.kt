package com.inventory.manager.ui.chatbot

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.inventory.manager.InventoryApp
import com.inventory.manager.data.model.ChatMessage
import com.inventory.manager.databinding.FragmentChatbotBinding
import com.inventory.manager.viewmodel.AnalyticsViewModel
import com.inventory.manager.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatbotFragment : Fragment() {

    private var _b: FragmentChatbotBinding? = null
    private val b get() = _b!!

    private val invVM: InventoryViewModel by activityViewModels {
        InventoryViewModel.Factory((requireActivity().application as InventoryApp).inventoryRepository)
    }
    private val analyticsVM: AnalyticsViewModel by activityViewModels {
        val app = requireActivity().application as InventoryApp
        AnalyticsViewModel.Factory(app.salesRepository, app.inventoryRepository)
    }

    private lateinit var chatAdapter: ChatAdapter
    private val engine = ChatbotEngine()
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentChatbotBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter()
        val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        b.rvChat.layoutManager = layoutManager
        b.rvChat.adapter = chatAdapter

        // Welcome message with quick suggestions
        if (messages.isEmpty()) {
            addBotMessage(
                "👋 Hi! I'm your ByteStock Assistant.\n\nI can help with stock queries, GST calculations, sales insights, and how-to guides.\n\nWhat do you need help with?",
                listOf("Show low stock", "How to create bill", "GST rates", "Top selling products")
            )
        }

        // Feed live data into engine
        invVM.allItems.observe(viewLifecycleOwner) { items ->
            engine.setData(items ?: emptyList(), analyticsVM.summary.value)
        }
        analyticsVM.summary.observe(viewLifecycleOwner) { summary ->
            engine.setData(invVM.allItems.value ?: emptyList(), summary)
        }

        b.btnSend.setOnClickListener { sendMessage() }
        b.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
        }
    }

    private fun sendMessage(text: String = b.etMessage.text.toString().trim()) {
        if (text.isBlank()) return
        b.etMessage.setText("")

        // User bubble
        messages.add(ChatMessage(text = text, isUser = true))
        refreshChat()

        // Typing indicator
        b.tvTyping.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            delay(700)
            val resp = engine.process(text)
            _b?.let {
                it.tvTyping.visibility = View.GONE
                addBotMessage(resp.text, resp.suggestions)
            }
        }
    }

    private fun addBotMessage(text: String, suggestions: List<String> = emptyList()) {
        messages.add(ChatMessage(text = text, isUser = false, suggestions = suggestions))
        refreshChat()
        updateSuggestionChips(suggestions)
    }

    private fun refreshChat() {
        chatAdapter.submitList(messages.toList())
        _b?.rvChat?.post { _b?.rvChat?.smoothScrollToPosition(messages.size - 1) }
    }

    private fun updateSuggestionChips(suggestions: List<String>) {
        val chipGroup = _b?.chipGroupSuggestions ?: return
        chipGroup.removeAllViews()
        suggestions.forEach { suggestion ->
            val chip = Chip(requireContext()).apply {
                text = suggestion
                isClickable = true
                isCheckable = false
                setChipBackgroundColorResource(android.R.color.transparent)
                setOnClickListener { sendMessage(suggestion) }
            }
            chipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
