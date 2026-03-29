package net.jetluna.lasthardcore.auth.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import net.jetluna.lasthardcore.auth.LHAuthPlugin
import java.util.UUID

class AuthListener(private val plugin: LHAuthPlugin) : Listener {

    private fun isFrozen(uuid: UUID): Boolean {
        return !plugin.authManager.isLoggedIn(uuid)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId

        event.joinMessage(null)

        val spawnLoc = plugin.getSpawnLocation()
        if (spawnLoc != null) {
            player.teleport(spawnLoc)
        } else {
            player.teleport(player.world.spawnLocation)
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val isReg = plugin.authManager.isRegistered(uuid)

            Bukkit.getScheduler().runTask(plugin, Runnable {
                val mainTitle = Component.text("§4§lLAST §c§lHARDCORE")

                val subTitle = if (isReg) {
                    Component.text("§eАвторизуйтесь: §b/login <пароль>")
                } else {
                    Component.text("§eЗарегистрируйтесь: §b/reg <пароль> <пароль>")
                }

                val times = net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(500),
                    java.time.Duration.ofMillis(60000),
                    java.time.Duration.ofMillis(1000)
                )

                player.showTitle(net.kyori.adventure.title.Title.title(mainTitle, subTitle, times))
            })
        })
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player

        if (player.location.y <= 10.0) {
            val spawnLoc = plugin.getSpawnLocation()
            if (spawnLoc != null) {
                player.teleport(spawnLoc)
            } else {
                player.teleport(player.world.spawnLocation)
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        plugin.authManager.logout(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncChatEvent) {
        if (isFrozen(event.player.uniqueId)) {
            event.isCancelled = true
            event.player.sendMessage(Component.text("Сначала авторизуйтесь!", NamedTextColor.RED))
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (isFrozen(event.player.uniqueId)) {
            val cmd = event.message.split(" ")[0].lowercase()
            if (cmd != "/login" && cmd != "/l" && cmd != "/reg" && cmd != "/register") {
                event.isCancelled = true
                event.player.sendMessage(Component.text("Вам доступны только команды авторизации!", NamedTextColor.RED))
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity is org.bukkit.entity.Player && isFrozen(entity.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (isFrozen(event.player.uniqueId)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (isFrozen(event.player.uniqueId)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDropItem(event: PlayerDropItemEvent) {
        if (isFrozen(event.player.uniqueId)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val whoClicked = event.whoClicked
        if (whoClicked is org.bukkit.entity.Player && isFrozen(whoClicked.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(event: PlayerInteractEvent) {
        if (isFrozen(event.player.uniqueId)) event.isCancelled = true
    }
}