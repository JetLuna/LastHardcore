package net.jetluna.lasthardcore.api.managers

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.api.models.PunishmentInfo
import net.jetluna.lasthardcore.api.models.PunishmentType
import java.util.UUID

class PunishmentManager(private val api: LastHardcoreAPI) {

    fun punish(targetUuid: UUID, staffUuid: UUID, type: PunishmentType, reason: String, durationMs: Long = -1L) {
        Bukkit.getScheduler().runTaskAsynchronously(api, Runnable {
            val issueTime = System.currentTimeMillis()
            val expireTime = if (durationMs == -1L) -1L else issueTime + durationMs

            val sql = "INSERT INTO punishments (target_uuid, staff_uuid, type, reason, issue_time, expire_time, active) VALUES (?, ?, ?, ?, ?, ?, ?)"
            val conn = api.databaseManager.getConnection() ?: return@Runnable

            try {
                val stmt = conn.prepareStatement(sql)
                stmt.setString(1, targetUuid.toString())
                stmt.setString(2, staffUuid.toString())
                stmt.setString(3, type.name)
                stmt.setString(4, reason)
                stmt.setLong(5, issueTime)
                stmt.setLong(6, expireTime)
                stmt.setBoolean(7, true)

                stmt.executeUpdate()
                stmt.close()
            } catch (e: Exception) {
                api.logger.severe("Ошибка при выдаче наказания: ${e.message}")
            }

            if (type == PunishmentType.BAN) {
                Bukkit.getScheduler().runTask(api, Runnable {
                    val player = Bukkit.getPlayer(targetUuid)
                    player?.kick(Component.text("§cВы заблокированы на сервере!\n\n§7Причина: §f$reason"))
                })
            }
        })
    }

    fun kick(targetUuid: UUID, reason: String) {
        val player = Bukkit.getPlayer(targetUuid)
        player?.kick(Component.text("§cВы были кикнуты сервером!\n\n§7Причина: §f$reason"))
    }

    fun pardon(targetUuid: UUID, type: PunishmentType) {
        Bukkit.getScheduler().runTaskAsynchronously(api, Runnable {
            val sql = "UPDATE punishments SET active = false WHERE target_uuid = ? AND type = ?"
            val conn = api.databaseManager.getConnection() ?: return@Runnable

            try {
                val stmt = conn.prepareStatement(sql)
                stmt.setString(1, targetUuid.toString())
                stmt.setString(2, type.name)
                stmt.executeUpdate()
                stmt.close()
            } catch (e: Exception) {
                api.logger.severe("Ошибка при снятии наказания: ${e.message}")
            }
        })
    }

    private fun getActivePunishmentInfo(targetUuid: UUID, type: PunishmentType): PunishmentInfo? {
        val sql = "SELECT reason, expire_time, staff_uuid FROM punishments WHERE target_uuid = ? AND type = ? AND active = TRUE"
        val conn = api.databaseManager.getConnection() ?: return null

        try {
            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, targetUuid.toString())
            stmt.setString(2, type.name)
            val rs = stmt.executeQuery()

            while (rs.next()) {
                val expireTime = rs.getLong("expire_time")
                val reason = rs.getString("reason")
                val staffUuidStr = rs.getString("staff_uuid")

                if (expireTime != -1L && System.currentTimeMillis() > expireTime) {
                    pardon(targetUuid, type)
                    continue
                }

                rs.close()
                stmt.close()
                return PunishmentInfo(reason, expireTime, UUID.fromString(staffUuidStr))
            }
            rs.close()
            stmt.close()
        } catch (e: Exception) {
            api.logger.severe("Ошибка при проверке наказания: ${e.message}")
        }
        return null
    }

    fun isBanned(uuid: UUID): PunishmentInfo? = getActivePunishmentInfo(uuid, PunishmentType.BAN)

    fun isMuted(uuid: UUID): PunishmentInfo? = getActivePunishmentInfo(uuid, PunishmentType.MUTE)
}