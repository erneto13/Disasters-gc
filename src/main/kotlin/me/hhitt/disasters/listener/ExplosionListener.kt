package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplosionListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        for (arena in arenaManager.getArenas()) {
            if (arena.borderService.isLocationInArena(event.location)) {
                event.yield = 0f

                event.blockList().clear()

                return
            }
        }
    }
}
