package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.disaster.impl.FloorIsLava
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class FloorIsLavaListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.blockX == event.to.blockX &&
                        event.from.blockY == event.to.blockY &&
                        event.from.blockZ == event.to.blockZ
        ) {
            return
        }

        val player = event.player
        val arena = arenaManager.getArena(player) ?: return

        if (arena.isWaiting()) return

        if (!arena.disasters.any { it is FloorIsLava }) return

        if (!arena.borderService.isLocationInArenaTp(player)) return

        val blockUnderFeet = getBlockPlayerIsStandingOn(event.to)

        if (blockUnderFeet != null) {
            DisasterRegistry.addBlockToFloorIsLava(arena, blockUnderFeet)
        }
    }

    private fun getBlockPlayerIsStandingOn(playerLocation: Location): Location? {
        val world = playerLocation.world ?: return null

        val blockY = (playerLocation.y - 0.1).toInt()

        val blockUnderFeet = world.getBlockAt(playerLocation.blockX, blockY, playerLocation.blockZ)

        // Verificar que sea un bloque sólido válido
        if (blockUnderFeet.type.isSolid &&
                        blockUnderFeet.type != Material.AIR &&
                        blockUnderFeet.type != Material.WATER &&
                        blockUnderFeet.type != Material.LAVA
        ) {
            return blockUnderFeet.location
        }

        return null
    }
}
