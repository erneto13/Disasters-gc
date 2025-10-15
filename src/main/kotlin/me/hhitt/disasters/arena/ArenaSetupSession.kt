package me.hhitt.disasters.arena

import org.bukkit.Location

/** ArenaSetupSession class represents a setup session for configuring a game arena.
 *
 * @param arenaName The name of the arena being set up.
 * @param corner1 One corner of the arena's bounding box (optional).
 * @param corner2 The opposite corner of the arena's bounding box (optional).
 * @param spawn The spawn location for players in the arena (optional).
 *
 **/
data class ArenaSetupSession(
    val arenaName: String,
    var corner1: Location? = null,
    var corner2: Location? = null,
    var spawn: Location? = null
) {
    fun isComplete(): Boolean {
        return corner1 != null && corner2 != null && spawn != null
    }
}