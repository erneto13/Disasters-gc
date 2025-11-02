package me.hhitt.disasters.arena.service

import me.hhitt.disasters.arena.Arena
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

/**
 * RespawnService is responsible for handling player respawns in the arena.
 *
 * @param arena The arena where the players are located.
 */
class RespawnService(private val arena: Arena) {

    fun setSpectator(player: Player) {
        val craftPlayer = player as CraftPlayer
        craftPlayer.handle.connection.send(
                ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, 1.0f)
        )
        player.gameMode = GameMode.SPECTATOR

        val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        player.health = maxHealth

        if (arena.playing.isNotEmpty()) {
            player.teleport(arena.playing[0].location.clone().add(0.0, 3.0, 0.0))
        } else {
            player.teleport(arena.location)
        }
    }

    fun respawnAtArena(player: Player) {
        val craftPlayer = player as CraftPlayer
        craftPlayer.handle.connection.send(
                ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, 1.0f)
        )
        player.teleport(arena.location)
        player.gameMode = GameMode.SURVIVAL

        val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        player.health = maxHealth
    }
}
