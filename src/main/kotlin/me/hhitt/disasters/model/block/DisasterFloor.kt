package me.hhitt.disasters.model.block

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.world.level.block.Blocks
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer

class DisasterFloor(
        private val arena: Arena,
        val location: Location,
        private val stages: List<Material>
) {

    private var currentStage = 0
    private var ticksSinceLastUpdate = 0
    private val ticksPerStage: Int

    init {
        val config =
                me.hhitt.disasters.storage.file.DisasterFileManager.getDisasterConfig(
                        "floor-is-lava"
                )
        ticksPerStage = config?.getInt("ticks-per-stage", 5) ?: 5
    }

    fun tick() {
        ticksSinceLastUpdate++

        if (ticksSinceLastUpdate >= ticksPerStage) {
            ticksSinceLastUpdate = 0
            updateMaterial()
        }
    }

    fun updateMaterial() {
        if (currentStage < stages.size) {
            setBlockMaterial(location, stages[currentStage])
            currentStage++
        } else {
            DisasterRegistry.removeBlockFromFloorIsLava(arena, this)
        }
    }

    private fun setBlockMaterial(location: Location, material: Material) {
        val worldServer = (location.world as CraftWorld).handle
        val blockPosition = BlockPos(location.blockX, location.blockY, location.blockZ)

        val blockData =
                when (material) {
                    Material.YELLOW_WOOL -> Blocks.YELLOW_WOOL.defaultBlockState()
                    Material.ORANGE_WOOL -> Blocks.ORANGE_WOOL.defaultBlockState()
                    Material.RED_WOOL -> Blocks.RED_WOOL.defaultBlockState()
                    Material.LAVA -> Blocks.LAVA.defaultBlockState()
                    Material.RED_CONCRETE -> Blocks.RED_CONCRETE.defaultBlockState()
                    Material.ORANGE_CONCRETE -> Blocks.ORANGE_CONCRETE.defaultBlockState()
                    Material.YELLOW_CONCRETE -> Blocks.YELLOW_CONCRETE.defaultBlockState()
                    else -> return
                }

        worldServer.setBlockAndUpdate(blockPosition, blockData)

        val packet = ClientboundBlockUpdatePacket(worldServer, blockPosition)
        arena.playing.forEach { player -> (player as CraftPlayer).handle.connection.send(packet) }
    }
}
