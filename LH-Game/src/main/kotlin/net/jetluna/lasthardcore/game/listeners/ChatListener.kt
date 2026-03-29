package net.jetluna.lasthardcore.game.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.api.models.Rank
import java.util.UUID

class ChatListener : Listener {

    private val CONSOLE_UUID = UUID(0, 0)

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val api = LastHardcoreAPI.instance

        val muteInfo = api.punishmentManager.isMuted(player.uniqueId)

        if (muteInfo != null) {
            event.isCancelled = true

            val staffRank = api.rankManager.getOfflineRank(muteInfo.staffUuid)
            val staffName = if (muteInfo.staffUuid == CONSOLE_UUID) "СЕРВЕР" else Bukkit.getOfflinePlayer(muteInfo.staffUuid).name ?: "Unknown"

            val (staffPrefixStr, staffNameColor) = if (muteInfo.staffUuid == CONSOLE_UUID) {
                Pair("§4§lСЕРВЕР ", "§4")
            } else {
                val sRankName = staffRank.name
                when (staffRank) {
                    Rank.CREATOR -> Pair("§4§l$sRankName ", "§4")
                    Rank.ADMIN -> Pair("§c§l$sRankName ", "§c")
                    Rank.MODER -> Pair("§9§l$sRankName ", "§9")
                    Rank.HELPER -> Pair("§a§l$sRankName ", "§a")
                    else -> Pair("", "§7")
                }
            }
            val formattedStaff = "$staffPrefixStr$staffNameColor$staffName"

            val remain = formatRemainingTime(muteInfo.expireTime)
            player.sendMessage(Component.text(
                "§cДоступ к чату заблокирован!\n\n" +
                        "§7Причина: §f${muteInfo.reason}\n" +
                        "§7Срок наказания: §f$remain\n" +
                        "§7Выдал: §r$formattedStaff\n\n" +
                        "§7Обжаловать: §bdiscord.gg/lasthardcore"
            ))
            return
        }

        val rank = api.rankManager.getRank(player.uniqueId)
        val messageText = LegacyComponentSerializer.legacyAmpersand().serialize(event.message())

        val rankName = rank.name
        val (prefixStr, nameColor) = when (rank) {
            Rank.CREATOR -> Pair("§4§l$rankName ", "§4")
            Rank.ADMIN -> Pair("§c§l$rankName ", "§c")
            Rank.MODER -> Pair("§9§l$rankName ", "§9")
            Rank.HELPER -> Pair("§a§l$rankName ", "§a")
            else -> Pair("", "§7")
        }

        val msgColor = if (rank != Rank.DEFAULT) "§f" else "§7"
        val formatString = "$prefixStr$nameColor${player.name} §8>> $msgColor$messageText"

        event.isCancelled = true

        val finalComponent = Component.text(formatString)
        for (recipient in event.viewers()) {
            recipient.sendMessage(finalComponent)
        }
    }

    private fun formatRemainingTime(expireTime: Long): String {
        if (expireTime == -1L) return "Навсегда"
        val millis = expireTime - System.currentTimeMillis()
        if (millis <= 0) return "Снимается..."
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days дн. ${hours % 24} ч."
            hours > 0 -> "$hours ч. ${minutes % 60} мин."
            minutes > 0 -> "$minutes мин."
            else -> "$seconds сек."
        }
    }
}