package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.model.block.DisappearBlock
import me.hhitt.disasters.util.Notify
import java.util.concurrent.CopyOnWriteArrayList

class BlockDisappear : Disaster {

    private val blocks = CopyOnWriteArrayList<DisappearBlock>()

    override fun start(arena: Arena) {
    }

    override fun pulse(time: Int) {
        blocks.forEach { it.updateMaterial() }
    }

    override fun stop(arena: Arena) {
    }

    fun addBlock(block: DisappearBlock) {
        blocks.add(block)
    }

    fun removeBlock(block: DisappearBlock) {
        blocks.remove(block)
    }
}