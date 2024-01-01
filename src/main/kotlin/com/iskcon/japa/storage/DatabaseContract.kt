package com.iskcon.japa.storage

internal object DatabaseContract {
    const val DB_NAME = "iskcon_rt_japa_events_db"
    const val DB_URL = "jdbc:postgresql://localhost:5432/"
    const val USER = "samos"
    const val PASS = "35487"

    const val TABLE_CHAT_RECORDS = "chat_records"
    const val COL_ID = "row_id"
    const val COL_CHAT_ID = "chat_id"
    const val COL_ACTION = "action"
    const val COL_IS_CHANTING_NOW = "is_chanting_now"
    const val TIMESTAMP = "timestamp"
}
