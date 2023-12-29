package com.iskcon.japa.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object JapaTelegramBot : TelegramLongPollingBot() {

    private val chantingDevotees = mutableSetOf<Long>()

    fun launchBot() {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(JapaTelegramBot)
    }

    override fun getBotToken(): String {
        return ""
    }

    override fun getBotUsername(): String {
        return ""
    }

    override fun onUpdateReceived(update: Update?) {
        update?.let {
            val chatId: Long
            if (it.hasMessage()) { // on user text message
                chatId = update.message.from.id
                handleUserMessage(chatId, update.message.text)
            } else if (it.hasCallbackQuery()) { // on user button click
                chatId = update.callbackQuery.from.id
                handleUserMessage(chatId, update.callbackQuery.data)
            }
        }
    }

    private fun handleUserMessage(chatId: Long, userMsg: String?) {
        userMsg?.let {
            when {
                it.lowercase().contains("/start")
                        || it.lowercase().contains("btn_update") -> {
                    sendAnswer(chatId)
                }

                it.lowercase().contains("btn_join") -> {
                    chantingDevotees.add(chatId)
                    sendAnswer(chatId)
                }

                it.lowercase().contains("btn_leave") -> {
                    chantingDevotees.remove(chatId)
                    sendAnswer(chatId)
                }

                else -> Unit
            }
        }
    }

    private fun sendAnswer(chatId: Long) {
        val photo = createPhotoMessage(chatId, "srila-prabhupada-blissful-japa")
        executeAsync(photo)

        sendApiMethodAsync(
            createMessage(
                chatId,
                "Chanting Devotees amount:   *${chantingDevotees.size}*",
                mapOf(
                    "btn_join" to "Join",
                    "btn_leave" to "Leave",
                    "btn_update" to "Update",
                )
            )
        )
    }
}