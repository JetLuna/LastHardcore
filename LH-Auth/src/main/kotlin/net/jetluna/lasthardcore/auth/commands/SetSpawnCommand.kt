package net.jetluna.lasthardcore.auth.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.jetluna.lasthardcore.auth.LHAuthPlugin

class SetSpawnCommand(private val plugin: LHAuthPlugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Команда только для игроков!")
            return true
        }

        if (!sender.isOp && !sender.hasPermission("lh.admin")) {
            sender.sendMessage(Component.text("У вас нет прав!", NamedTextColor.RED))
            return true
        }

        val loc = sender.location

        plugin.config.set("spawn.world", loc.world.name)
        plugin.config.set("spawn.x", loc.x)
        plugin.config.set("spawn.y", loc.y)
        plugin.config.set("spawn.z", loc.z)
        plugin.config.set("spawn.yaw", loc.yaw)
        plugin.config.set("spawn.pitch", loc.pitch)
        plugin.saveConfig()

        sender.sendMessage(Component.text("Точка спавна авторизации установлена!", NamedTextColor.GREEN))
        return true
    }
}