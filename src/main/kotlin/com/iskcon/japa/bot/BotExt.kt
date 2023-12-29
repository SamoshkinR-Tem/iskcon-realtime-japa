package com.iskcon.japa.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun TelegramLongPollingBot.getChatId(update: Update): Long? {
    if (update.hasMessage()) {
        return update.message.from.id
    }

    if (update.hasCallbackQuery()) {
        return update.callbackQuery.from.id
    }

    return null
}

fun TelegramLongPollingBot.createMessage(chatId: Long, text: String): SendMessage {
    val msg = SendMessage()
    msg.text = String(text.toByteArray(), Charsets.UTF_8)
    msg.parseMode = "markdown"
    msg.chatId = chatId.toString()
    return msg
}

fun TelegramLongPollingBot.createMessage(chatId: Long, text: String, buttons: Map<String, String>): SendMessage {
    val msg = createMessage(chatId, text)
    if (buttons.isNotEmpty()) {
        attachButtonsTile(msg, buttons)
    }
    return msg
}

/*
private fun attachButtonsColumn(msg: SendMessage, buttons: Map<String, String>) {
    val markup = InlineKeyboardMarkup()
    val keyboard: MutableList<List<InlineKeyboardButton>> = ArrayList()

    for (buttonId in buttons.keys) {
        buttons[buttonId]?.let { buttonText ->

            val button = InlineKeyboardButton()
            button.text = String(buttonText.toByteArray(), StandardCharsets.UTF_8)
            button.callbackData = buttonId

            keyboard.add(listOf(button))
        }
    }

    markup.keyboard = keyboard
    msg.replyMarkup = markup
}
*/

private fun attachButtonsTile(msg: SendMessage, buttons: Map<String, String>) {
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

fun TelegramLongPollingBot.createPhotoMessage(chatId: Long, name: String): SendPhoto {
    try {
        val photo = SendPhoto()
        val inputFile = InputFile()
        inputFile.setMedia(Files.newInputStream(Path.of("images/$name.jpg")), name)
        photo.photo = inputFile
        photo.setChatId(chatId)
        return photo
    } catch (e: IOException) {
        throw RuntimeException("Can't create photo message!")
    }
}

/*
fun TelegramLongPollingBot.createPhotoMessageScaled(chatId: Long, name: String): SendPhoto {
    try {
        val originalPath = Path.of("images/$name.jpg")
        val resizedPath = Path.of("images/resized_$name.jpg")

        // Масштабируем изображение
        resizeImage(originalPath, resizedPath, 350) // 800 - это желаемая ширина

        val photo = SendPhoto()
        photo.setChatId(chatId)
        photo.setPhoto(InputFile(Files.newInputStream(resizedPath), name))

        return photo
    } catch (e: Exception) {
        throw RuntimeException("Can't create or resize photo message!", e)
    }
}

private fun resizeImage(originalPath: Path, outputPath: Path, width: Int) {
    val originalImage: BufferedImage = ImageIO.read(originalPath.toFile())
    val height = (width * originalImage.height) / originalImage.width
    val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = resizedImage.createGraphics()
    g.drawImage(originalImage, 0, 0, width, height, null)
    g.dispose()
    ImageIO.write(resizedImage, "jpg", outputPath.toFile())
}
*/
