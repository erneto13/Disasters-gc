package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify

class Swap : Disaster {

    val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "swap")
    }

    override fun pulse(time: Int) {

        if (time % 10 != 0) return

        arenas.toList().forEach { arena ->
            val players = arena.alive.toList()
            if (players.size < 2) return@forEach

            val shuffled = players.shuffled().toMutableList()
            for (i in 0 until shuffled.size - 1 step 2) {
                val player1 = shuffled[i]
                val player2 = shuffled[i + 1]

                val loc1 = player1.location
                val loc2 = player2.location

                player1.teleport(loc2)
                player2.teleport(loc1)
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }
}
