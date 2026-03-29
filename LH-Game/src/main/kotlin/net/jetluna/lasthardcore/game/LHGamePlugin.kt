package net.jetluna.lasthardcore.game

import org.bukkit.plugin.java.JavaPlugin
import net.jetluna.lasthardcore.game.commands.MsgCommand
import net.jetluna.lasthardcore.game.commands.ReviveCommand
import net.jetluna.lasthardcore.game.commands.StaffChatCommand
import net.jetluna.lasthardcore.game.listeners.ChatListener
import net.jetluna.lasthardcore.game.listeners.GameListener
import net.jetluna.lasthardcore.game.listeners.ItemCooldownListener
import net.jetluna.lasthardcore.game.listeners.ProtectionListener
import net.jetluna.lasthardcore.game.managers.ScoreboardManager
import net.jetluna.lasthardcore.game.managers.TabManager

class LHGamePlugin : JavaPlugin() {

    lateinit var tabManager: TabManager
        private set

    companion object {
        lateinit var instance: LHGamePlugin
            private set
    }

    override fun onEnable() {
        instance = this

        tabManager = TabManager(this)
        ScoreboardManager(this)

        server.pluginManager.registerEvents(GameListener(), this)
        server.pluginManager.registerEvents(ProtectionListener(), this)
        server.pluginManager.registerEvents(ChatListener(), this)
        server.pluginManager.registerEvents(ItemCooldownListener(), this)

        val msgCmd = MsgCommand()
        getCommand("msg")?.setExecutor(msgCmd)
        getCommand("w")?.setExecutor(msgCmd)
        getCommand("tell")?.setExecutor(msgCmd)
        getCommand("r")?.setExecutor(msgCmd)
        getCommand("reply")?.setExecutor(msgCmd)

        getCommand("sc")?.setExecutor(StaffChatCommand())
        getCommand("revive")?.setExecutor(ReviveCommand())

        logger.info("LH-Game успешно запущен!")
    }

    override fun onDisable() {
        logger.info("LH-Game выключен!")
    }
}