package me.hhitt.disasters.disaster.impl

import java.util.concurrent.CopyOnWriteArrayList
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.model.block.DisasterFloor
import me.hhitt.disasters.storage.file.DisasterFileManager
import me.hhitt.disasters.util.Notify
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class FloorIsLava : Disaster {

    private val blocks = CopyOnWriteArrayList<DisasterFloor>()
    private var stages: List<Material> = listOf()
    private var fastTickTask: BukkitTask? = null
    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        loadConfig()
        arenas.add(arena)
        startFastTicker()
    }

    override fun pulse(time: Int) {}

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        blocks.clear()
        stopFastTicker()
    }

    private fun startFastTicker() {
        fastTickTask =
                object : BukkitRunnable() {
                            override fun run() {
                                if (arenas.isEmpty()) {
                                    cancel()
                                    return
                                }
                                blocks.forEach { it.tick() }
                            }
                        }
                        .runTaskTimer(Disasters.getInstance(), 0L, 1L)
    }

    private fun stopFastTicker() {
        fastTickTask?.cancel()
        fastTickTask = null
    }

    fun addBlock(block: DisasterFloor) {
        blocks.add(block)
    }

    fun removeBlock(block: DisasterFloor) {
        blocks.remove(block)
    }

    private fun loadConfig() {
        val config = DisasterFileManager.getDisasterConfig("floor-is-lava")

        if (config == null) {
            Disasters.getInstance()
                    .logger
                    .warning("FloorIsLava config not found! Using default values.")
            stages =
                    listOf(
                            Material.YELLOW_WOOL,
                            Material.ORANGE_WOOL,
                            Material.RED_WOOL,
                            Material.LAVA
                    )
            return
        }

        val stageNames = config.getStringList("stages")
        stages =
                stageNames.mapNotNull { stageName ->
                    try {
                        Material.valueOf(stageName.uppercase())
                    } catch (e: IllegalArgumentException) {
                        Disasters.getInstance()
                                .logger
                                .warning("Invalid material in floor-is-lava config: $stageName")
                        null
                    }
                }

        if (stages.isEmpty()) {
            stages =
                    listOf(
                            Material.YELLOW_WOOL,
                            Material.ORANGE_WOOL,
                            Material.RED_WOOL,
                            Material.LAVA
                    )
        }
    }

    fun getStages(): List<Material> = stages
}
