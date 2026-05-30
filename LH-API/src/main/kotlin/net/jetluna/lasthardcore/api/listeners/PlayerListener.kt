package net.jetluna.lasthardcore.api.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import net.jetluna.lasthardcore.api.LastHardcoreAPI

class PlayerListener(private val api: LastHardcoreAPI) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        api.rankManager.loadPlayer(event.uniqueId, event.name)
        api.playtimeManager.loadPlayer(event.uniqueId)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        api.rankManager.unloadPlayer(event.player.uniqueId)
        api.playtimeManager.saveAndUnloadPlayer(event.player.uniqueId)
    }
}