package com.iskcon.japa

import com.iskcon.japa.bot.JapaTelegramBot
import com.iskcon.japa.storage.JapaTelegramBotDatabase

fun main() {
    JapaTelegramBot.launchBot()
    JapaTelegramBotDatabase().createDatabaseIfNotExists()
}
