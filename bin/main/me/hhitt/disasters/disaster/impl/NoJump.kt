package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NoJump : Disaster {
    private val arenaPlayers = mutableMapOf<Arena, MutableList<Player>>()
    private var count = 0

    override fun start(arena: Arena) {
        val players = mutableListOf<Player>()
        arena.alive.forEach {
            players.add(it)
            it.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 5, 10, true, false))
        }
        arenaPlayers[arena] = players
    }

    override fun pulse(time: Int) {
        if (count > 30) return
        if (time % 11 != 0) return

        arenaPlayers.forEach { (arena, players) ->
            players.filter { arena.alive.contains(it) }.forEach {
                it.addPotionEffect(
                        PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 5, 10, true, false)
                )
            }
        }

        count++
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach { it.removePotionEffect(PotionEffectType.JUMP_BOOST) }
        arenaPlayers.remove(arena)
    }
}
