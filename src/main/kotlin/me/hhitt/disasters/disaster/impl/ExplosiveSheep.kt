package me.hhitt.disasters.disaster.impl

import kotlin.random.Random
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.model.entity.DisasterSheep
import me.hhitt.disasters.util.Notify
import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Player

class ExplosiveSheep : Disaster {

    val arenas = mutableListOf<Arena>()
    val sheeps = mutableListOf<DisasterSheep>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "explosive-sheep")
    }

    override fun pulse(time: Int) {
        tick()

        if (time % 5 != 0) return
        arenas.toList().forEach { arena ->
            arena.alive.toList().forEach { player -> spawnSheep(player, 10, 1) }
        }
    }

    override fun stop(arena: Arena) {
        sheeps.toList().forEach { it.remove(Entity.RemovalReason.KILLED) }
        sheeps.clear()
        arenas.remove(arena)
    }

    private fun tick() {
        val iterator = sheeps.iterator()
        while (iterator.hasNext()) {
            val sheep = iterator.next()
            if (sheep.isAlive) {
                sheep.call()
            } else {
                iterator.remove()
            }
        }
    }

    private fun spawnSheep(player: Player, radius: Int, amount: Int) {
        repeat(amount) {
            val spawnLocation = findSafeSpawnLocation(player.location, radius)
            spawnLocation?.let {
                val sheep =
                        DisasterSheep(
                                net.minecraft.world.entity.EntityType.SHEEP,
                                (spawnLocation.world as CraftWorld).handle.level,
                                spawnLocation
                        )
                (spawnLocation.world as CraftWorld).handle.addFreshEntity(sheep)
                sheeps.add(sheep)
            }
        }
    }

    private fun findSafeSpawnLocation(location: Location, radius: Int): Location? {
        repeat(10) {
            val randomX = location.x + Random.nextDouble(-radius.toDouble(), radius.toDouble())
            val randomZ = location.z + Random.nextDouble(-radius.toDouble(), radius.toDouble())
            val highestY =
                    location.world.getHighestBlockYAt(randomX.toInt(), randomZ.toInt()).toDouble()
            val potentialLocation = Location(location.world, randomX, highestY + 1, randomZ)

            if (isSafeLocation(potentialLocation)) {
                return potentialLocation
            }
        }
        return null
    }

    private fun isSafeLocation(location: Location): Boolean {
        val world = location.world
        val block = world.getBlockAt(location)
        val blockAbove = world.getBlockAt(location.add(0.0, 1.0, 0.0))

        return block.type != Material.LAVA &&
                block.type != Material.CACTUS &&
                blockAbove.type == Material.AIR
    }
}
