package net.jetluna.lasthardcore.api.commands

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.api.models.Rank

class SetRankCommand(private val api: LastHardcoreAPI) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lh.admin") && !sender.isOp) {
            sender.sendMessage(Component.text("§cУ вас нет прав!"))
            return true
        }

        if (args.size < 2) {
            sender.sendMessage(Component.text("§cИспользование: /setrank <игрок> <ранг>"))
            val availableRanks = Rank.values().joinToString(", ") { it.name }
            sender.sendMessage(Component.text("§7Доступные ранги: §f$availableRanks"))
            return true
        }

        val targetName = args[0]
        val rankNameInput = args[1].uppercase()

        val newRank = try {
            Rank.valueOf(rankNameInput)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(Component.text("§cРанг не найден!"))
            return true
        }

        @Suppress("DEPRECATION")
        val targetOffline = Bukkit.getOfflinePlayer(targetName)
        val targetUuid = targetOffline.uniqueId

        api.rankManager.setRank(targetUuid, newRank)

        val rName = newRank.name
        val (prefixStr, nameColor) = when (newRank) {
            Rank.CREATOR -> Pair("§4§l$rName ", "§4")
            Rank.ADMIN -> Pair("§c§l$rName ", "§c")
            Rank.MODER -> Pair("§9§l$rName ", "§9")
            Rank.HELPER -> Pair("§a§l$rName ", "§a")
            else -> Pair("", "§7")
        }

        val targetRealName = targetOffline.name ?: targetName
        val formattedTarget = "$prefixStr$nameColor$targetRealName"

        sender.sendMessage(Component.text("§aВы установили ранг $rName игроку $targetRealName!"))

        val targetPlayer = Bukkit.getPlayer(targetUuid)
        targetPlayer?.sendMessage(Component.text("§aВаш ранг был изменен на: $formattedTarget"))

        return true
    }
}