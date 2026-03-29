package net.jetluna.lasthardcore.api.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.api.models.PunishmentType
import net.jetluna.lasthardcore.api.models.Rank
import java.util.UUID

class PunishCommands(private val api: LastHardcoreAPI) : CommandExecutor {

    private val CONSOLE_UUID = UUID(0, 0)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lh.staff")) {
            sender.sendMessage(Component.text("У вас нет прав для использования этой команды!", NamedTextColor.RED))
            return true
        }

        val cmd = command.name.lowercase()

        if (args.isEmpty()) {
            if (cmd == "unban" || cmd == "unmute" || cmd == "kick") {
                sender.sendMessage(Component.text("Использование: /$label <игрок> [причина]", NamedTextColor.RED))
            } else {
                sender.sendMessage(Component.text("Использование: /$label <игрок> [время 1m/1h/1d] [причина]", NamedTextColor.RED))
            }
            return true
        }

        val targetName = args[0]
        val staffUuid = if (sender is Player) sender.uniqueId else CONSOLE_UUID
        val staffName = sender.name

        @Suppress("DEPRECATION")
        val targetOffline = Bukkit.getOfflinePlayer(targetName)
        val targetUuid = targetOffline.uniqueId

        var durationMillis = -1L
        var reasonStartIdx = 1

        if (cmd == "ban" || cmd == "mute") {
            if (args.size > 1) {
                val parsedTime = parseTime(args[1])
                if (parsedTime > 0) {
                    durationMillis = parsedTime
                    reasonStartIdx = 2
                }
            }
        }

        val reason = if (args.size > reasonStartIdx) args.drop(reasonStartIdx).joinToString(" ") else "Нарушение правил"

        Bukkit.getScheduler().runTaskAsynchronously(api, Runnable {
            val staffRank = api.rankManager.getOfflineRank(staffUuid)
            val targetRank = api.rankManager.getOfflineRank(targetUuid)

            if (sender is Player && !sender.isOp) {
                if (targetRank.level >= staffRank.level) {
                    sender.sendMessage(Component.text("§cВы не можете наказать игрока с рангом ${targetRank.name} или выше!"))
                    return@Runnable
                }
            }

            val (staffPrefixStr, staffNameColor) = if (staffUuid == CONSOLE_UUID) {
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

            val tRankName = targetRank.name
            val (targetPrefixStr, targetNameColor) = when (targetRank) {
                Rank.CREATOR -> Pair("§4§l$tRankName ", "§4")
                Rank.ADMIN -> Pair("§c§l$tRankName ", "§c")
                Rank.MODER -> Pair("§9§l$tRankName ", "§9")
                Rank.HELPER -> Pair("§a§l$tRankName ", "§a")
                else -> Pair("", "§7")
            }
            val formattedTarget = "$targetPrefixStr$targetNameColor$targetName"

            val timeStr = if (durationMillis == -1L) "Навсегда" else formatTime(durationMillis)
            val actionName = when (cmd) {
                "ban" -> if (durationMillis == -1L) "перманентный бан" else "временный бан"
                "mute" -> if (durationMillis == -1L) "перманентный мут" else "временный мут"
                "kick" -> "кик"
                else -> "наказание"
            }

            val staffMessage = Component.text("$formattedStaff §fвыдал $actionName игроку $formattedTarget §fна §e$timeStr §fпо причине §e$reason")

            when (cmd) {
                "ban" -> {
                    api.punishmentManager.punish(targetUuid, staffUuid, PunishmentType.BAN, reason, durationMillis)
                    Bukkit.getScheduler().runTask(api, Runnable {
                        val player = Bukkit.getPlayer(targetUuid)
                        player?.kick(Component.text(
                            "§cДоступ к серверу заблокирован!\n\n" +
                                    "§7Причина: §f$reason\n" +
                                    "§7Срок наказания: §f$timeStr\n\n" +
                                    "§7Выдал: §r$formattedStaff\n" +
                                    "§7Обжаловать: §bdiscord.gg/lasthardcore"
                        ))
                    })
                }
                "mute" -> api.punishmentManager.punish(targetUuid, staffUuid, PunishmentType.MUTE, reason, durationMillis)
                "kick" -> Bukkit.getScheduler().runTask(api, Runnable { api.punishmentManager.kick(targetUuid, reason) })
                "unban" -> { api.punishmentManager.pardon(targetUuid, PunishmentType.BAN); sender.sendMessage(Component.text("§aСнят бан.")) }
                "unmute" -> { api.punishmentManager.pardon(targetUuid, PunishmentType.MUTE); sender.sendMessage(Component.text("§aСнят мут.")) }
            }

            if (cmd in listOf("ban", "mute", "kick")) {
                for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                    if (api.rankManager.getRank(onlinePlayer.uniqueId).isStaff()) {
                        onlinePlayer.sendMessage(staffMessage)
                    }
                }
                Bukkit.getConsoleSender().sendMessage(staffMessage)
            }
        })

        return true
    }

    private fun parseTime(timeString: String): Long {
        val regex = "([0-9]+)([smhd])".toRegex()
        val match = regex.find(timeString) ?: return -1L
        val (value, unit) = match.destructured
        val amount = value.toLongOrNull() ?: return -1L

        return when (unit) {
            "s" -> amount * 1000L
            "m" -> amount * 60 * 1000L
            "h" -> amount * 60 * 60 * 1000L
            "d" -> amount * 24 * 60 * 60 * 1000L
            else -> -1L
        }
    }

    private fun formatTime(millis: Long): String {
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