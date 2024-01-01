package com.iskcon.japa.storage

import java.time.LocalDateTime

data class ChatEventEntity(
    val chatId: Long = 0,
    val action: String = "",
    val isChantingNow: Boolean? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
