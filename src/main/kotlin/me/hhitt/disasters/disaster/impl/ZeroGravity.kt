package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

class ZeroGravity : Disaster {

    private val players = mutableListOf<Player>()

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


        Notify.disaster(arena, "zero-gravity")

    }

    override fun pulse(time: Int) {

        if(time % 5 != 0) return

        players.forEach() {
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

    override fun stop(arena: Arena) {
        arena.playing.forEach {
            players.remove(it)
            it.removePotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)
        }
    }
}