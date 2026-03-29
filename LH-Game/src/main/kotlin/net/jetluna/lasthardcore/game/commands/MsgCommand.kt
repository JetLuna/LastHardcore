package net.jetluna.lasthardcore.game.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.jetluna.lasthardcore.api.LastHardcoreAPI
import java.util.UUID

class MsgCommand : CommandExecutor {

    private val replies = mutableMapOf<UUID, UUID>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        val cmd = label.lowercase()

        var target: Player? = null
        var message = ""

        if (cmd == "msg" || cmd == "tell" || cmd == "w") {
            if (args.size < 2) {
                sender.sendMessage(Component.text("§cИспользование: /msg <игрок> <сообщение>"))
                return true
            }
            target = Bukkit.getPlayer(args[0])
            if (target == null) {
                sender.sendMessage(Component.text("§cИгрок не найден!"))
                return true
            }
            message = args.drop(1).joinToString(" ")
        } else if (cmd == "r" || cmd == "reply") {
            if (args.isEmpty()) {
                sender.sendMessage(Component.text("§cИспользование: /r <сообщение>"))
                return true
            }
            val targetUuid = replies[sender.uniqueId]
            if (targetUuid == null) {
                sender.sendMessage(Component.text("§cВам некому отвечать!"))
                return true
            }
            target = Bukkit.getPlayer(targetUuid)
            if (target == null) {
                sender.sendMessage(Component.text("§cИгрок не в сети!"))
                return true
            }
            message = args.joinToString(" ")
        }

        if (target != null) {
            sendPrivateMessage(sender, target, message)
        }
        return true
    }

    private fun sendPrivateMessage(sender: Player, target: Player, message: String) {
        val senderRank = LastHardcoreAPI.instance.rankManager.getRank(sender.uniqueId)
        val targetRank = LastHardcoreAPI.instance.rankManager.getRank(target.uniqueId)

        replies[sender.uniqueId] = target.uniqueId
        replies[target.uniqueId] = sender.uniqueId

        val senderNameFormat = "${senderRank.prefix}${sender.name}"
        val targetNameFormat = "${targetRank.prefix}${target.name}"

        val senderComp = Component.text("§8[§7Я §8-> §r$targetNameFormat§8] §f$message")
            .clickEvent(ClickEvent.suggestCommand("/msg ${target.name} "))
            .hoverEvent(HoverEvent.showText(Component.text("§7Нажмите, чтобы ответить")))

        val targetComp = Component.text("§8[§r$senderNameFormat §8-> §7Мне§8] §f$message")
            .clickEvent(ClickEvent.suggestCommand("/msg ${sender.name} "))
            .hoverEvent(HoverEvent.showText(Component.text("§7Нажмите, чтобы ответить")))

        sender.sendMessage(senderComp)
        target.sendMessage(targetComp)

        target.playSound(target.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2f)
    }
}