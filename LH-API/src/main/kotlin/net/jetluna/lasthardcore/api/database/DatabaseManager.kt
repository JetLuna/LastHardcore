package net.jetluna.lasthardcore.api.database

import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseManager(private val plugin: JavaPlugin) {

    private var connection: Connection? = null

    private val host = plugin.config.getString("database.host")
    private val port = plugin.config.getInt("database.port")
    private val database = plugin.config.getString("database.database")
    private val username = plugin.config.getString("database.username")
    private val password = plugin.config.getString("database.password")

    fun connect() {
        try {
            val url = "jdbc:mysql://$host:$port/$database?useSSL=false&autoReconnect=true"
            connection = DriverManager.getConnection(url, username, password)
            plugin.logger.info("Успешное подключение к базе данных MySQL!")

            createTables()
        } catch (e: SQLException) {
            plugin.logger.severe("Ошибка подключения к базе данных: ${e.message}")
        }
    }

    private fun createTables() {
        val statement = connection?.createStatement()
        statement?.execute(
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16) NOT NULL,
                rank_name VARCHAR(20) DEFAULT 'DEFAULT',
                playtime INT DEFAULT 0,
                lives INT DEFAULT 3
            );
            """.trimIndent()
        )
        try {
            statement?.execute("ALTER TABLE players ADD COLUMN playtime INT DEFAULT 0;")
        } catch (e: Exception) {
        }
        try {
            statement?.execute("ALTER TABLE players ADD COLUMN lives INT DEFAULT 3;")
        } catch (e: Exception) {
        }

        statement?.execute(
            """
            CREATE TABLE IF NOT EXISTS punishments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                target_uuid VARCHAR(36) NOT NULL,
                staff_uuid VARCHAR(36) NOT NULL,
                type VARCHAR(10) NOT NULL,
                reason TEXT,
                issue_time BIGINT NOT NULL,
                expire_time BIGINT NOT NULL,
                active BOOLEAN DEFAULT TRUE
            );
            """.trimIndent()
        )
        statement?.close()
    }

    fun getConnection(): Connection? {
        return try {
            if (connection != null && !connection!!.isClosed) {
                connection
            } else {
                connect()
                connection
            }
        } catch (e: SQLException) {
            null
        }
    }

    fun disconnect() {
        try {
            if (connection != null && !connection!!.isClosed) {
                connection!!.close()
                plugin.logger.info("Отключение от базы данных...")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}