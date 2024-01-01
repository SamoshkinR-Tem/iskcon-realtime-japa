package com.iskcon.japa.bot

import com.iskcon.japa.bot.BotActions.*
import com.iskcon.japa.logger.FileLogger.logger
import com.iskcon.japa.storage.ChatEventDao
import com.iskcon.japa.storage.ChatEventEntity
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object JapaTelegramBot : TelegramLongPollingBot() {

    fun launchBot() {
        logger.info("launchBot")
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(JapaTelegramBot)
    }

    override fun getBotToken(): String {
        logger.info("getBotToken")
        return ""
    }

    override fun getBotUsername(): String {
        logger.info("getBotUsername")
        return ""
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info("onUpdateReceived")
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
        logger.info("handleUserMessage")
        userMsg?.let {
            when {
                it.lowercase().contains(START.key)
                        || it.lowercase().contains(BTN_UPDATE.key) -> {
                    saveChatEventToDatabase(
                        ChatEventEntity(
                            chatId = chatId,
                            action = BTN_UPDATE.key,
                        )
                    ).onSuccess { sendAnswer(chatId) }
                }

                it.lowercase().contains(BTN_JOIN.key) -> {
                    saveChatEventToDatabase(
                        ChatEventEntity(
                            chatId = chatId,
                            action = BTN_JOIN.key,
                            isChantingNow = true,
                        )
                    ).onSuccess { sendAnswer(chatId) }
                }

                it.lowercase().contains(BTN_LEAVE.key) -> {
                    saveChatEventToDatabase(
                        ChatEventEntity(
                            chatId = chatId,
                            action = BTN_LEAVE.key,
                            isChantingNow = false,
                        )
                    ).onSuccess { sendAnswer(chatId) }
                }

                else -> Unit
            }
        }
    }

    private fun sendAnswer(chatId: Long) = runCatching {
        logger.info("sendAnswer")
        val photo = createPhotoMessage(chatId, "srila-prabhupada-blissful-japa")
        executeAsync(photo)

        sendApiMethodAsync(
            createMessage(
                chatId,
                "Chanting Devotees amount:   *${ChatEventDao.countChantingNow()}*",
                mapOf(
                    BTN_JOIN.key to "Join",
                    BTN_LEAVE.key to "Leave",
                    BTN_UPDATE.key to "Update",
                )
            )
        )
    }.onFailure { logger.error(it.message) }

    private fun saveChatEventToDatabase(event: ChatEventEntity) = runCatching {
        ChatEventDao.saveChatEvent(event)
        logger.info("ChatEvent was successfully saved to the database.")
    }.onFailure {
        logger.error("ChatEvent db saving error: ${it.message}")
    }
}
