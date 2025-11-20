package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaSetupManager
import me.hhitt.disasters.arena.ArenaSetupSession
import me.hhitt.disasters.util.Msg
import me.hhitt.disasters.util.SelectionTool
import me.hhitt.disasters.visual.CuboidVisualizer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Handles player interactions for selecting arena corners during setup.
 *
 * Responsibilities:
 * - Listen for block interactions with the selection tool
 * - Update setup session with selected corners
 * - Trigger visualization when both corners are set
 * - Clean up on player quit
 */
class SelectionListener(
    private val setupManager: ArenaSetupManager,
    private val visualizer: CuboidVisualizer
) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        // Check if player is using the selection tool
        if (item.type != Material.GOLDEN_AXE) return
        if (!SelectionTool.isSelectionAxe(item)) return

        // Check if player has an active setup session
        val session = setupManager.getSession(player) ?: return

        val block = event.clickedBlock ?: return
        event.isCancelled = true

        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> handleLeftClick(session, block, player)
            Action.RIGHT_CLICK_BLOCK -> handleRightClick(session, block, player)
            else -> return
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player

        // Clean up visualization and session
        visualizer.stopVisualization(player.name)
        setupManager.removeSession(player)
    }

    /**
     * Handles left click to set the first corner.
     */
    private fun handleLeftClick(
        session: ArenaSetupSession,
        block: Block,
        player: Player
    ) {
        session.corner1 = block.location

        Msg.send(
            player,
            "arena-setup.first-corner-selected",
            "x" to block.x.toString(),
            "y" to block.y.toString(),
            "z" to block.z.toString()
        )

        // Check if both corners are now set
        checkBothCornersSet(session, player)
    }

    /**
     * Handles right click to set the second corner.
     */
    private fun handleRightClick(
        session: ArenaSetupSession,
        block: Block,
        player: Player
    ) {
        session.corner2 = block.location

        Msg.send(
            player,
            "arena-setup.second-corner-selected",
            "x" to block.x.toString(),
            "y" to block.y.toString(),
            "z" to block.z.toString()
        )

        // Check if both corners are now set
        checkBothCornersSet(session, player)
    }

    /**
     * Checks if both corners are set and starts visualization.
     */
    private fun checkBothCornersSet(
        session: ArenaSetupSession,
        player: Player
    ) {
        val corner1 = session.corner1 ?: return
        val corner2 = session.corner2 ?: return

        // Calculate and display distance
        val distance = corner1.distance(corner2)
        Msg.send(
            player,
            "arena-setup.distance-between-corners",
            "distance" to distance.toInt().toString()
        )

        // Calculate volume
        val dx = kotlin.math.abs(corner1.x - corner2.x) + 1
        val dy = kotlin.math.abs(corner1.y - corner2.y) + 1
        val dz = kotlin.math.abs(corner1.z - corner2.z) + 1
        val volume = (dx * dy * dz).toInt()

        Msg.send(
            player,
            "arena-setup.arena-volume",
            "volume" to volume.toString(),
            "dx" to dx.toInt().toString(),
            "dy" to dy.toInt().toString(),
            "dz" to dz.toInt().toString()
        )

        // Start persistent visualization
        visualizer.startVisualization(player.name, corner1, corner2)

        Msg.send(player, "arena-setup.visualization-started")
    }
}