package me.hhitt.disasters.arena.service

import kotlin.math.max
import kotlin.math.min
import me.hhitt.disasters.arena.Arena
import org.bukkit.Material
import org.bukkit.block.Block

class FluidCleanupService(private val arena: Arena) {

    private val minX = min(arena.corner1.x, arena.corner2.x).toInt()
    private val maxX = max(arena.corner1.x, arena.corner2.x).toInt()
    private val minY = min(arena.corner1.y, arena.corner2.y).toInt()
    private val maxY = max(arena.corner1.y, arena.corner2.y).toInt()
    private val minZ = min(arena.corner1.z, arena.corner2.z).toInt()
    private val maxZ = max(arena.corner1.z, arena.corner2.z).toInt()

    fun cleanupFluids() {
        val world = arena.corner1.world

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                for (y in 0 until world.maxHeight) {
                    val block = world.getBlockAt(x, y, z)
                    if (isFluid(block)) {
                        block.type = Material.AIR
                    }
                }
            }
        }
    }

    fun cleanupExtendedArea(expansionRadius: Int = 10) {
        val world = arena.corner1.world

        val expandedMinX = minX - expansionRadius
        val expandedMaxX = maxX + expansionRadius
        val expandedMinZ = minZ - expansionRadius
        val expandedMaxZ = maxZ + expansionRadius

        for (x in expandedMinX..expandedMaxX) {
            for (z in expandedMinZ..expandedMaxZ) {
                for (y in 0 until world.maxHeight) {
                    val block = world.getBlockAt(x, y, z)
                    if (isFluid(block)) {
                        block.type = Material.AIR
                    }
                }
            }
        }
    }

    fun cleanupFallenFluids() {
        val world = arena.corner1.world

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                for (y in 0 until minY) {
                    val block = world.getBlockAt(x, y, z)
                    if (isFluid(block)) {
                        block.type = Material.AIR
                    }
                }
            }
        }
    }

    private fun isFluid(block: Block): Boolean {
        val material = block.type
        return material == Material.WATER ||
                material == Material.LAVA ||
                material.name.contains("WATER") ||
                material.name.contains("LAVA")
    }

    fun countFluids(): Int {
        val world = arena.corner1.world
        var count = 0

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                for (y in 0 until world.maxHeight) {
                    val block = world.getBlockAt(x, y, z)
                    if (isFluid(block)) {
                        count++
                    }
                }
            }
        }

        return count
    }
}
