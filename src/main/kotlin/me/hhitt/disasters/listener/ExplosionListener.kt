package me.hhitt.disasters.listener

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.Location
import org.bukkit.block.Block

class ExplosionListener(private val arenaManager: ArenaManager) : Listener {

    private val plugin = Disasters.getInstance()
    private val debrisManager = plugin.getDebrisManager()

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityExplode(event: EntityExplodeEvent) {
        handleExplosion(event.location, event.blockList(), event.yield)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockExplode(event: BlockExplodeEvent) {
        handleExplosion(event.block.location, event.blockList(), 1.0f)
    }

    private fun handleExplosion(
            location: Location,
            blockList: MutableList<Block>,
            yield: Float
    ) {
        for (arena in arenaManager.getArenas()) {
            if (arena.borderService.isLocationInArena(location)) {
                val blocksToDebris = blockList.toList()

                blockList.clear()

                debrisManager.createDebrisFromExplosion(location, blocksToDebris, 4.0f)

                return
            }
        }
    }
}
