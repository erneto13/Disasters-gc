package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify

class OneHearth: Disaster {
    override fun start(arena: Arena) {
        arena.playing.forEach {
            it.health = 1.0
            it.absorptionAmount = 0.0
        }
        Notify.disaster(arena, "one-hearth")
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
    }
}