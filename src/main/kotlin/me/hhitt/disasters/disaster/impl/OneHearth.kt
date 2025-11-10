package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.attribute.Attribute

class OneHearth : Disaster {
    override fun start(arena: Arena) {
        arena.alive.forEach { player ->
            val maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH)
            maxHealthAttribute?.baseValue = 10.0

            player.health = 10.0
            player.absorptionAmount = 0.0
        }
    }

    override fun pulse(time: Int) {}

    override fun stop(arena: Arena) {
        arena.playing.forEach { player ->
            val maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH)
            maxHealthAttribute?.baseValue = 20.0

            if (player.health > 0) {
                player.health = 20.0
            }
        }
    }
}
