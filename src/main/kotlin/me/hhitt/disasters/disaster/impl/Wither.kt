package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.entity.EntityType
import org.bukkit.entity.Wither
import org.bukkit.entity.WitherSkull

class Wither : Disaster {
    private val activeWithers = mutableMapOf<Arena, MutableList<Wither>>()

    override fun start(arena: Arena) {
        val witherList = mutableListOf<Wither>()

        repeat(1) {
            val wither =
                    arena.location.world.spawnEntity(arena.location, EntityType.WITHER) as Wither

            wither.isCustomNameVisible = false

            wither.setAI(true)

            witherList.add(wither)
        }

        activeWithers[arena] = witherList
    }

    override fun pulse(time: Int) {
        activeWithers.forEach { (arena, withers) ->
            withers.removeIf { !it.isValid || it.isDead }

            withers.forEach { wither ->
                if (wither.target == null && arena.alive.isNotEmpty()) {
                    wither.target = arena.alive.random()
                } else if (wither.target == null || arena.alive.isEmpty()) {
                    shootAtRandomBlock(wither, arena)
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        activeWithers.remove(arena)?.forEach { it.remove() }
    }

    private fun shootAtRandomBlock(wither: Wither, arena: Arena) {
        val minX = kotlin.math.min(arena.corner1.blockX, arena.corner2.blockX)
        val maxX = kotlin.math.max(arena.corner1.blockX, arena.corner2.blockX)
        val minY = kotlin.math.min(arena.corner1.blockY, arena.corner2.blockY)
        val maxY = kotlin.math.max(arena.corner1.blockY, arena.corner2.blockY)
        val minZ = kotlin.math.min(arena.corner1.blockZ, arena.corner2.blockZ)
        val maxZ = kotlin.math.max(arena.corner1.blockZ, arena.corner2.blockZ)

        val randomX = kotlin.random.Random.nextInt(minX, maxX + 1)
        val randomY = kotlin.random.Random.nextInt(minY, maxY + 1)
        val randomZ = kotlin.random.Random.nextInt(minZ, maxZ + 1)

        val targetLocation =
                org.bukkit.Location(
                        arena.corner1.world,
                        randomX.toDouble(),
                        randomY.toDouble(),
                        randomZ.toDouble()
                )

        val direction = targetLocation.toVector().subtract(wither.location.toVector()).normalize()
        wither.world.spawn(wither.eyeLocation, WitherSkull::class.java) { skull ->
            skull.isCharged = true
            skull.velocity = direction.multiply(1.5)
            skull.shooter = wither
        }
    }
}
