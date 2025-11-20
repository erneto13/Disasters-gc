package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.entity.Player

class Cobweb : Disaster {

    private val arenaPlayers = mutableMapOf<Arena, MutableList<Player>>()

    override fun start(arena: Arena) {
        val players = mutableListOf<Player>()
        arena.alive.forEach { players.add(it) }
        arenaPlayers[arena] = players
    }

    override fun pulse(time: Int) {
        if (time % 5 != 0) return

        arenaPlayers.forEach { (arena, players) ->
            players.filter { arena.alive.contains(it) }.forEach { player ->
                if (player.location.block.type != org.bukkit.Material.COBWEB) {
                    setInCobweb(player)
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenaPlayers.remove(arena)
    }

    fun setInCobweb(player: Player) {
        player.location.block.type = org.bukkit.Material.COBWEB
    }
}
