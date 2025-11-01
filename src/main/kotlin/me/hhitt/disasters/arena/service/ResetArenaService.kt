package me.hhitt.disasters.arena.service

import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import kotlin.math.max
import kotlin.math.min
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameState
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class ResetArenaService(private val arena: Arena, private val worldEdit: WorldEditPlugin?) {

    private val world = arena.corner1.world
    private lateinit var clipboard: Clipboard
    private lateinit var center: BlockVector3

    private val minX = min(arena.corner1.x, arena.corner2.x).toInt()
    private val maxX = max(arena.corner1.x, arena.corner2.x).toInt()
    private val minY = min(arena.corner1.y, arena.corner2.y).toInt()
    private val maxY = max(arena.corner1.y, arena.corner2.y).toInt()
    private val minZ = min(arena.corner1.z, arena.corner2.z).toInt()
    private val maxZ = max(arena.corner1.z, arena.corner2.z).toInt()

    fun save() {
        val min: BlockVector3 = BlockVector3.at(arena.corner1.x, arena.corner1.y, arena.corner1.z)
        val max: BlockVector3 = BlockVector3.at(arena.corner2.x, arena.corner2.y, arena.corner2.z)
        val region = CuboidRegion(min, max)
        val clipboard = BlockArrayClipboard(region)
        worldEdit!!.worldEdit.newEditSession(BukkitAdapter.adapt(world)).use { editSession ->
            val forwardExtentCopy =
                    ForwardExtentCopy(editSession, region, clipboard, region.minimumPoint)
            try {
                Operations.complete(forwardExtentCopy)
            } catch (e: WorldEditException) {
                throw RuntimeException(e)
            }
            this.clipboard = clipboard
            this.center = region.minimumPoint
        }
    }

    fun paste() {
        performPrePasteCleanup()

        worldEdit!!.worldEdit.newEditSession(BukkitAdapter.adapt(world)).use { editSession ->
            val operation =
                    ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(center)
                            .ignoreAirBlocks(false)
                            .build()
            try {
                Operations.complete(operation)
            } catch (e: WorldEditException) {
                throw RuntimeException(e)
            }
        }

        performPostPasteCleanup()

        refreshChunks(world, arena.corner1, arena.corner2)
        arena.state = GameState.RECRUITING
    }

    private fun performPrePasteCleanup() {
        val plugin = Disasters.getInstance()

        plugin.logger.info("Starting pre-paste cleanup for arena ${arena.name}")

        arena.entityCleanupService.cleanupMeteors()
        arena.entityCleanupService.cleanupFireworks()
        arena.entityCleanupService.cleanupExtendedArea(50)
        removeEntitiesInRegion()
        arena.fluidCleanupService.cleanupFluids()
        arena.fluidCleanupService.cleanupExtendedArea(10)

        plugin.logger.info("Pre-paste cleanup completed for arena ${arena.name}")
    }

    private fun performPostPasteCleanup() {
        val plugin = Disasters.getInstance()
        plugin.server.scheduler.runTaskLater(
                plugin,
                Runnable { arena.entityCleanupService.cleanupArenaEntities() },
                5L
        )
    }

    private fun refreshChunks(world: World, loc1: Location, loc2: Location) {
        val minXX = minX shr 4
        val maxXX = maxX shr 4
        val minZZ = minZ shr 4
        val maxZZ = maxZ shr 4

        for (x in minXX..maxXX) {
            for (z in minZZ..maxZZ) {
                world.refreshChunk(x, z)
            }
        }
    }

    private fun removeEntitiesInRegion() {
        for (entity in world.entities) {
            val loc = entity.location
            if (loc.blockX in minX..maxX && loc.blockY in minY..maxY && loc.blockZ in minZ..maxZ) {
                if (entity !is Player) {
                    entity.remove()
                }
            }
        }
    }
}
