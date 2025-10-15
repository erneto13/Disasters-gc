package me.hhitt.disasters.visual

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.plugin.Plugin

/**
 * Manages particle visualization for cuboid regions.
 *
 * This class handles:
 * - Drawing particle cuboids between two corners
 * - Managing persistent particle tasks per player
 * - Cleaning up visualizations when players quit or save
 */
class CuboidVisualizer(private val plugin: Plugin) {

    private val activeVisualizations = mutableMapOf<String, BukkitTask>()

    /**
     * Starts showing a persistent cuboid visualization for a player.
     * The visualization will continue until manually stopped.
     *
     * @param playerName The player's name
     * @param corner1 First corner of the cuboid
     * @param corner2 Second corner of the cuboid
     */
    fun startVisualization(playerName: String, corner1: Location, corner2: Location) {
        // Cancel any existing visualization
        stopVisualization(playerName)

        // Create new persistent task
        val task = object : BukkitRunnable() {
            override fun run() {
                // Check if locations are still valid
                if (corner1.world == null || corner2.world == null) {
                    cancel()
                    activeVisualizations.remove(playerName)
                    return
                }
                drawCuboid(corner1, corner2)
            }
        }

        // Run every 10 ticks (0.5 seconds) indefinitely
        val scheduledTask = task.runTaskTimer(plugin, 0L, 10L)
        activeVisualizations[playerName] = scheduledTask
    }

    /**
     * Stops the visualization for a specific player.
     *
     * @param playerName The player's name
     */
    fun stopVisualization(playerName: String) {
        activeVisualizations[playerName]?.cancel()
        activeVisualizations.remove(playerName)
    }

    /**
     * Checks if a player has an active visualization.
     */
    fun hasActiveVisualization(playerName: String): Boolean {
        return activeVisualizations.containsKey(playerName)
    }

    /**
     * Stops all active visualizations. Useful for plugin shutdown.
     */
    fun stopAllVisualizations() {
        activeVisualizations.values.forEach { it.cancel() }
        activeVisualizations.clear()
    }

    /**
     * Draws a cuboid using particles between two locations.
     */
    private fun drawCuboid(loc1: Location, loc2: Location) {
        val world = loc1.world ?: return

        // Get min and max coordinates
        val minX = minOf(loc1.x, loc2.x)
        val minY = minOf(loc1.y, loc2.y)
        val minZ = minOf(loc1.z, loc2.z)
        val maxX = maxOf(loc1.x, loc2.x) + 1
        val maxY = maxOf(loc1.y, loc2.y) + 1
        val maxZ = maxOf(loc1.z, loc2.z) + 1

        val particleSpacing = 0.5

        // Draw the 12 edges of the cube
        // Bottom edges (Y minimum)
        drawLine(world, minX, minY, minZ, maxX, minY, minZ, particleSpacing)
        drawLine(world, minX, minY, maxZ, maxX, minY, maxZ, particleSpacing)
        drawLine(world, minX, minY, minZ, minX, minY, maxZ, particleSpacing)
        drawLine(world, maxX, minY, minZ, maxX, minY, maxZ, particleSpacing)

        // Top edges (Y maximum)
        drawLine(world, minX, maxY, minZ, maxX, maxY, minZ, particleSpacing)
        drawLine(world, minX, maxY, maxZ, maxX, maxY, maxZ, particleSpacing)
        drawLine(world, minX, maxY, minZ, minX, maxY, maxZ, particleSpacing)
        drawLine(world, maxX, maxY, minZ, maxX, maxY, maxZ, particleSpacing)

        // Vertical edges
        drawLine(world, minX, minY, minZ, minX, maxY, minZ, particleSpacing)
        drawLine(world, maxX, minY, minZ, maxX, maxY, minZ, particleSpacing)
        drawLine(world, minX, minY, maxZ, minX, maxY, maxZ, particleSpacing)
        drawLine(world, maxX, minY, maxZ, maxX, maxY, maxZ, particleSpacing)

        // Draw the 8 vertices with larger particles
        val vertices = listOf(
            Location(world, minX, minY, minZ),
            Location(world, maxX, minY, minZ),
            Location(world, minX, minY, maxZ),
            Location(world, maxX, minY, maxZ),
            Location(world, minX, maxY, minZ),
            Location(world, maxX, maxY, minZ),
            Location(world, minX, maxY, maxZ),
            Location(world, maxX, maxY, maxZ)
        )

        vertices.forEach { vertex ->
            world.spawnParticle(
                Particle.DUST,
                vertex,
                5,
                0.0, 0.0, 0.0,
                Particle.DustOptions(Color.YELLOW, 2.0f)
            )
        }
    }

    /**
     * Draws a line of particles between two points.
     */
    private fun drawLine(
        world: World,
        x1: Double, y1: Double, z1: Double,
        x2: Double, y2: Double, z2: Double,
        spacing: Double
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1
        val distance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        val points = (distance / spacing).toInt().coerceAtLeast(1)

        for (i in 0..points) {
            val t = i.toDouble() / points
            val x = x1 + dx * t
            val y = y1 + dy * t
            val z = z1 + dz * t

            world.spawnParticle(
                Particle.DUST,
                Location(world, x, y, z),
                1,
                0.0, 0.0, 0.0,
                Particle.DustOptions(Color.AQUA, 1.0f)
            )
        }
    }
}