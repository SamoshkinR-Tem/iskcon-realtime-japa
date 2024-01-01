package com.iskcon.japa.storage

import com.iskcon.japa.logger.FileLogger.logger
import com.iskcon.japa.storage.DatabaseContract.COL_ACTION
import com.iskcon.japa.storage.DatabaseContract.COL_CHAT_ID
import com.iskcon.japa.storage.DatabaseContract.COL_ID
import com.iskcon.japa.storage.DatabaseContract.COL_IS_CHANTING_NOW
import com.iskcon.japa.storage.DatabaseContract.DB_NAME
import com.iskcon.japa.storage.DatabaseContract.DB_URL
import com.iskcon.japa.storage.DatabaseContract.PASS
import com.iskcon.japa.storage.DatabaseContract.TABLE_CHAT_RECORDS
import com.iskcon.japa.storage.DatabaseContract.TIMESTAMP
import com.iskcon.japa.storage.DatabaseContract.USER
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types

object ChatEventDao {
    private const val INSERT_SQL =
        "INSERT INTO $TABLE_CHAT_RECORDS ($COL_CHAT_ID, $COL_ACTION, $COL_IS_CHANTING_NOW, $TIMESTAMP) VALUES (?, ?, ?, ?)"
    private const val COUNT_JOIN_CLICKS_SQL = "SELECT COUNT(*) FROM $TABLE_CHAT_RECORDS WHERE $COL_IS_CHANTING_NOW = true"
    private const val COUNT_CHANTING_DEVOTEES_SQL =
        "WITH last_status AS (" +
                "SELECT $COL_CHAT_ID," +
                      "$COL_IS_CHANTING_NOW," +
                       "ROW_NUMBER() OVER (PARTITION BY $COL_CHAT_ID ORDER BY $COL_ID DESC) as rn " +
                "FROM $TABLE_CHAT_RECORDS " +
                "WHERE $COL_IS_CHANTING_NOW IS NOT NULL " +
                "AND \"$TIMESTAMP\" > now() - INTERVAL '24 hours') " +
        "SELECT COUNT(*) " +
        "FROM last_status " +
        "WHERE rn = 1 AND $COL_IS_CHANTING_NOW = true"

    fun saveChatEvent(record: ChatEventEntity) {
        DriverManager.getConnection("$DB_URL$DB_NAME", USER, PASS).use { connection ->
            logger.info("Database $DB_NAME connected.")
            connection.prepareStatement(INSERT_SQL).use { preparedStatement ->
                preparedStatement.setLong(1, record.chatId)
                preparedStatement.setString(2, record.action)
                record.isChantingNow?.let { preparedStatement.setBoolean(3, it) }
                    ?: preparedStatement.setNull(3, Types.BOOLEAN)
                preparedStatement.setTimestamp(4, Timestamp.valueOf(record.timestamp))
                preparedStatement.executeUpdate()
            }
        }
    }

    fun countChantingNow(): Short {
        try {
            DriverManager.getConnection("$DB_URL$DB_NAME", USER, PASS).use { connection ->
                logger.info("Database $DB_NAME connected.")
                connection.createStatement().use { statement ->
                    val resultSet = statement.executeQuery(COUNT_CHANTING_DEVOTEES_SQL)
                    if (resultSet.next()) {
                        val amount = resultSet.getShort(1)
                        logger.info("countChantingNow resultSet.next() $amount")
                        return amount
                    }
                }
            }
        } catch (e: SQLException) {
            logger.error("countChantingNow error: ${e.message}")
        }
        logger.error("countChantingNow returned default \"0\"")
        return 0
    }
}
