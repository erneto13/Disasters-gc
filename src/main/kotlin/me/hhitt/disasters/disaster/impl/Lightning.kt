package me.hhitt.disasters.disaster.impl

import kotlin.random.Random
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Material

class Lightning : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val radius = 5
    private val random = Random

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "lightning")
    }

    override fun pulse(time: Int) {
        if (time % 3 != 0) return
        arenas.toList().forEach { arena ->
            if (arena.alive.isEmpty()) return@forEach

            val target = arena.alive.random()
            val location = target.location

            val offsetX = (random.nextDouble() - 0.5) * 2 * radius
            val offsetZ = (random.nextDouble() - 0.5) * 2 * radius

            val strikeLocation = location.clone().add(offsetX, 0.0, offsetZ)
            val highestBlockY =
                    strikeLocation.world?.getHighestBlockYAt(strikeLocation)?.toDouble()
                            ?: strikeLocation.y
            strikeLocation.y = highestBlockY
            strikeLocation.world?.strikeLightning(strikeLocation)
            strikeLocation.block.type = Material.AIR
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }
}
