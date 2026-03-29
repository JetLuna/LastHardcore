package net.jetluna.lasthardcore.api.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import java.text.SimpleDateFormat
import java.util.Date

class PunishmentListener(private val api: LastHardcoreAPI) : Listener {

    private fun formatTime(time: Long): String {
        if (time == -1L) return "Никогда (Навсегда)"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        return sdf.format(Date(time))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uuid = event.uniqueId
        val banInfo = api.punishmentManager.isBanned(uuid)

        if (banInfo != null) {
            val staffRank = api.rankManager.getOfflineRank(banInfo.staffUuid)
            val staffPrefix = if (staffRank.level == 0) "§4[Сервер] " else staffRank.prefix
            val staffName = api.rankManager.getNameByUuid(banInfo.staffUuid)
            val timeStr = formatTime(banInfo.expireTime)

            val kickMessage = Component.text(
                "§cДоступ к серверу заблокирован!\n\n" +
                        "§7Причина: §f${banInfo.reason}\n" +
                        "§7Истекает через: §f$timeStr\n\n" +
                        "§7Выдал: §r$staffPrefix$staffName\n" +
                        "§7Обжаловать: §bdiscord.gg/lasthardcore"
            )
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val muteInfo = api.punishmentManager.isMuted(player.uniqueId)

        if (muteInfo != null) {
            event.isCancelled = true
            val staffRank = api.rankManager.getOfflineRank(muteInfo.staffUuid)
            val staffPrefix = if (staffRank.level == 0) "§4[Сервер] " else staffRank.prefix
            val staffName = api.rankManager.getNameByUuid(muteInfo.staffUuid)
            val timeStr = formatTime(muteInfo.expireTime)

            player.sendMessage(Component.text(
                "§cДоступ к чату заблокирован!\n\n" +
                        "§7Причина: §f${muteInfo.reason}\n" +
                        "§7Истекает через: §f$timeStr\n\n" +
                        "§7Выдал: §r$staffPrefix$staffName"
            ))
        }
    }
}