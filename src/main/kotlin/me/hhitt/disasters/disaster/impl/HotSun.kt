package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Msg
import org.bukkit.Particle
import org.bukkit.entity.Player

class HotSun : Disaster {

    private val arenaPlayers = mutableMapOf<Arena, MutableList<Player>>()

    override fun start(arena: Arena) {
        val players = mutableListOf<Player>()
        arena.alive.forEach { players.add(it) }
        arenaPlayers[arena] = players
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return

        arenaPlayers.forEach { (arena, players) ->
            players.filter { it.isValid && arena.alive.contains(it) }.forEach { player ->
                val loc = player.location

                val exposed =
                        loc.block.lightFromSky >= 15 &&
                                player.world.getHighestBlockAt(loc).y <= loc.y

                if (exposed) {
                    player.damage(0.5)

                    player.world.spawnParticle(
                            Particle.LAVA,
                            player.location.add(0.0, 1.0, 0.0),
                            10,
                            0.3,
                            0.5,
                            0.3,
                            0.01
                    )
                    Msg.playSound(player, "entity.player.hurt.on_fire")
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenaPlayers.remove(arena)
    }
}
