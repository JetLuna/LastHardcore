package net.jetluna.lasthardcore.api.managers

import org.bukkit.Bukkit
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.api.models.Rank
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RankManager(private val api: LastHardcoreAPI) {

    private val playerRanks = ConcurrentHashMap<UUID, Rank>()

    fun loadPlayer(uuid: UUID, name: String) {
        val conn = api.databaseManager.getConnection() ?: return
        var rank = Rank.DEFAULT

        try {
            val selectStmt = conn.prepareStatement("SELECT rank_name FROM players WHERE uuid = ?")
            selectStmt.setString(1, uuid.toString())
            val rs = selectStmt.executeQuery()

            if (rs.next()) {
                val rankName = rs.getString("rank_name")
                try {
                    rank = Rank.valueOf(rankName)
                } catch (e: IllegalArgumentException) {
                    api.logger.warning("У игрока $name неизвестный ранг: $rankName. Установлен DEFAULT.")
                }
            } else {
                val insertStmt = conn.prepareStatement("INSERT INTO players (uuid, name, rank_name) VALUES (?, ?, ?)")
                insertStmt.setString(1, uuid.toString())
                insertStmt.setString(2, name)
                insertStmt.setString(3, rank.name)
                insertStmt.executeUpdate()
                insertStmt.close()
            }
            rs.close()
            selectStmt.close()

            playerRanks[uuid] = rank

        } catch (e: Exception) {
            api.logger.severe("Ошибка при загрузке профиля игрока $name: ${e.message}")
        }
    }

    fun unloadPlayer(uuid: UUID) {
        playerRanks.remove(uuid)
    }

    fun getRank(uuid: UUID): Rank {
        return playerRanks[uuid] ?: Rank.DEFAULT
    }

    fun setRank(uuid: UUID, newRank: Rank) {
        if (playerRanks.containsKey(uuid)) {
            playerRanks[uuid] = newRank
        }

        Bukkit.getScheduler().runTaskAsynchronously(api, Runnable {
            val conn = api.databaseManager.getConnection() ?: return@Runnable
            try {
                val stmt = conn.prepareStatement("UPDATE players SET rank_name = ? WHERE uuid = ?")
                stmt.setString(1, newRank.name)
                stmt.setString(2, uuid.toString())
                stmt.executeUpdate()
                stmt.close()
            } catch (e: Exception) {
                api.logger.severe("Ошибка при выдаче ранга: ${e.message}")
            }
        })
    }

    fun getOfflineRank(uuid: UUID): Rank {
        playerRanks[uuid]?.let { return it }

        // Специальный UUID для консоли
        if (uuid == UUID(0, 0)) return Rank.CREATOR

        var rank = Rank.DEFAULT
        val conn = api.databaseManager.getConnection() ?: return rank
        try {
            val stmt = conn.prepareStatement("SELECT rank_name FROM players WHERE uuid = ?")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            if (rs.next()) {
                rank = try { Rank.valueOf(rs.getString("rank_name")) } catch (e: Exception) { Rank.DEFAULT }
            }
            rs.close()
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rank
    }

    fun getNameByUuid(uuid: UUID): String {
        if (uuid == UUID(0, 0)) return "Консоль"

        val player = Bukkit.getPlayer(uuid)
        if (player != null) return player.name

        var name = "Неизвестно"
        val conn = api.databaseManager.getConnection() ?: return name
        try {
            val stmt = conn.prepareStatement("SELECT name FROM players WHERE uuid = ?")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            if (rs.next()) {
                name = rs.getString("name")
            }
            rs.close()
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return name
    }
}