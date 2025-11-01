package me.hhitt.disasters.model.block

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import net.minecraft.world.level.block.Blocks
import org.bukkit.craftbukkit.entity.CraftPlayer

class DisasterFloor(private val arena: Arena, val location: Location) {
    private val materials = listOf(
        Material.YELLOW_WOOL,
        Material.ORANGE_WOOL,
        Material.RED_WOOL,
        Material.LAVA
    )
    private var currentStage = 0

    fun updateMaterial() {
        if (currentStage >= materials.size) {
            DisasterRegistry.removeBlockFromFloorIsLava(arena, this)
            return
        }

        val targetMaterial = materials[currentStage]
        
        arena.playing.firstOrNull()?.sendMessage("§7[DEBUG] Cambiando bloque a $targetMaterial en stage $currentStage")
        
        setBlockMaterial(location, targetMaterial)
        currentStage++
    }

    private fun setBlockMaterial(location: Location, material: Material) {
        try {
            val worldServer = (location.world as CraftWorld).handle
            val blockPosition = BlockPos(location.blockX, location.blockY, location.blockZ)
            
            val blockData = when (material) {
                Material.YELLOW_WOOL -> Blocks.YELLOW_WOOL.defaultBlockState()
                Material.ORANGE_WOOL -> Blocks.ORANGE_WOOL.defaultBlockState()
                Material.RED_WOOL -> Blocks.RED_WOOL.defaultBlockState()
                Material.LAVA -> Blocks.LAVA.defaultBlockState()
                else -> {
                    arena.playing.firstOrNull()?.sendMessage("§c[DEBUG] Material no soportado: $material")
                    return
                }
            }
            
            worldServer.setBlockAndUpdate(blockPosition, blockData)
            
            val packet = ClientboundBlockUpdatePacket(worldServer, blockPosition)
            arena.playing.forEach { player -> 
                (player as CraftPlayer).handle.connection.send(packet) 
            }
        } catch (e: Exception) {
            arena.playing.firstOrNull()?.sendMessage("§c[DEBUG] Error al cambiar bloque: ${e.message}")
            e.printStackTrace()
        }
    }
}