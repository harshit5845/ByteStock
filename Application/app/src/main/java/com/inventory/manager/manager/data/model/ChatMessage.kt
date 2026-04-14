package com.inventory.manager.data.model

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestions: List<String> = emptyList()
)
