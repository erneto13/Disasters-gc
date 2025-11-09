package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player

class Blind : Disaster {

    override fun start(arena: Arena) {
        arena.playing.forEach {
            player: Player -> player.addPotionEffect(
                org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS,
                    20 * 20,
                    0,
                    true, // Ambient
                    false // Show particles
                )
            )
        }
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach {
            player: Player -> player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS)
        }
    }


}