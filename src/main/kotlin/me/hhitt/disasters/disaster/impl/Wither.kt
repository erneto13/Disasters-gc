package me.hhitt.disasters.disaster.impl

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.DisasterFileManager
import me.hhitt.disasters.util.EntityUtils
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Wither
import org.bukkit.entity.WitherSkull
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Wither : Disaster {
    private val activeWithers = mutableMapOf<Arena, MutableList<Wither>>()
    private var tickCounter = 0

    private var witherCount = 1
    private var spawnHeightOffset = 15
    private var spawnSpread = true
    private var spawnSpreadRadius = 10
    private var healthMultiplier = 2.0
    private var enableAI = true
    private var showCustomName = false
    private var customName = "<red><bold>BOSS WITHER"
    private var retargetInterval = 40
    private var targetClosestPlayer = true
    private var breakBlocks = true
    private var explodeOnDeath = false
    private var skullShootInterval = 30
    private var shootChargedSkulls = true
    private var chargedSkullChance = 0.5f
    private var skullExplosionPower = 3.0f
    private var skullDestroyBlocks = true
    private var shootRandomSkulls = true
    private var randomSkullChance = 0.3f
    private var enforceBoundaries = true
    private var boundaryPadding = 5
    private var maxHeightAboveFloor = 30
    private var minHeightAboveFloor = 10
    private var movementSpeed = 1.0
    private var dropLoot = false
    private var alwaysDropNetherStar = false
    private var applyWitherEffect = true
    private var witherEffectRadius = 10
    private var witherEffectInterval = 60
    private var witherEffectDuration = 100
    private var witherEffectAmplifier = 1

    override fun start(arena: Arena) {
        loadConfig()
        val witherList = mutableListOf<Wither>()

        val minX = min(arena.corner1.x, arena.corner2.x)
        val maxX = max(arena.corner1.x, arena.corner2.x)
        val minY = min(arena.corner1.y, arena.corner2.y)
        val minZ = min(arena.corner1.z, arena.corner2.z)
        val maxZ = max(arena.corner1.z, arena.corner2.z)

        val centerX = (minX + maxX) / 2
        val centerZ = (minZ + maxZ) / 2
        val floorY = minY

        val world = arena.corner1.world

        repeat(witherCount) { i ->
            val spawnLoc =
                    if (spawnSpread && witherCount > 1) {
                        val angle = (2 * Math.PI * i) / witherCount
                        val offsetX = Math.cos(angle) * spawnSpreadRadius
                        val offsetZ = Math.sin(angle) * spawnSpreadRadius
                        Location(
                                world,
                                centerX + offsetX,
                                floorY + spawnHeightOffset,
                                centerZ + offsetZ
                        )
                    } else {
                        Location(world, centerX, floorY + spawnHeightOffset, centerZ)
                    }

            val wither = world.spawnEntity(spawnLoc, EntityType.WITHER) as Wither

            wither.setAI(enableAI)
            EntityUtils.setScaledHealth(wither, healthMultiplier)

            if (showCustomName) {
                wither.customName(me.hhitt.disasters.util.Msg.parse(customName))
                wither.isCustomNameVisible = true
            }

            witherList.add(wither)
        }

        activeWithers[arena] = witherList
        tickCounter = 0
    }

    override fun pulse(time: Int) {
        tickCounter++

        activeWithers.forEach { (arena, withers) ->
            withers.removeIf { wither ->
                if (!wither.isValid || wither.isDead) {
                    if (explodeOnDeath && wither.isDead) {
                        wither.location.world.createExplosion(
                                wither.location,
                                skullExplosionPower * 2,
                                false,
                                true
                        )
                    }
                    true
                } else {
                    false
                }
            }

            withers.forEach { wither ->
                // Retarget players
                if (tickCounter % retargetInterval == 0) {
                    if (arena.alive.isNotEmpty()) {
                        wither.target =
                                if (targetClosestPlayer) {
                                    getClosestPlayer(wither, arena)
                                } else {
                                    arena.alive.random()
                                }
                    }
                }

                // Keep within boundaries
                if (enforceBoundaries && tickCounter % retargetInterval == 0) {
                    keepWitherInBounds(wither, arena)
                }

                // Shoot skulls
                if (tickCounter % skullShootInterval == 0) {
                    if (wither.target != null && wither.target is Player) {
                        shootSkullAtPlayer(wither, wither.target as Player)
                    } else if (shootRandomSkulls && Random.nextFloat() < randomSkullChance) {
                        shootAtRandomBlock(wither, arena)
                    }
                }

                // Apply wither effect to nearby players
                if (applyWitherEffect && tickCounter % witherEffectInterval == 0) {
                    applyWitherEffectToNearbyPlayers(wither, arena)
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        activeWithers.remove(arena)?.forEach { wither ->
            if (wither.isValid) {
                if (!dropLoot && !alwaysDropNetherStar) {
                    wither.remove()
                } else {
                    wither.health = 0.0
                }
            }
        }
        tickCounter = 0
    }

    private fun loadConfig() {
        val config = DisasterFileManager.getDisasterConfig("wither")

        if (config == null) {
            Disasters.getInstance().logger.warning("Wither config not found! Using default values.")
            return
        }

        witherCount = config.getInt("wither-count", 1)
        spawnHeightOffset = config.getInt("spawn-height-offset", 15)
        spawnSpread = config.getBoolean("spawn-spread", true)
        spawnSpreadRadius = config.getInt("spawn-spread-radius", 10)
        healthMultiplier = config.getDouble("health-multiplier", 2.0)
        enableAI = config.getBoolean("enable-ai", true)
        showCustomName = config.getBoolean("show-custom-name", false)
        customName = config.getString("custom-name") ?: "<red><bold>BOSS WITHER"
        retargetInterval = config.getInt("retarget-interval", 40)
        targetClosestPlayer = config.getBoolean("target-closest-player", true)
        breakBlocks = config.getBoolean("break-blocks", true)
        explodeOnDeath = config.getBoolean("explode-on-death", false)
        skullShootInterval = config.getInt("skull-shoot-interval", 30)
        shootChargedSkulls = config.getBoolean("shoot-charged-skulls", true)
        chargedSkullChance = config.getDouble("charged-skull-chance", 0.5).toFloat()
        skullExplosionPower = config.getDouble("skull-explosion-power", 3.0).toFloat()
        skullDestroyBlocks = config.getBoolean("skull-destroy-blocks", true)
        shootRandomSkulls = config.getBoolean("shoot-random-skulls", true)
        randomSkullChance = config.getDouble("random-skull-chance", 0.3).toFloat()
        enforceBoundaries = config.getBoolean("enforce-boundaries", true)
        boundaryPadding = config.getInt("boundary-padding", 5)
        maxHeightAboveFloor = config.getInt("max-height-above-floor", 30)
        minHeightAboveFloor = config.getInt("min-height-above-floor", 10)
        movementSpeed = config.getDouble("movement-speed", 1.0)
        dropLoot = config.getBoolean("drop-loot", false)
        alwaysDropNetherStar = config.getBoolean("always-drop-nether-star", false)
        applyWitherEffect = config.getBoolean("apply-wither-effect", true)
        witherEffectRadius = config.getInt("wither-effect-radius", 10)
        witherEffectInterval = config.getInt("wither-effect-interval", 60)
        witherEffectDuration = config.getInt("wither-effect-duration", 100)
        witherEffectAmplifier = config.getInt("wither-effect-amplifier", 1)
    }

    private fun getClosestPlayer(wither: Wither, arena: Arena): Player? {
        return arena.alive.minByOrNull { player -> wither.location.distance(player.location) }
    }

    private fun keepWitherInBounds(wither: Wither, arena: Arena) {
        val minX = min(arena.corner1.x, arena.corner2.x) + boundaryPadding
        val maxX = max(arena.corner1.x, arena.corner2.x) - boundaryPadding
        val minY = min(arena.corner1.y, arena.corner2.y)
        val minZ = min(arena.corner1.z, arena.corner2.z) + boundaryPadding
        val maxZ = max(arena.corner1.z, arena.corner2.z) - boundaryPadding

        val currentY = wither.location.y
        val floorY = minY

        val targetMinY = floorY + minHeightAboveFloor
        val targetMaxY = floorY + maxHeightAboveFloor

        val targetY =
                if (currentY > targetMaxY) {
                    targetMaxY.toDouble()
                } else if (currentY < targetMinY) {
                    targetMinY.toDouble()
                } else {
                    currentY
                }

        val currentX = wither.location.x
        val currentZ = wither.location.z

        val targetX = currentX.coerceIn(minX, maxX)
        val targetZ = currentZ.coerceIn(minZ, maxZ)

        if (currentX != targetX || currentY != targetY || currentZ != targetZ) {
            val targetLoc = Location(arena.corner1.world, targetX, targetY, targetZ)
            val direction = targetLoc.toVector().subtract(wither.location.toVector()).normalize()
            wither.velocity = direction.multiply(movementSpeed * 0.3)
        }
    }

    private fun shootSkullAtPlayer(wither: Wither, player: Player) {
        val direction = player.location.toVector().subtract(wither.location.toVector()).normalize()

        wither.world.spawn(wither.eyeLocation, WitherSkull::class.java) { skull ->
            val isCharged = shootChargedSkulls && Random.nextFloat() < chargedSkullChance
            skull.isCharged = isCharged
            skull.velocity = direction.multiply(1.5)
            skull.shooter = wither
            skull.yield = skullExplosionPower
        }
    }

    private fun shootAtRandomBlock(wither: Wither, arena: Arena) {
        val minX = min(arena.corner1.blockX, arena.corner2.blockX)
        val maxX = max(arena.corner1.blockX, arena.corner2.blockX)
        val minY = min(arena.corner1.blockY, arena.corner2.blockY)
        val maxY = max(arena.corner1.blockY, arena.corner2.blockY)
        val minZ = min(arena.corner1.blockZ, arena.corner2.blockZ)
        val maxZ = max(arena.corner1.blockZ, arena.corner2.blockZ)

        val randomX = Random.nextInt(minX, maxX + 1)
        val randomY = Random.nextInt(minY, maxY + 1)
        val randomZ = Random.nextInt(minZ, maxZ + 1)

        val targetLocation =
                Location(
                        arena.corner1.world,
                        randomX.toDouble(),
                        randomY.toDouble(),
                        randomZ.toDouble()
                )

        val direction = targetLocation.toVector().subtract(wither.location.toVector()).normalize()

        wither.world.spawn(wither.eyeLocation, WitherSkull::class.java) { skull ->
            val isCharged = shootChargedSkulls && Random.nextFloat() < chargedSkullChance
            skull.isCharged = isCharged
            skull.velocity = direction.multiply(1.5)
            skull.shooter = wither
            skull.yield = skullExplosionPower
        }
    }

    private fun applyWitherEffectToNearbyPlayers(wither: Wither, arena: Arena) {
        arena.alive.forEach { player ->
            if (wither.location.distance(player.location) <= witherEffectRadius) {
                player.addPotionEffect(
                        PotionEffect(
                                PotionEffectType.WITHER,
                                witherEffectDuration,
                                witherEffectAmplifier,
                                false,
                                true
                        )
                )
            }
        }
    }
}
