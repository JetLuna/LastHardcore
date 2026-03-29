package net.jetluna.lasthardcore.api.managers

import net.jetluna.lasthardcore.api.LastHardcoreAPI
import org.bukkit.Bukkit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlaytimeManager(private val api: LastHardcoreAPI) {

    // Кеш: UUID -> количество сыгранных минут
    private val playtimeCache = ConcurrentHashMap<UUID, Int>()

    fun loadPlayer(uuid: UUID) {
        val conn = api.databaseManager.getConnection() ?: return
        try {
            val stmt = conn.prepareStatement("SELECT playtime FROM players WHERE uuid = ?")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            if (rs.next()) {
                playtimeCache[uuid] = rs.getInt("playtime")
            } else {
                playtimeCache[uuid] = 0
            }
            rs.close()
            stmt.close()
        } catch (e: Exception) {
            api.logger.severe("Ошибка загрузки времени игры: ${e.message}")
            playtimeCache[uuid] = 0
        }
    }

    fun saveAndUnloadPlayer(uuid: UUID) {
        val minutes = playtimeCache.remove(uuid) ?: return
        Bukkit.getScheduler().runTaskAsynchronously(api, Runnable {
            val conn = api.databaseManager.getConnection() ?: return@Runnable
            try {
                val stmt = conn.prepareStatement("UPDATE players SET playtime = ? WHERE uuid = ?")
                stmt.setInt(1, minutes)
                stmt.setString(2, uuid.toString())
                stmt.executeUpdate()
                stmt.close()
            } catch (e: Exception) {
                api.logger.severe("Ошибка сохранения времени игры: ${e.message}")
            }
        })
    }

    fun getMinutes(uuid: UUID): Int {
        return playtimeCache[uuid] ?: 0
    }

    fun addMinute(uuid: UUID) {
        val current = playtimeCache[uuid] ?: 0
        playtimeCache[uuid] = current + 1
    }
}