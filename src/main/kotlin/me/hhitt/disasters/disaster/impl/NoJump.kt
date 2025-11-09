package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NoJump : Disaster {
    private val players = mutableListOf<Player>()
    private var count = 0

    override fun start(arena: Arena) {
        arena.playing.forEach {
            players.add(it)
            it.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 5, 10, true, false))
        }

    }

    override fun pulse(time: Int) {
        if (count > 30) return
        if (time % 11 != 0) return

        players.forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 5, 10, true, false))
        }

        count++
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach {
            players.remove(it)
            it.removePotionEffect(PotionEffectType.JUMP_BOOST)
        }
    }
}
