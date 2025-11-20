package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

class FoodLevelChangeListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        val player = event.entity as? org.bukkit.entity.Player ?: return

        arenaManager.getArena(player)?.let {
            event.isCancelled = true
        }
    }
}
