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

        player.sendMessage("§7[DEBUG] Floor is Lava activo")

        if (!arena.borderService.isLocationInArenaTp(player)) return

        val solidBlockBelow = findSolidBlockBelow(event.to)
        if (solidBlockBelow != null) {
            player.sendMessage("§7[DEBUG] Bloque encontrado: ${solidBlockBelow.block.type}")
            DisasterRegistry.addBlockToFloorIsLava(arena, solidBlockBelow)
        } else {
            player.sendMessage("§c[DEBUG] No se encontró bloque sólido")
        }
    }

    private fun findSolidBlockBelow(location: Location): Location? {
        val world = location.world ?: return null
        val startY = location.blockY

        for (y in startY downTo (startY - 5).coerceAtLeast(world.minHeight)) {
            val checkBlock = world.getBlockAt(location.blockX, y, location.blockZ)

            if (checkBlock.type.isSolid &&
                            checkBlock.type != Material.AIR &&
                            checkBlock.type != Material.WATER &&
                            checkBlock.type != Material.LAVA &&
                            !checkBlock.type.name.contains("LEAVES")
            ) {
                return checkBlock.location
            }
        }

        return null
    }
}
