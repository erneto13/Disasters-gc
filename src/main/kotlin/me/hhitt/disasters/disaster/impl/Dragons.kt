package me.hhitt.disasters.disaster.impl

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.DisasterFileManager
import me.hhitt.disasters.util.EntityUtils
import me.hhitt.disasters.util.Notify
import org.bukkit.Location
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.EntityType

class Dragons : Disaster {

    private val dragons = mutableListOf<EnderDragon>()
    private val arenas = mutableListOf<Arena>()
    private var tickCounter = 0

    private var dragonCount = 2
    private var spawnHeightOffset = 20
    private var horizontalSpread = 15
    private var retargetInterval = 40
    private var maxHeightAboveFloor = 30
    private var minHeightAboveFloor = 15
    private var dragonPhase = 0
    private var enforceBoundaries = true
    private var boundaryPadding = 5
    private var canAttackPlayers = true
    private var healthMultiplier = 1.0
    private var dropLoot = false
    private var fireballExplosionPower = 3.0f
    private var fireballRate = 15
    private var fireballDestroyBlocks = true
    private var randomFireballChance = 0.6f
    private var playExplosionSounds = true

    override fun start(arena: Arena) {
        loadConfig()
        arenas.add(arena)

        val minX = min(arena.corner1.x, arena.corner2.x)
        val maxX = max(arena.corner1.x, arena.corner2.x)
        val minY = min(arena.corner1.y, arena.corner2.y)
        val minZ = min(arena.corner1.z, arena.corner2.z)
        val maxZ = max(arena.corner1.z, arena.corner2.z)

        val centerX = (minX + maxX) / 2
        val centerZ = (minZ + maxZ) / 2
        val floorY = minY

        val world = arena.corner1.world

        for (i in 0 until dragonCount) {
            val angle = (2 * Math.PI * i) / dragonCount
            val offsetX = Math.cos(angle) * horizontalSpread
            val offsetZ = Math.sin(angle) * horizontalSpread

            val spawnLoc =
                    Location(
                            world,
                            centerX + offsetX,
                            floorY + spawnHeightOffset,
                            centerZ + offsetZ
                    )

            val dragon = world.spawnEntity(spawnLoc, EntityType.ENDER_DRAGON) as EnderDragon

            dragon.isCustomNameVisible = false
            dragon.setAI(true)

            val phase =
                    when (dragonPhase) {
                        0 -> EnderDragon.Phase.CIRCLING
                        1 -> EnderDragon.Phase.CHARGE_PLAYER
                        2 -> EnderDragon.Phase.LAND_ON_PORTAL
                        3 -> EnderDragon.Phase.STRAFING
                        10 -> EnderDragon.Phase.HOVER
                        else -> EnderDragon.Phase.CIRCLING
                    }
            dragon.phase = phase

            EntityUtils.setScaledHealth(dragon, healthMultiplier)

            dragons.add(dragon)
        }

    }

    override fun pulse(time: Int) {
        tickCounter++

        val iterator = dragons.iterator()
        while (iterator.hasNext()) {
            val dragon = iterator.next()
            if (!dragon.isValid || dragon.isDead) {
                iterator.remove()
                continue
            }

            // keep dragons in bounds and moving
            if (enforceBoundaries && tickCounter % retargetInterval == 0) {
                val arena = arenas.firstOrNull() ?: continue
                keepDragonInBounds(dragon, arena)
            }

            // ensure dragon stays in aggressive phase
            if (tickCounter % 20 == 0) {
                val targetPhase =
                        when (dragonPhase) {
                            0 -> EnderDragon.Phase.CIRCLING
                            1 -> EnderDragon.Phase.CHARGE_PLAYER
                            3 -> EnderDragon.Phase.STRAFING
                            10 -> EnderDragon.Phase.HOVER
                            else -> EnderDragon.Phase.CIRCLING
                        }

                if (dragon.phase != targetPhase) {
                    dragon.phase = targetPhase
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)

        dragons.forEach { dragon ->
            if (dragon.isValid) {
                if (!dropLoot) {
                    dragon.remove()
                } else {
                    dragon.health = 0.0
                }
            }
        }
        dragons.clear()
        tickCounter = 0
    }

    private fun loadConfig() {
        val config = DisasterFileManager.getDisasterConfig("dragons")

        if (config == null) {
            Disasters.getInstance()
                    .logger
                    .warning("Dragons config not found! Using default values.")
            return
        }

        dragonCount = config.getInt("dragon-count", 2)
        spawnHeightOffset = config.getInt("spawn-height-offset", 20)
        horizontalSpread = config.getInt("horizontal-spread", 15)
        retargetInterval = config.getInt("retarget-interval", 40)
        maxHeightAboveFloor = config.getInt("max-height-above-floor", 30)
        minHeightAboveFloor = config.getInt("min-height-above-floor", 15)

        // load dragon phase and validate
        val configPhase = config.getInt("dragon-phase", 0)
        dragonPhase =
                when (configPhase) {
                    in 0..10 -> configPhase
                    else -> {
                        Disasters.getInstance()
                                .logger
                                .warning("Invalid dragon-phase: $configPhase, using 0")
                        0
                    }
                }

        enforceBoundaries = config.getBoolean("enforce-boundaries", true)
        boundaryPadding = config.getInt("boundary-padding", 5)
        canAttackPlayers = config.getBoolean("can-attack-players", true)
        healthMultiplier = config.getDouble("health-multiplier", 1.0)
        dropLoot = config.getBoolean("drop-loot", false)

        Disasters.getInstance()
                .logger
                .info(
                        "Dragons config loaded - Phase: $dragonPhase, Height: $minHeightAboveFloor-$maxHeightAboveFloor"
                )
    }

    private fun keepDragonInBounds(dragon: EnderDragon, arena: Arena) {
        val minX = min(arena.corner1.x, arena.corner2.x) + boundaryPadding
        val maxX = max(arena.corner1.x, arena.corner2.x) - boundaryPadding
        val minY = min(arena.corner1.y, arena.corner2.y)
        val minZ = min(arena.corner1.z, arena.corner2.z) + boundaryPadding
        val maxZ = max(arena.corner1.z, arena.corner2.z) - boundaryPadding

        val currentY = dragon.location.y
        val floorY = minY

        // calculate target height based on floor
        val targetMinY = floorY + minHeightAboveFloor
        val targetMaxY = floorY + maxHeightAboveFloor

        // if dragon is too high or too low, bring it back
        val targetY =
                if (currentY > targetMaxY) {
                    targetMaxY.toDouble()
                } else if (currentY < targetMinY) {
                    targetMinY.toDouble()
                } else {
                    Random.nextDouble(targetMinY.toDouble(), targetMaxY.toDouble())
                }

        val targetX = Random.nextDouble(minX, maxX)
        val targetZ = Random.nextDouble(minZ, maxZ)

        val targetLoc = Location(arena.corner1.world, targetX, targetY, targetZ)

        // set phase based on config
        val targetPhase =
                when (dragonPhase) {
                    0 -> EnderDragon.Phase.CIRCLING
                    1 -> EnderDragon.Phase.CHARGE_PLAYER
                    3 -> EnderDragon.Phase.STRAFING
                    10 -> EnderDragon.Phase.HOVER
                    else -> EnderDragon.Phase.CIRCLING
                }

        dragon.phase = targetPhase

        // if there are players, sometimes target them
        if (arena.alive.isNotEmpty() && Random.nextFloat() < 0.4f) {
            val randomPlayer = arena.alive.random()
            val playerLoc = randomPlayer.location

            // target near player but at configured height
            val nearPlayerLoc =
                    Location(
                            arena.corner1.world,
                            playerLoc.x + Random.nextDouble(-5.0, 5.0),
                            floorY +
                                    minHeightAboveFloor +
                                    Random.nextInt(0, maxHeightAboveFloor - minHeightAboveFloor),
                            playerLoc.z + Random.nextDouble(-5.0, 5.0)
                    )

            val direction =
                    nearPlayerLoc.toVector().subtract(dragon.location.toVector()).normalize()
            dragon.velocity = direction.multiply(0.5)
        } else {
            val direction = targetLoc.toVector().subtract(dragon.location.toVector()).normalize()
            dragon.velocity = direction.multiply(0.4)
        }
    }
}
