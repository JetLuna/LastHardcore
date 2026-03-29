package net.jetluna.lasthardcore.api

import net.jetluna.lasthardcore.api.commands.PunishCommands
import net.jetluna.lasthardcore.api.commands.SetRankCommand
import org.bukkit.plugin.java.JavaPlugin
import net.jetluna.lasthardcore.api.database.DatabaseManager
import net.jetluna.lasthardcore.api.listeners.PlayerListener
import net.jetluna.lasthardcore.api.listeners.PunishmentListener
import net.jetluna.lasthardcore.api.managers.LivesManager
import net.jetluna.lasthardcore.api.managers.PlaytimeManager
import net.jetluna.lasthardcore.api.managers.PunishmentManager
import net.jetluna.lasthardcore.api.managers.RankManager

class LastHardcoreAPI : JavaPlugin() {
    lateinit var livesManager: LivesManager
        private set

    lateinit var playtimeManager: PlaytimeManager
        private set

    lateinit var rankManager: RankManager
        private set

    lateinit var punishmentManager: PunishmentManager
        private set

    lateinit var databaseManager: DatabaseManager
        private set

    companion object {
        lateinit var instance: LastHardcoreAPI
            private set
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        logger.info("Подключение к базе данных...")

        databaseManager = DatabaseManager(this)
        databaseManager.connect()

        logger.info("LH-API успешно инициализирован!")

        punishmentManager = PunishmentManager(this)

        val punishCmds = PunishCommands(this)
        getCommand("ban")?.setExecutor(punishCmds)
        getCommand("mute")?.setExecutor(punishCmds)
        getCommand("kick")?.setExecutor(punishCmds)
        getCommand("unban")?.setExecutor(punishCmds)
        getCommand("unmute")?.setExecutor(punishCmds)

        server.pluginManager.registerEvents(PunishmentListener(this), this)

        rankManager = RankManager(this)

        getCommand("setrank")?.setExecutor(SetRankCommand(this))

        server.pluginManager.registerEvents(PlayerListener(this), this)

        playtimeManager = PlaytimeManager(this)
        livesManager = LivesManager(this)
    }

    override fun onDisable() {
        databaseManager.disconnect()
        logger.info("LH-API выключен!")
    }
}