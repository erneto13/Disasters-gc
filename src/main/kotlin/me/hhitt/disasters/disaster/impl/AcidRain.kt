package me.hhitt.disasters.disaster.impl

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.DisasterFileManager
import me.hhitt.disasters.util.Notify
import me.hhitt.disasters.util.PS
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.WeatherType
import org.bukkit.block.Block
import org.bukkit.craftbukkit.entity.CraftPlayer

class AcidRain : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val damagingBlocks = mutableMapOf<Block, Int>()
    private var damageCounter = 0

    private var playerDamageInterval = 40
    private var initialDamage = 0.5
    private var maxDamage = 2.0
    private var damageIncreaseRate = 80
    private var blockDamageCheckInterval = 5
    private var blocksToDamagePerPulse = 15
    private var blockDamageTicksToBreak = 100
    private var breakStageInterval = 11
    private var enableParticles = true
    private var enableSounds = true

    override fun start(arena: Arena) {
        loadConfig()

        arena.playing.forEach {
            val player: CraftPlayer = it as CraftPlayer
            player.handle.connection.player.setPlayerWeather(WeatherType.DOWNFALL, true)
        }
        arenas.add(arena)
        damagingBlocks.clear()
        damageCounter = 0
        Notify.disaster(arena, "acid-rain")
    }

    override fun pulse(time: Int) {
        arenas.toList().forEach { arena ->
            if (damageCounter % playerDamageInterval == 0) {
                arena.alive.toList().forEach { player ->
                    if (!isCovered(player.location.block)) {
                        val damageAmount =
                                min(
                                        maxDamage,
                                        initialDamage + (damageCounter / damageIncreaseRate) * 0.5
                                )
                        player.damage(damageAmount)

                        if (enableParticles) {
                            spawnAcidParticles(player)
                        }

                        if (enableSounds) {
                            PS.playSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 0.3f, 1.5f)
                        }
                    }
                }
            }

            processBlockDamage(arena)

            if (damageCounter % blockDamageCheckInterval == 0) {
                findNewBlocksToDamage(arena)
            }

            damageCounter++
        }
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach {
            val player: CraftPlayer = it as CraftPlayer
            player.handle.connection.player.setPlayerWeather(WeatherType.CLEAR, true)
        }

        damagingBlocks.keys.forEach { block ->
            val blockPos = BlockPos(block.x, block.y, block.z)
            val blockId = block.hashCode()
            val clearPacket = ClientboundBlockDestructionPacket(blockId, blockPos, -1)

            arena.playing.forEach { player ->
                (player as CraftPlayer).handle.connection.send(clearPacket)
            }
        }

        damagingBlocks.clear()
        arenas.remove(arena)
    }

    private fun loadConfig() {
        val config = DisasterFileManager.getDisasterConfig("acid-rain")

        if (config == null) {
            Disasters.getInstance()
                    .logger
                    .warning("AcidRain config not found! Using default values.")
            return
        }

        playerDamageInterval = config.getInt("player-damage-interval", 40)
        initialDamage = config.getDouble("initial-damage", 0.5)
        maxDamage = config.getDouble("max-damage", 2.0)
        damageIncreaseRate = config.getInt("damage-increase-rate", 80)
        blockDamageCheckInterval = config.getInt("block-damage-check-interval", 5)
        blocksToDamagePerPulse = config.getInt("blocks-to-damage-per-pulse", 15)
        blockDamageTicksToBreak = config.getInt("block-damage-ticks-to-break", 100)
        breakStageInterval = config.getInt("break-stage-interval", 11)
        enableParticles = config.getBoolean("enable-particles", true)
        enableSounds = config.getBoolean("enable-sounds", true)
    }

    private fun isCovered(block: Block): Boolean {
        val world = block.world
        val playerY = block.y

        for (y in playerY + 1 until world.maxHeight) {
            val aboveBlock = world.getBlockAt(block.x, y, block.z)
            if (aboveBlock.type != Material.AIR) {
                return true
            }
        }
        return false
    }

    private fun findNewBlocksToDamage(arena: Arena) {
        val world = arena.corner1.world
        val minX = min(arena.corner1.blockX, arena.corner2.blockX)
        val maxX = max(arena.corner1.blockX, arena.corner2.blockX)
        val minZ = min(arena.corner1.blockZ, arena.corner2.blockZ)
        val maxZ = max(arena.corner1.blockZ, arena.corner2.blockZ)
        val maxY = max(arena.corner1.blockY, arena.corner2.blockY)

        repeat(blocksToDamagePerPulse) {
            val x = Random.nextInt(minX, maxX + 1)
            val z = Random.nextInt(minZ, maxZ + 1)

            for (y in maxY downTo 0) {
                val block = world.getBlockAt(x, y, z)
                if (block.type != Material.AIR && !damagingBlocks.containsKey(block)) {
                    if (!isCovered(block)) {
                        damagingBlocks[block] = 0
                        break
                    }
                }
            }
        }
    }

    private fun processBlockDamage(arena: Arena) {
        val iterator = damagingBlocks.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val block = entry.key
            val damage = entry.value

            if (block.type == Material.AIR || block.type == Material.VOID_AIR) {
                iterator.remove()
                continue
            }

            val newDamage = damage + 1

            if (enableParticles && newDamage % 5 == 0) {
                spawnBlockDamageParticles(block)
            }

            val breakStage = min(9, (newDamage / breakStageInterval))
            if (breakStage >= 0) {
                val blockPos = BlockPos(block.x, block.y, block.z)
                val blockId = block.hashCode()
                val packet = ClientboundBlockDestructionPacket(blockId, blockPos, breakStage)

                arena.playing.forEach { player ->
                    (player as CraftPlayer).handle.connection.send(packet)
                }
            }

            if (newDamage >= blockDamageTicksToBreak) {
                val blockPos = BlockPos(block.x, block.y, block.z)
                val blockId = block.hashCode()
                val clearPacket = ClientboundBlockDestructionPacket(blockId, blockPos, -1)

                arena.playing.forEach { player ->
                    (player as CraftPlayer).handle.connection.send(clearPacket)
                }

                block.breakNaturally()
                iterator.remove()

                if (enableParticles) {
                    spawnBlockBreakParticles(block)
                }

                if (enableSounds) {
                    PS.playSound(block.location, Sound.BLOCK_STONE_BREAK, 0.5f, 0.8f)
                }
            } else {
                damagingBlocks[block] = newDamage
            }
        }
    }

    private fun spawnAcidParticles(player: org.bukkit.entity.Player) {
        val location = player.location.add(0.0, 1.5, 0.0)

        PS.spawnParticles(location, Particle.SCRAPE, 15, 0.3, 0.3, 0.3, 0.02)
        PS.spawnParticles(location, Particle.CRIT, 10, 0.2, 0.2, 0.2, 0.01)
        PS.spawnParticles(location, Particle.SMOKE, 5, 0.2, 0.2, 0.2, 0.01)
        PS.spawnParticles(
                location,
                Particle.DUST,
                8,
                0.25,
                0.25,
                0.25,
                0.01,
                Particle.DustOptions(Color.fromRGB(50, 200, 50), 1.0f)
        )
    }

    private fun spawnBlockDamageParticles(block: Block) {
        val location = block.location.add(0.5, 1.0, 0.5)

        PS.spawnParticles(location, Particle.SMOKE, 3, 0.3, 0.1, 0.3, 0.01)
        PS.spawnParticles(location, Particle.SCRAPE, 2, 0.2, 0.1, 0.2, 0.01)
    }

    private fun spawnBlockBreakParticles(block: Block) {
        val location = block.location.add(0.5, 0.5, 0.5)

        PS.spawnParticles(location, Particle.BLOCK, 30, 0.3, 0.3, 0.3, 0.1, block.blockData)
        PS.spawnParticles(location, Particle.CRIT, 10, 0.3, 0.3, 0.3, 0.05)
    }
}
