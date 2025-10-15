package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.impl.*
import me.hhitt.disasters.model.block.DisappearBlock
import me.hhitt.disasters.model.block.DisasterFloor
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * DisasterRegistry is a singleton object that manages the active disasters in the game.
 * It allows adding, removing, and pulsing disasters for each arena.
 */

object DisasterRegistry {

    private val activeDisasters = mutableMapOf<Arena, MutableList<Disaster>>()
    private val disasterClasses = listOf(
        AcidRain::class,
        Apocalypse::class,
        Blind::class,
        Cobweb::class,
        Lag::class,
        Wither::class,
        AllowFight::class,
        BlockDisappear::class,
        HotSun::class,
        Murder::class,
        ZeroGravity::class,
        ExplosiveSheep::class,
        FloorIsLava::class,
        Grounded::class,
        Lightning::class,
        OneHearth::class,
        Swap::class,
        WorldBorder::class,
        NoJump::class
    )

    private inline fun <reified T : Disaster> getDisaster(arena: Arena): T? {
        return activeDisasters[arena]?.find { it is T } as? T
    }

    fun addRandomDisaster(arena: Arena) {
        val maxDisasters = arena.maxDisasters
        val currentDisasters = activeDisasters.getOrPut(arena) { mutableListOf() }

        if (currentDisasters.size >= maxDisasters) {
            val toRemove = currentDisasters.removeAt(0)
            toRemove.stop(arena)
        }

        val available = disasterClasses.filter { cls ->
            currentDisasters.none { it::class == cls }
        }

        if (available.isNotEmpty()) {
            val newDisaster = available.random().constructors.first().call()
            newDisaster.start(arena)
            currentDisasters.add(newDisaster)
            arena.disasters.add(newDisaster)
        }
    }

    fun pulseAll(time: Int) {
        activeDisasters.forEach { (_, disasters) ->
            disasters.forEach { it.pulse(time) }
        }
    }

    fun removeDisasters(arena: Arena) {
        activeDisasters[arena]?.forEach { it.stop(arena) }
        activeDisasters.remove(arena)
        arena.disasters.clear()
    }

    fun addBlockToDisappear(arena: Arena, location: Location) {
        if(location.block.type.isAir) return
        val disaster = activeDisasters[arena]?.find { it is BlockDisappear } as? BlockDisappear
        if (disaster == null) {
        } else {
            val block = DisappearBlock(arena, location)
            disaster.addBlock(block)
        }
    }


    fun removeBlockFromDisappear(arena: Arena, block: DisappearBlock) {
        getDisaster<BlockDisappear>(arena)?.removeBlock(block)
    }

    fun addBlockToFloorIsLava(arena: Arena, location: Location) {
        if(location.block.type.isAir) return
        val block = DisasterFloor(arena, location)
        getDisaster<FloorIsLava>(arena)?.addBlock(block)
    }

    fun removeBlockFromFloorIsLava(arena: Arena, block: DisasterFloor) {
        getDisaster<FloorIsLava>(arena)?.removeBlock(block)
    }

    fun isGrounded(arena: Arena, player: Player): Boolean {
        return getDisaster<Grounded>(arena)?.isGrounded(player) ?: false
    }

    fun isAllowedToFight(arena: Arena, player: Player): Boolean {
        return getDisaster<AllowFight>(arena)?.isAllowed(player) ?: false
    }

    fun isMurder(arena: Arena, player: Player): Boolean {
        return getDisaster<Murder>(arena)?.isMurder(player) ?: false
    }
}
