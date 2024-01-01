package com.iskcon.japa.bot

enum class BotActions(val key: String) {
    START("/start"),
    BTN_JOIN("btn_join"),
    BTN_LEAVE("btn_leave"),
    BTN_UPDATE("btn_update"),
    UNKNOWN("unknown");

    companion object {
        fun parse(input: String?) = entries
            .find { it.key == input } ?: UNKNOWN
    }
}
