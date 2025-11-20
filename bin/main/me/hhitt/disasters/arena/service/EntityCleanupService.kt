package me.hhitt.disasters.arena.service

import kotlin.math.max
import kotlin.math.min
import me.hhitt.disasters.arena.Arena
import org.bukkit.entity.*

class EntityCleanupService(private val arena: Arena) {

    private val minX = min(arena.corner1.x, arena.corner2.x).toInt()
    private val maxX = max(arena.corner1.x, arena.corner2.x).toInt()
    private val minY = min(arena.corner1.y, arena.corner2.y).toInt()
    private val maxY = max(arena.corner1.y, arena.corner2.y).toInt()
    private val minZ = min(arena.corner1.z, arena.corner2.z).toInt()
    private val maxZ = max(arena.corner1.z, arena.corner2.z).toInt()

    fun cleanupArenaEntities() {
        val world = arena.corner1.world

        world.entities.forEach { entity ->
            if (entity is Player) return@forEach

            val loc = entity.location
            if (isInArenaBounds(loc.blockX, loc.blockY, loc.blockZ)) {
                entity.remove()
            }
        }
    }

    fun cleanupExtendedArea(expansionRadius: Int = 50) {
        val world = arena.corner1.world

        val expandedMinX = minX - expansionRadius
        val expandedMaxX = maxX + expansionRadius
        val expandedMinZ = minZ - expansionRadius
        val expandedMaxZ = maxZ + expansionRadius

        world.entities.forEach { entity ->
            if (entity is Player) return@forEach

            val loc = entity.location

            if (loc.blockX in expandedMinX..expandedMaxX &&
                            loc.blockZ in expandedMinZ..expandedMaxZ &&
                            loc.blockY >= 0 && // From bedrock
                            loc.blockY <= world.maxHeight
            ) { // To build limit

                when (entity) {
                    is ArmorStand -> entity.remove()
                    is Firework -> entity.remove()
                    is Projectile -> entity.remove()
                    is Monster -> entity.remove()
                    is TNTPrimed -> entity.remove()
                    is FallingBlock -> entity.remove()
                    is Item -> entity.remove()
                    else -> {
                        if (isInArenaBounds(loc.blockX, loc.blockY, loc.blockZ)) {
                            entity.remove()
                        }
                    }
                }
            }
        }
    }

    fun cleanupMeteors() {
        val world = arena.corner1.world

        world.entities.filterIsInstance<ArmorStand>().forEach { stand ->
            if (!stand.isVisible && stand.equipment.helmet != null) {
                stand.remove()
            }
        }
    }

    private fun isInArenaBounds(x: Int, y: Int, z: Int): Boolean {
        return x in minX..maxX && y in minY..maxY && z in minZ..maxZ
    }

    fun cleanupFireworks() {
        val world = arena.corner1.world
        world.entities.filterIsInstance<Firework>().forEach { firework ->
            val loc = firework.location
            if (loc.blockX in (minX - 20)..(maxX + 20) && loc.blockZ in (minZ - 20)..(maxZ + 20)) {
                firework.remove()
            }
        }
    }
}
