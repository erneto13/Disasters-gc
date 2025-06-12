package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.entity.Player

class HotSun : Disaster {

    private val players = mutableListOf<Player>()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return

        players.forEach { player ->
            val location = player.location.add(0.0, 1.0, 0.0)
            if (location.block.type.isSolid) {
                player.damage(0.5)
            }
        }
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }
}