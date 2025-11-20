package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class ItemPickupListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onItemPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        arenaManager.getArena(player) ?: return
        event.isCancelled = true
    }
}
