package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Msg
import me.hhitt.disasters.util.Notify
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


class Murder : Disaster {

    private val players = mutableListOf<Player>()
    private val murders = mutableListOf<Player>()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
        addMurder()
        Notify.disaster(arena, "murder")
    }

    override fun pulse(time: Int) {
        if(time % 11 != 0) return
        addMurder()
    }

    override fun stop(arena: Arena) {
        TODO("Not yet implemented")
    }

    private fun addMurder(){
        var player: Player
        do{
            player = players.random()
        } while (murders.contains(player))
        murders.add(player)
        player.inventory.setItem(5, ItemStack.of(Material.DIAMOND_SWORD))
        Msg.send(player, "now-murder")
    }
}