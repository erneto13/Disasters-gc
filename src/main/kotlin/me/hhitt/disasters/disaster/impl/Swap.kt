package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Msg
import me.hhitt.disasters.util.Notify
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound

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

            if (players.size < 2) {
                players.forEach { player -> Msg.send(player, "swap.not-enough-players") }
                return@forEach
            }

            val shuffled = players.shuffled().toMutableList()

            val leftoverPlayer = if (shuffled.size % 2 != 0) shuffled.removeLast() else null

            for (i in 0 until shuffled.size - 1 step 2) {
                val player1 = shuffled[i]
                val player2 = shuffled[i + 1]

                val loc1 = player1.location.clone()
                val loc2 = player2.location.clone()

                spawnTeleportParticles(loc1)
                spawnTeleportParticles(loc2)

                player1.playSound(loc1, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
                player2.playSound(loc2, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)

                player1.teleport(loc2)
                player2.teleport(loc1)

                spawnTeleportParticles(loc2)
                spawnTeleportParticles(loc1)

                player1.playSound(player1.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f)
                player2.playSound(player2.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f)

                Msg.send(player1, "swap.swapped-with", "player" to player2.name)
                Msg.send(player2, "swap.swapped-with", "player" to player1.name)
            }

            leftoverPlayer?.let { player -> Msg.send(player, "swap.no-pair-found") }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }

    private fun spawnTeleportParticles(location: Location) {
        val world = location.world

        world.spawnParticle(
                Particle.PORTAL,
                location.clone().add(0.0, 1.0, 0.0),
                50,
                0.5,
                1.0,
                0.5,
                1.0
        )

        world.spawnParticle(
                Particle.REVERSE_PORTAL,
                location.clone().add(0.0, 1.0, 0.0),
                30,
                0.3,
                0.8,
                0.3,
                0.1
        )

        world.spawnParticle(
                Particle.DUST,
                location.clone().add(0.0, 1.0, 0.0),
                20,
                0.4,
                0.8,
                0.4,
                Particle.DustOptions(Color.fromRGB(170, 0, 170), 1.5f)
        )

        world.spawnParticle(
                Particle.SMOKE,
                location.clone().add(0.0, 0.5, 0.0),
                15,
                0.3,
                0.5,
                0.3,
                0.05
        )

        world.spawnParticle(Particle.FLASH, location.clone().add(0.0, 0.1, 0.0), 1)
    }
}
