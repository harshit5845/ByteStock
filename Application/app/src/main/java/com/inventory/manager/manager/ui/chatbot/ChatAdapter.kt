package com.inventory.manager.ui.chatbot

import android.view.*; import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter; import androidx.recyclerview.widget.RecyclerView
import com.inventory.manager.data.model.ChatMessage
import com.inventory.manager.databinding.ItemChatBotBinding
import com.inventory.manager.databinding.ItemChatUserBinding
import java.text.SimpleDateFormat; import java.util.*

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {
    companion object {
        const val USER = 0; const val BOT = 1
        val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
            override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
        }
        val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
    }
    override fun getItemViewType(pos: Int) = if (getItem(pos).isUser) USER else BOT
    inner class UserVH(private val b: ItemChatUserBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessage) { b.tvMessage.text = m.text; b.tvTime.text = fmt.format(Date(m.timestamp)) }
    }
    inner class BotVH(private val b: ItemChatBotBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessage) { b.tvMessage.text = m.text; b.tvTime.text = fmt.format(Date(m.timestamp)) }
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(p.context)
        return if (t == USER) UserVH(ItemChatUserBinding.inflate(inf, p, false))
               else BotVH(ItemChatBotBinding.inflate(inf, p, false))
    }
    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        if (h is UserVH) h.bind(getItem(pos)) else (h as BotVH).bind(getItem(pos))
    }
}
