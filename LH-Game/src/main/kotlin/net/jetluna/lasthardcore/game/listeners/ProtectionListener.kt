package net.jetluna.lasthardcore.game.listeners

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent

class ProtectionListener : Listener {

    @EventHandler
    fun onTntPlace(event: BlockPlaceEvent) {
        if (event.block.type == Material.TNT) {
            event.isCancelled = true
            event.player.sendMessage(Component.text("§cНа этом сервере запрещено использовать динамит!"))
        }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val entity = event.entity
        if (entity is TNTPrimed || entity is EnderCrystal) {
            event.blockList().clear()
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val target = event.entity
        if (target is Player) {
            if (damager is EnderCrystal || damager is TNTPrimed) {
                event.isCancelled = true
            }
        }
    }
}