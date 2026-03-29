package net.jetluna.lasthardcore.game.commands

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import net.jetluna.lasthardcore.api.models.Rank

class StaffChatCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        val rank = LastHardcoreAPI.instance.rankManager.getRank(sender.uniqueId)

        if (!rank.isStaff()) {
            sender.sendMessage(Component.text("§cУ вас нет прав для использования стафф-чата!"))
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(Component.text("§cИспользование: /sc <сообщение>"))
            return true
        }

        val rankName = rank.name
        val (prefixStr, nameColor) = when (rank) {
            Rank.CREATOR -> Pair("§4§l$rankName ", "§4")
            Rank.ADMIN -> Pair("§c§l$rankName ", "§c")
            Rank.MODER -> Pair("§9§l$rankName ", "§9")
            Rank.HELPER -> Pair("§a§l$rankName ", "§a")
            else -> Pair("", "§7")
        }

        val message = args.joinToString(" ")
        val format = "§8[§bSC§8] $prefixStr$nameColor${sender.name} §8>> §b$message"

        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (LastHardcoreAPI.instance.rankManager.getRank(onlinePlayer.uniqueId).isStaff()) {
                onlinePlayer.sendMessage(Component.text(format))
            }
        }
        Bukkit.getConsoleSender().sendMessage(Component.text(format))

        return true
    }
}