package net.jetluna.lasthardcore.game.listeners

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ItemCooldownListener : Listener {

    private val totemCooldowns = ConcurrentHashMap<UUID, Long>()
    private val appleCooldowns = ConcurrentHashMap<UUID, Long>()

    @EventHandler
    fun onTotemPop(event: EntityResurrectEvent) {
        val player = event.entity as? Player ?: return
        val uuid = player.uniqueId
        val currentTime = System.currentTimeMillis()

        if (totemCooldowns.containsKey(uuid)) {
            val expireTime = totemCooldowns[uuid]!!
            if (currentTime < expireTime) {
                event.isCancelled = true
                val left = (expireTime - currentTime) / 1000
                player.sendActionBar(Component.text("§c✕ Тотем не сработал! Перезарядка: §e${left}с"))
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
                return
            }
        }

        totemCooldowns[uuid] = currentTime + 60000L
        player.sendActionBar(Component.text("§a✔ Тотем использован! §cПерезарядка: 60с"))
    }

    @EventHandler
    fun onAppleEat(event: PlayerItemConsumeEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val item = event.item.type

        if (item == Material.GOLDEN_APPLE || item == Material.ENCHANTED_GOLDEN_APPLE) {
            val currentTime = System.currentTimeMillis()

            if (appleCooldowns.containsKey(uuid)) {
                val expireTime = appleCooldowns[uuid]!!
                if (currentTime < expireTime) {
                    event.isCancelled = true
                    val left = (expireTime - currentTime) / 1000
                    player.sendActionBar(Component.text("§c✕ Слишком много яблок! Подождите: §e${left}с"))
                    player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                    return
                }
            }

            appleCooldowns[uuid] = currentTime + 30000L
            player.sendActionBar(Component.text("§e✔ Яблоко съедено! §cПерезарядка: 30с"))
        }
    }
}