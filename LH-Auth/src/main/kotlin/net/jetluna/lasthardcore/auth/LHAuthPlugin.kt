package net.jetluna.lasthardcore.auth

import org.bukkit.Location
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import net.jetluna.lasthardcore.auth.managers.AuthManager
import net.jetluna.lasthardcore.auth.commands.AuthCommands
import net.jetluna.lasthardcore.auth.commands.SetSpawnCommand
import net.jetluna.lasthardcore.auth.listeners.AuthListener

class LHAuthPlugin : JavaPlugin() {

    lateinit var authManager: AuthManager
        private set

    companion object {
        lateinit var instance: LHAuthPlugin
            private set
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        authManager = AuthManager()

        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")

        server.pluginManager.registerEvents(AuthListener(this), this)

        val authCommands = AuthCommands(this)
        getCommand("reg")?.setExecutor(authCommands)
        getCommand("login")?.setExecutor(authCommands)
        getCommand("register")?.setExecutor(authCommands)
        getCommand("l")?.setExecutor(authCommands)

        getCommand("setauthspawn")?.setExecutor(SetSpawnCommand(this))

        logger.info("LH-Auth успешно запущен!")
    }

    fun getSpawnLocation(): Location? {
        val worldName = config.getString("spawn.world") ?: return null
        val world = Bukkit.getWorld(worldName) ?: return null
        val x = config.getDouble("spawn.x")
        val y = config.getDouble("spawn.y")
        val z = config.getDouble("spawn.z")
        val yaw = config.getDouble("spawn.yaw").toFloat()
        val pitch = config.getDouble("spawn.pitch").toFloat()
        return Location(world, x, y, z, yaw, pitch)
    }
}