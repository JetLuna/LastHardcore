package net.jetluna.lasthardcore.auth.managers

import net.jetluna.lasthardcore.api.LastHardcoreAPI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AuthManager {
    private val loggedInPlayers = ConcurrentHashMap.newKeySet<UUID>()

    init {
        createTable()
    }

    private fun createTable() {
        val conn = LastHardcoreAPI.instance.databaseManager.getConnection() ?: return
        try {
            val stmt = conn.createStatement()
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS auth_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    password_hash VARCHAR(60) NOT NULL,
                    reg_date BIGINT NOT NULL,
                    last_login BIGINT NOT NULL
                );
                """.trimIndent()
            )
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLoggedIn(uuid: UUID): Boolean {
        return loggedInPlayers.contains(uuid)
    }

    fun login(uuid: UUID) {
        loggedInPlayers.add(uuid)
    }

    fun logout(uuid: UUID) {
        loggedInPlayers.remove(uuid)
    }

    fun isRegistered(uuid: UUID): Boolean {
        val conn = net.jetluna.lasthardcore.api.LastHardcoreAPI.instance.databaseManager.getConnection() ?: return false
        var registered = false
        try {
            val stmt = conn.prepareStatement("SELECT 1 FROM auth_data WHERE uuid = ?")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            registered = rs.next()
            rs.close()
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return registered
    }
}