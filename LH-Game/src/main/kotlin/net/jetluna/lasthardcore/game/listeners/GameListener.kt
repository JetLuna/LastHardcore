package net.jetluna.lasthardcore.game.listeners

import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import net.jetluna.lasthardcore.api.LastHardcoreAPI

class GameListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val livesManager = LastHardcoreAPI.instance.livesManager
        val lives = livesManager.getLives(player.uniqueId)

        val mainTitle = Component.text("§4§lLAST §c§lHARDCORE")
        val subTitle = Component.text("§eПриятной игры!")
        val times = net.kyori.adventure.title.Title.Times.times(
            java.time.Duration.ofMillis(500),
            java.time.Duration.ofMillis(3000),
            java.time.Duration.ofMillis(1000)
        )
        player.showTitle(net.kyori.adventure.title.Title.title(mainTitle, subTitle, times))

        if (lives <= 0) {
            player.gameMode = GameMode.SPECTATOR
            player.sendMessage(Component.text("§cВы окончательно мертвы и находитесь в режиме наблюдателя."))
        } else {
            livesManager.updateMaxHealth(player)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val livesManager = LastHardcoreAPI.instance.livesManager

        livesManager.removeLife(player.uniqueId)
        val newLives = livesManager.getLives(player.uniqueId)

        if (newLives <= 0) {
            player.sendMessage(Component.text("§4§lВЫ ПОТЕРЯЛИ ПОСЛЕДНЮЮ ЖИЗНЬ!"))
        } else {
            player.sendMessage(Component.text("§cВы умерли! Осталось жизней: §e$newLives"))
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val livesManager = LastHardcoreAPI.instance.livesManager
        val newLives = livesManager.getLives(player.uniqueId)

        if (newLives <= 0) {
            player.gameMode = GameMode.SPECTATOR
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 1.0
        } else {
            livesManager.updateMaxHealth(player)
        }
    }
}