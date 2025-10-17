package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.attribute.Attribute

class OneHearth : Disaster {
    override fun start(arena: Arena) {
        Notify.disaster(arena, "one-hearth")
        arena.playing.forEach { player ->
            player.health = 2.0
            player.absorptionAmount = 0.0
        }
    }

    override fun pulse(time: Int) {

    }

    override fun stop(arena: Arena) {
        arena.playing.forEach { player ->
            val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            player.health = maxHealth
        }
    }
}