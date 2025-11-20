package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent

class BlockDropItemListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockDropItem(event: BlockDropItemEvent) {
        val location = event.block.location

        val arena = arenaManager.getArena(location)
        if (arena != null) {
            event.isCancelled = true
        }
    }
}
