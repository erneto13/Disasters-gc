package me.hhitt.disasters.disaster

import MeteorShower
import kotlin.reflect.KClass
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.impl.*
import me.hhitt.disasters.model.block.DisappearBlock
import me.hhitt.disasters.model.block.DisasterFloor
import org.bukkit.Location
import org.bukkit.entity.Player

object DisasterRegistry {

    private val activeDisasters = mutableMapOf<Arena, MutableList<Disaster>>()
    private val activationCounts = mutableMapOf<Arena, Int>()

    private val disasterClasses =
            listOf(
                    AcidRain::class,
                    Apocalypse::class,
                    Blind::class,
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
                    NoJump::class,
                    MeteorShower::class
            )

    init {
        DisasterConfig.load()
    }

    private inline fun <reified T : Disaster> getDisaster(arena: Arena): T? {
        return activeDisasters[arena]?.find { it is T } as? T
    }

    fun getDisasterClassByName(name: String): KClass<out Disaster>? {
        return disasterClasses.find { it.simpleName?.equals(name, ignoreCase = true) == true }
    }

    fun addRandomDisaster(arena: Arena) {
        val maxDisasters = arena.maxDisasters
        val currentDisasters = activeDisasters.getOrPut(arena) { mutableListOf() }
        val currentCount = activationCounts.getOrPut(arena) { 0 }

        val wave = DisasterConfig.getCurrentWave(currentCount)

        val availableByPriority =
                DisasterConfig.getDisastersByPriority(disasterClasses, wave.priority)

        val notActive =
                availableByPriority.filter { cls -> currentDisasters.none { it::class == cls } }

        val compatible = mutableListOf<KClass<out Disaster>>()
        for (cls in notActive) {
            if (DisasterConfig.isCompatibleWithActive(cls, currentDisasters)) {
                compatible.add(cls)
            }
        }

        if (compatible.isEmpty()) {
            activationCounts[arena] = currentCount + 1
            return
        }

        val toAdd = wave.count.coerceAtMost(compatible.size)
        val shuffled = compatible.shuffled()

        var actuallyAdded = 0

        for (i in 0 until toAdd) {
            while (currentDisasters.size >= maxDisasters) {
                val toRemove = currentDisasters.removeAt(0)
                toRemove.stop(arena)
            }

            val selectedClass = shuffled[i]

            if (!DisasterConfig.isCompatibleWithActive(selectedClass, currentDisasters)) {
                continue
            }

            try {
                val newDisaster = selectedClass.constructors.first().call()
                newDisaster.start(arena)
                currentDisasters.add(newDisaster)
                arena.disasters.add(newDisaster)
                actuallyAdded++
            } catch (e: Exception) {}
        }

        if (actuallyAdded > 0) {
            activationCounts[arena] = currentCount + 1
        }
    }

    fun pulseAll(time: Int) {
        activeDisasters.forEach { (_, disasters) -> disasters.forEach { it.pulse(time) } }
    }

    fun removeDisasters(arena: Arena) {
        activeDisasters[arena]?.forEach { it.stop(arena) }
        activeDisasters.remove(arena)
        activationCounts.remove(arena)
        arena.disasters.clear()
    }

    fun addBlockToDisappear(arena: Arena, location: Location) {
        val block = location.block

        if (!block.type.isSolid || block.type.isAir) return

        if (block.type == org.bukkit.Material.WATER || block.type == org.bukkit.Material.LAVA)
                return

        val disaster = activeDisasters[arena]?.find { it is BlockDisappear } as? BlockDisappear
        disaster?.addBlock(DisappearBlock(arena, location))
    }

    fun registerDisaster(arena: Arena, disaster: Disaster) {
        val list = activeDisasters.getOrPut(arena) { mutableListOf() }
        list.add(disaster)
        arena.disasters.add(disaster)
    }

    fun removeBlockFromDisappear(arena: Arena, block: DisappearBlock) {
        getDisaster<BlockDisappear>(arena)?.removeBlock(block)
    }

    fun addBlockToFloorIsLava(arena: Arena, location: Location) {
        if (location.block.type.isAir) return

        val floorDisaster = getDisaster<FloorIsLava>(arena) ?: return
        val stages = floorDisaster.getStages()

        val block = DisasterFloor(arena, location, stages)
        floorDisaster.addBlock(block)
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

    fun reloadConfig() {
        DisasterConfig.reload()
    }
}
