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

class JapaTelegramBotDatabase {

    private fun databaseNotExists(): Boolean {
        try {
            DriverManager.getConnection("$DB_URL$DB_NAME", USER, PASS).use { return false }
        } catch (e: SQLException) {
            if (e.sqlState == "3D000") { // Код состояния SQL для "База данных не существует"
                logger.info("Database $DB_NAME is not exist | sqlState ${e.sqlState}.")
                return true
            } else {
                logger.info("Database $DB_NAME unknownException ${e.message} | sqlState ${e.sqlState}.")
                throw e
            }
        }
    }

    fun createDatabaseIfNotExists() {
        if (databaseNotExists()) {
            try {
                DriverManager.getConnection("${DB_URL}postgres", USER, PASS).use { connection ->
                    connection.createStatement().use { statement ->
                        // Database creation
                        val sqlCreateDB = "CREATE DATABASE $DB_NAME"
                        statement.executeUpdate(sqlCreateDB)
                        logger.info("Database $DB_NAME created successfully.")

                        // Created database connection
                        DriverManager.getConnection("$DB_URL$DB_NAME", USER, PASS).use { dbConnection ->
                            logger.info("Database $DB_NAME connected.")
                            dbConnection.createStatement().use { dbStatement ->
                                // Table 'chat_records' creation
                                val sqlCreateTable = """
                                    CREATE TABLE $TABLE_CHAT_RECORDS (
                                        $COL_ID SERIAL PRIMARY KEY,
                                        $COL_CHAT_ID BIGINT NOT NULL,
                                        $COL_ACTION VARCHAR(255) NOT NULL,
                                        $COL_IS_CHANTING_NOW BOOLEAN,
                                        $TIMESTAMP TIMESTAMP NOT NULL)
                                """.trimIndent()
                                dbStatement.executeUpdate(sqlCreateTable)
                                logger.info("Table chat_records created successfully in $DB_NAME.")
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                logger.error("DB creation error: ${e.message}")
            }
        } else {
            logger.info("Database $DB_NAME already exist.")
        }
    }
}
