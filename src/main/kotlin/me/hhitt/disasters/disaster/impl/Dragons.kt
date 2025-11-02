package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.EntityType

class Dragons : Disaster {

    private val dragons = mutableListOf<EnderDragon>()
    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)

        val centerX = (arena.corner1.x + arena.corner2.x) / 2
        val centerY = (arena.corner1.y + arena.corner2.y) / 2 + 20
        val centerZ = (arena.corner1.z + arena.corner2.z) / 2

        val world = arena.corner1.world

        val dragon1Location =
                world.getBlockAt(centerX.toInt() + 15, centerY.toInt(), centerZ.toInt()).location
        val dragon2Location =
                world.getBlockAt(centerX.toInt() - 15, centerY.toInt(), centerZ.toInt()).location

        val dragon1 = world.spawnEntity(dragon1Location, EntityType.ENDER_DRAGON) as EnderDragon
        val dragon2 = world.spawnEntity(dragon2Location, EntityType.ENDER_DRAGON) as EnderDragon

        dragon1.isCustomNameVisible = false
        dragon2.isCustomNameVisible = false

        dragon1.setAI(true)
        dragon2.setAI(true)

        dragons.add(dragon1)
        dragons.add(dragon2)

        Notify.disaster(arena, "dragons")
    }

    override fun pulse(time: Int) {
        val iterator = dragons.iterator()
        while (iterator.hasNext()) {
            val dragon = iterator.next()
            if (!dragon.isValid || dragon.isDead) {
                iterator.remove()
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)

        dragons.forEach { dragon ->
            if (dragon.isValid) {
                dragon.remove()
            }
        }
        dragons.clear()
    }
}