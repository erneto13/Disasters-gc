package me.hhitt.disasters.arena

import me.hhitt.disasters.util.Msg
import org.bukkit.entity.Player

/**
 * Validates arena setup sessions and provides feedback to players.
 *
 * This class centralizes all validation logic for arena setup,
 * making it easier to maintain and test.
 */
object ArenaValidator {

    /**
     * Validates that a setup session is complete and ready to save.
     * Sends appropriate error messages to the player if validation fails.
     *
     * @param session The setup session to validate
     * @param player The player setting up the arena
     * @return true if the session is valid and complete, false otherwise
     */
    fun validateSession(session: ArenaSetupSession, player: Player): Boolean {
        if (session.isComplete()) {
            return true
        }

        // Send general error message
        Msg.send(player, "arena-setup.setup-missing-data")

        // Send specific missing component messages
        if (session.corner1 == null) {
            Msg.send(player, "arena-setup.setup-missing-corner1")
        }
        if (session.corner2 == null) {
            Msg.send(player, "arena-setup.setup-missing-corner2")
        }
        if (session.spawn == null) {
            Msg.send(player, "arena-setup.setup-missing-spawn")
        }

        return false
    }

    /**
     * Checks if an arena name is valid (not null, not empty, not too long).
     */
    fun isValidArenaName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        if (name.length > 32) return false
        return name.matches(Regex("[a-zA-Z0-9_-]+"))
    }

    /**
     * Validates that the arena bounds are reasonable.
     * Returns an error message if invalid, null if valid.
     */
    fun validateArenaBounds(session: ArenaSetupSession): String? {
        val corner1 = session.corner1 ?: return "Missing corner 1"
        val corner2 = session.corner2 ?: return "Missing corner 2"

        // Check if corners are in the same world
        if (corner1.world != corner2.world) {
            return "Corners must be in the same world"
        }

        // Check minimum size (at least 3x3x3)
        val dx = kotlin.math.abs(corner1.x - corner2.x)
        val dy = kotlin.math.abs(corner1.y - corner2.y)
        val dz = kotlin.math.abs(corner1.z - corner2.z)

        if (dx < 3 || dy < 3 || dz < 3) {
            return "Arena is too small (minimum 3x3x3 blocks)"
        }

        // Check maximum size (prevent server lag)
        if (dx > 500 || dy > 256 || dz > 500) {
            return "Arena is too large (maximum 500x256x500 blocks)"
        }

        return null
    }

    /**
     * Validates that the spawn point is within the arena bounds.
     */
    fun validateSpawnLocation(session: ArenaSetupSession): String? {
        val spawn = session.spawn ?: return "Spawn not set"
        val corner1 = session.corner1 ?: return "Corner 1 not set"
        val corner2 = session.corner2 ?: return "Corner 2 not set"

        val minX = minOf(corner1.x, corner2.x)
        val maxX = maxOf(corner1.x, corner2.x)
        val minY = minOf(corner1.y, corner2.y)
        val maxY = maxOf(corner1.y, corner2.y)
        val minZ = minOf(corner1.z, corner2.z)
        val maxZ = maxOf(corner1.z, corner2.z)

        if (spawn.x !in minX..maxX ||
            spawn.y !in minY..maxY ||
            spawn.z !in minZ..maxZ) {
            return "Spawn must be inside the arena bounds"
        }

        return null
    }
}