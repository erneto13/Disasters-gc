package me.hhitt.disasters.model.block

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.world.level.block.Blocks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer

class DisappearBlock(private val arena: Arena, val location: Location)  {

    private val materials = listOf(
        Material.RED_WOOL,
        Material.AIR
    )
    private var currentStage = 0

    fun updateMaterial() {
        if (currentStage < materials.size) {
            setBlockMaterial(location, materials[currentStage])
            currentStage++
        } else {
            DisasterRegistry.removeBlockFromDisappear(arena, this)
        }
    }

    private fun setBlockMaterial(location: Location, material: Material) {
        val worldServer = (location.world as CraftWorld).handle
        val blockPosition = BlockPos(location.blockX, location.blockY - 1, location.blockZ)
        val blockData = when (material) {
            Material.RED_WOOL -> Blocks.RED_WOOL.defaultBlockState()
            Material.AIR -> Blocks.AIR.defaultBlockState()
            else -> return
        }
        worldServer.setBlockAndUpdate(blockPosition, blockData)
        val packet = ClientboundBlockUpdatePacket(worldServer, blockPosition)
        arena.playing.forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }
}