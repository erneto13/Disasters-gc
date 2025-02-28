package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMoveListener(private val arenaManager: ArenaManager): Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.block != event.to.block) {

            // If player is in the same block, we don't care and return
            if (event.from.blockX == event.to.blockX &&
                event.from.blockY == event.to.blockY &&
                event.from.blockZ == event.to.blockZ) {
                return
            }

            // Obtaining the arena if player is in one
            arenaManager.getArena(event.player)?.let { arena ->

                if (arena.isWaiting()) {
                    // We don't care if the game does not start
                    return
                }

                // Check if player is in the border and add the block to the floor is lava if needed
                if(arena.border.isLocationInArenaTp(event.player)) {
                    DisasterRegistry.addBlockToFloorIsLava(arena, event.player.location)
                }

            }

        }

    }


}