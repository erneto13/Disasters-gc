package me.hhitt.disasters.arena

import org.bukkit.Location

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