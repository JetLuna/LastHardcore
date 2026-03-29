package net.jetluna.lasthardcore.game.commands

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import net.jetluna.lasthardcore.api.LastHardcoreAPI

class ReviveCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lh.admin")) {
            sender.sendMessage(Component.text("§cУ вас нет прав!"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(Component.text("§cИспользование: /revive <игрок>"))
            return true
        }

        val target = Bukkit.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage(Component.text("§cИгрок не найден или оффлайн!"))
            return true
        }

        val livesManager = LastHardcoreAPI.instance.livesManager

        // Восстанавливаем 3 жизни
        livesManager.setLives(target.uniqueId, 3)
        livesManager.updateMaxHealth(target)

        // Вытаскиваем из спектра
        if (target.gameMode == GameMode.SPECTATOR) {
            target.gameMode = GameMode.SURVIVAL
        }

        sender.sendMessage(Component.text("§aВы успешно воскресили игрока ${target.name}!"))
        target.sendMessage(Component.text("§a§lВАС ВОСКРЕСИЛИ! §fВсе жизни восстановлены."))
        return true
    }
}