package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.entity.EntityType

class Wither : Disaster {
    override fun start(arena: Arena) {
        arena.location.world.spawnEntity(arena.location, EntityType.WITHER)
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
    }
}