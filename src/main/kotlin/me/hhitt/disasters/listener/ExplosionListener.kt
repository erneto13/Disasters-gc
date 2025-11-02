package me.hhitt.disasters.listener

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplosionListener(private val arenaManager: ArenaManager) : Listener {

    private val plugin = Disasters.getInstance()
    private val debrisManager = plugin.getDebrisManager()

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        for (arena in arenaManager.getArenas()) {
            if (arena.borderService.isLocationInArena(event.location)) {
                event.yield = 0f

                val blocksToDebris = event.blockList().toList()

                event.blockList().clear()

                debrisManager.createDebrisFromExplosion(event.location, blocksToDebris, 4.0f)

                return
            }
        }
    }
}
