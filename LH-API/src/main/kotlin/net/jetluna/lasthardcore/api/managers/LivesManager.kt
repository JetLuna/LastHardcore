package net.jetluna.lasthardcore.api.managers

import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LivesManager(private val api: LastHardcoreAPI) {

    private val livesCache = ConcurrentHashMap<UUID, Int>()

    private val defaultLives = 3
    private val hpReductionPerDeath = 3.5

    fun loadPlayer(uuid: UUID) {
        val conn = api.databaseManager.getConnection() ?: return
        try {
            val stmt = conn.prepareStatement("SELECT lives FROM players WHERE uuid = ?")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            livesCache[uuid] = if (rs.next()) rs.getInt("lives") else defaultLives
            rs.close()
            stmt.close()
        } catch (e: Exception) {
            livesCache[uuid] = defaultLives
        }
    }

    fun saveAndUnloadPlayer(uuid: UUID) {
        val lives = livesCache.remove(uuid) ?: return
        Bukkit.getScheduler().runTaskAsynchronously(api, Runnable {
            val conn = api.databaseManager.getConnection() ?: return@Runnable
            try {
                val stmt = conn.prepareStatement("UPDATE players SET lives = ? WHERE uuid = ?")
                stmt.setInt(1, lives)
                stmt.setString(2, uuid.toString())
                stmt.executeUpdate()
                stmt.close()
            } catch (e: Exception) {}
        })
    }

    fun getLives(uuid: UUID): Int = livesCache[uuid] ?: defaultLives

    fun setLives(uuid: UUID, amount: Int) {
        livesCache[uuid] = amount
    }

    fun removeLife(uuid: UUID) {
        setLives(uuid, getLives(uuid) - 1)
    }

    fun updateMaxHealth(player: Player) {
        val currentLives = getLives(player.uniqueId)
        val lostLives = Math.max(0, defaultLives - currentLives)

        var newMaxHealth = 20.0 - (lostLives * hpReductionPerDeath)
        if (newMaxHealth < 1.0) newMaxHealth = 1.0

        val attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
        attribute?.baseValue = newMaxHealth
    }
}