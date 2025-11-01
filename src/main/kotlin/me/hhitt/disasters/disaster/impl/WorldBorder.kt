package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.world.level.border.WorldBorder
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

class WorldBorder : Disaster {

    private val arenaSizes = mutableMapOf<Arena, Double>()
    private val shrinkAmountPerPulse = 0.5

    override fun start(arena: Arena) {
        Notify.disaster(arena, "world-border")
        val corner1 = arena.corner1
        val corner2 = arena.corner2
        val center =
                Location(
                        corner1.world,
                        (corner1.x + corner2.x) / 2,
                        (corner1.y + corner2.y) / 2,
                        (corner1.z + corner2.z) / 2
                )
        val initialRadius = corner1.distance(corner2) / 2
        arenaSizes[arena] = initialRadius

        for (player in arena.playing) {
            sendWorldBorder(player, center, initialRadius)
        }
    }

    override fun pulse(time: Int) {
        val iterator = arenaSizes.iterator()
        while (iterator.hasNext()) {
            val (arena, currentRadius) = iterator.next()
            val newRadius = currentRadius - shrinkAmountPerPulse
            if (newRadius <= 0) {
                stop(arena)
                iterator.remove()
            } else {
                arenaSizes[arena] = newRadius
                val center =
                        Location(
                                arena.corner1.world,
                                (arena.corner1.x + arena.corner2.x) / 2,
                                (arena.corner1.y + arena.corner2.y) / 2,
                                (arena.corner1.z + arena.corner2.z) / 2
                        )
                arena.playing.toList().forEach { player ->
                    sendWorldBorder(player, center, newRadius)
                }
                checkPlayersOutsideBorderAndApplyDamage(arena, center, newRadius)
            }
        }
    }

    override fun stop(arena: Arena) {
        arenaSizes.remove(arena)
        arena.playing.toList().forEach { player -> resetWorldBorder(player) }
    }

    private fun sendWorldBorder(player: Player, center: Location, size: Double) {
        val craftWorld = player.world as CraftWorld
        val worldServer = craftWorld.handle
        val worldBorder = WorldBorder()
        worldBorder.world = worldServer
        worldBorder.setCenter(center.x, center.z)
        worldBorder.size = size * 2

        val packet = ClientboundInitializeBorderPacket(worldBorder)
        sendPacket(player, packet)
    }

    private fun resetWorldBorder(player: Player) {
        val craftWorld = player.world as CraftWorld
        val worldServer = craftWorld.handle
        val worldBorder = WorldBorder()
        worldBorder.world = worldServer
        worldBorder.setCenter(player.world.spawnLocation.x, player.world.spawnLocation.z)
        worldBorder.size = player.world.worldBorder.maxSize

        val packet = ClientboundInitializeBorderPacket(worldBorder)
        sendPacket(player, packet)
    }

    private fun sendPacket(player: Player, packet: Packet<ClientGamePacketListener>) {
        (player as CraftPlayer).handle.connection.send(packet)
    }

    private fun checkPlayersOutsideBorderAndApplyDamage(
            arena: Arena,
            center: Location,
            radius: Double
    ) {
        val radiusSquared = radius * radius
        arena.alive.toList().forEach { player ->
            val loc = player.location
            if (loc.world == center.world) {
                val distanceSquared = loc.distanceSquared(center)
                if (distanceSquared > radiusSquared) {
                    player.damage(2.0)
                }
            }
        }
    }
}
