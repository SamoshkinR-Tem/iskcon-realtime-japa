package com.iskcon.japa.bot

import com.iskcon.japa.logger.FileLogger.logger
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun createMessage(chatId: Long, text: String): SendMessage {
    logger.info("createMessage")
    val msg = SendMessage()
    msg.text = String(text.toByteArray(), Charsets.UTF_8)
    msg.parseMode = "markdown"
    msg.chatId = chatId.toString()
    return msg
}

fun createMessage(chatId: Long, text: String, buttons: Map<String, String>): SendMessage {
    logger.info("createMessage with buttons")
    val msg = createMessage(chatId, text)
    if (buttons.isNotEmpty()) {
        attachButtonsTile(msg, buttons)
    }
    return msg
}

private fun attachButtonsTile(msg: SendMessage, buttons: Map<String, String>) {
    logger.info("attachButtonsTile")
    val markup = InlineKeyboardMarkup()
    val keyboard: MutableList<List<InlineKeyboardButton>> = ArrayList()

    val tempRow: MutableList<InlineKeyboardButton> = ArrayList()
    for ((buttonIndex, buttonId) in buttons.keys.withIndex()) {
        buttons[buttonId]?.let { buttonText ->

            val button = InlineKeyboardButton()
            button.text = String(buttonText.toByteArray(), StandardCharsets.UTF_8)
            button.callbackData = buttonId

            tempRow.add(button)

            // Добавляем ряд после каждых двух кнопок или при добавлении последней кнопки
            if ((buttonIndex + 1) % 2 == 0 || buttonIndex == buttons.keys.size - 1) {
                keyboard.add(ArrayList(tempRow))
                tempRow.clear()
            }
        }
    }

    markup.keyboard = keyboard
    msg.replyMarkup = markup
}

fun createPhotoMessage(chatId: Long, name: String): SendPhoto {
    try {
        val photo = SendPhoto()
        val inputFile = InputFile()
        inputFile.setMedia(Files.newInputStream(Path.of("images/$name.jpg")), name)
        photo.photo = inputFile
        photo.setChatId(chatId)
        logger.info("createPhotoMessage")
        return photo
    } catch (e: IOException) {
        throw RuntimeException("Can't create photo message!")
    }
}
