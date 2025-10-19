package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.model.block.DisasterFloor
import me.hhitt.disasters.util.Notify
import java.util.concurrent.CopyOnWriteArrayList

class FloorIsLava: Disaster {

    private val blocks = CopyOnWriteArrayList<DisasterFloor>()
    private val trackedLocations = mutableSetOf<String>()
    private var tickCounter = 0
    private val ticksPerStage = 40

    override fun start(arena: Arena) {
        Notify.disaster(arena, "floor-is-lava")
        tickCounter = 0
    }

    override fun pulse(time: Int) {
        tickCounter++

        if (tickCounter % ticksPerStage == 0) {
            blocks.forEach { it.updateMaterial() }
        }
    }

    override fun stop(arena: Arena) {
        blocks.clear()
        trackedLocations.clear()
    }

    fun addBlock(block: DisasterFloor) {
        val locationKey = "${block.location.blockX},${block.location.blockY},${block.location.blockZ}"

        if (trackedLocations.add(locationKey)) {
            blocks.add(block)
        }
    }

    fun removeBlock(block: DisasterFloor) {
        val locationKey = "${block.location.blockX},${block.location.blockY},${block.location.blockZ}"
        trackedLocations.remove(locationKey)
        blocks.remove(block)
    }
}