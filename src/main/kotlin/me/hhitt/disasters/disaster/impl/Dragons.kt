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

    override fun start(arena: Arena) {
        loadConfig()
        arenas.add(arena)

        val minX = min(arena.corner1.x, arena.corner2.x)
        val maxX = max(arena.corner1.x, arena.corner2.x)
        val minY = min(arena.corner1.y, arena.corner2.y)
        val maxY = max(arena.corner1.y, arena.corner2.y)
        val minZ = min(arena.corner1.z, arena.corner2.z)
        val maxZ = max(arena.corner1.z, arena.corner2.z)

        val centerX = (minX + maxX) / 2
        val centerY = (minY + maxY) / 2
        val centerZ = (minZ + maxZ) / 2

        val world = arena.corner1.world

        for (i in 0 until dragonCount) {
            val offsetX = if (i == 0) horizontalSpread else -horizontalSpread
            val spawnLoc = Location(world, centerX + offsetX, centerY + spawnHeightOffset, centerZ)

            val dragon = world.spawnEntity(spawnLoc, EntityType.ENDER_DRAGON) as EnderDragon

            dragon.isCustomNameVisible = false
            dragon.setAI(true)
            dragon.phase = EnderDragon.Phase.values()[dragonPhase]

            EntityUtils.setScaledHealth(dragon, healthMultiplier)

            dragons.add(dragon)
        }

        Notify.disaster(arena, "dragons")
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

            if (enforceBoundaries && tickCounter % retargetInterval == 0) {
                val arena = arenas.firstOrNull() ?: continue
                keepDragonInBounds(dragon, arena)
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
        dragonPhase = config.getInt("dragon-phase", 0).coerceIn(0, 10)
        enforceBoundaries = config.getBoolean("enforce-boundaries", true)
        boundaryPadding = config.getInt("boundary-padding", 5)
        canAttackPlayers = config.getBoolean("can-attack-players", true)
        healthMultiplier = config.getDouble("health-multiplier", 1.0)
        dropLoot = config.getBoolean("drop-loot", false)
    }

    private fun keepDragonInBounds(dragon: EnderDragon, arena: Arena) {
        val minX = min(arena.corner1.x, arena.corner2.x) + boundaryPadding
        val maxX = max(arena.corner1.x, arena.corner2.x) - boundaryPadding
        val minY = min(arena.corner1.y, arena.corner2.y) + minHeightAboveFloor
        val maxY = min(arena.corner1.y, arena.corner2.y) + maxHeightAboveFloor
        val minZ = min(arena.corner1.z, arena.corner2.z) + boundaryPadding
        val maxZ = max(arena.corner1.z, arena.corner2.z) - boundaryPadding

        val targetX = Random.nextDouble(minX, maxX)
        val targetY = Random.nextDouble(minY.toDouble(), maxY.toDouble())
        val targetZ = Random.nextDouble(minZ, maxZ)

        val targetLoc = Location(arena.corner1.world, targetX, targetY, targetZ)

        dragon.phase = EnderDragon.Phase.CIRCLING

        val currentLoc = dragon.location
        val direction = targetLoc.toVector().subtract(currentLoc.toVector()).normalize()
        val newLoc = currentLoc.add(direction.multiply(0.5))

        dragon.velocity = direction.multiply(0.3)
    }
}
