package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

class ZeroGravity : Disaster {

    private val players = mutableListOf<Player>()
    private val count = 0

    override fun start(arena: Arena) {
        arena.playing.forEach() {
            players.add(it)
            it.addPotionEffect(
                    PotionEffect(
                            org.bukkit.potion.PotionEffectType.LEVITATION,
                            20 * 5,
                            1,
                            true,
                            false
                    )
            )
        }
    }

    override fun pulse(time: Int) {
        if (count > 30) return

        if (time % 11 != 0) return

        players.forEach {
            it.addPotionEffect(
                    PotionEffect(
                            org.bukkit.potion.PotionEffectType.LEVITATION,
                            20 * 5,
                            1,
                            true,
                            false
                    )
            )
        }

        count.inc()
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach {
            players.remove(it)
            it.removePotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)
        }
    }
}
