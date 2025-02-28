package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.obj.block.DisasterFloor

class FloorIsLava: Disaster {

    val blocks = mutableListOf<DisasterFloor>()

    override fun start(arena: Arena) {
    }

    override fun pulse() {
        blocks.forEach { it.updateMaterial() }
    }

    override fun stop(arena: Arena) {
    }

    fun addBlock(block: DisasterFloor) {
        blocks.add(block)
    }

    fun removeBlock(block: DisasterFloor) {
        blocks.remove(block)
    }
}