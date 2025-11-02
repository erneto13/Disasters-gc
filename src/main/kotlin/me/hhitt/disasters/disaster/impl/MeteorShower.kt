package me.hhitt.disasters.disaster.impl

import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.DisasterFileManager
import me.hhitt.disasters.util.Head
import me.hhitt.disasters.util.Notify
import me.hhitt.disasters.util.PS
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

class MeteorShower : Disaster {
    private val arenas = mutableListOf<Arena>()
    private val activeMeteorTasks = mutableListOf<BukkitTask>()

    private var spawnRate = 5
    private var intensity = 40
    private var meteorSpawnDistance = 30.0
    private var minSize = 0.8
    private var maxSize = 2.5
    private var explosionMultiplier = 3.5f
    private var fallSpeed = 0.8
    private var spawnHeightMin = 20.0
    private var spawnHeightMax = 60.0
    private var lateralOffsetMultiplier = 1.0
    private var maxLifetimeTicks = 300
    private var intensityIncreaseRate = 60
    private var intensityIncreaseAmount = 2
    private var maxIntensity = 40
    private var enableParticles = true
    private var enableSounds = true
    private var enableImpactWave = true

    private val meteorHeads =
            listOf(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2IwODJhOTFjZjRkM2M2YThjNmM5YjQwNzQzZmMwNzlhY2JhYWE0YzczMDM0YTQ3Mjc0MzA4NzIyY2QxZmNiOSJ9fX0="
            )

    override fun start(arena: Arena) {
        loadConfig()
        arenas.add(arena)
        Notify.disaster(arena, "meteor-shower")
    }

    override fun pulse(time: Int) {
        if (time % intensityIncreaseRate == 0 && intensity < maxIntensity) {
            intensity += intensityIncreaseAmount
        }
        if (time % spawnRate != 0) return

        for (arena in arenas.toList()) {
            repeat(intensity) { spawnMeteor(arena) }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        activeMeteorTasks.forEach { if (!it.isCancelled) it.cancel() }
        activeMeteorTasks.clear()
        arena.entityCleanupService.cleanupMeteors()

        val config = DisasterFileManager.getDisasterConfig("meteor-shower")
        intensity = config?.getInt("initial-intensity") ?: 40

        Disasters.getInstance()
                .logger
                .info("MeteorShower stopped and cleaned up for arena: ${arena.name}")
    }

    private fun loadConfig() {
        val config =
                DisasterFileManager.getDisasterConfig("meteor-shower")
                        ?: run {
                            Disasters.getInstance()
                                    .logger
                                    .warning("MeteorShower config not found! Using default values.")
                            return
                        }

        spawnRate = config.getInt("spawn-rate", 5)
        intensity = config.getInt("initial-intensity", 40)
        maxIntensity = config.getInt("max-intensity", 40)
        meteorSpawnDistance = config.getDouble("spawn-distance", 30.0)
        minSize = config.getDouble("min-size", 0.8)
        maxSize = config.getDouble("max-size", 2.5)
        explosionMultiplier = config.getDouble("explosion-multiplier", 3.5).toFloat()
        fallSpeed = config.getDouble("fall-speed", 0.8)
        spawnHeightMin = config.getDouble("spawn-height-min", 20.0)
        spawnHeightMax = config.getDouble("spawn-height-max", 60.0)
        lateralOffsetMultiplier = config.getDouble("lateral-offset-multiplier", 1.0)
        maxLifetimeTicks = config.getInt("max-lifetime-ticks", 300)
        intensityIncreaseRate = config.getInt("intensity-increase-rate", 60)
        intensityIncreaseAmount = config.getInt("intensity-increase-amount", 2)
        enableParticles = config.getBoolean("enable-particles", true)
        enableSounds = config.getBoolean("enable-sounds", true)
        enableImpactWave = config.getBoolean("enable-impact-wave", true)
    }

    private fun spawnMeteor(arena: Arena) {
        val world = arena.corner1.world
        val minX = minOf(arena.corner1.x, arena.corner2.x)
        val maxX = maxOf(arena.corner1.x, arena.corner2.x)
        val minZ = minOf(arena.corner1.z, arena.corner2.z)
        val maxZ = maxOf(arena.corner1.z, arena.corner2.z)
        val maxY = maxOf(arena.corner1.y, arena.corner2.y)

        val centerX = (arena.corner1.x + arena.corner2.x) / 2.0
        val centerZ = (arena.corner1.z + arena.corner2.z) / 2.0

        val radiusX = (maxX - minX) * 0.35
        val radiusZ = (maxZ - minZ) * 0.35
        val targetX = centerX + Random.nextDouble(-radiusX, radiusX)
        val targetZ = centerZ + Random.nextDouble(-radiusZ, radiusZ)

        var targetY = maxY
        while (targetY > 0 &&
                !world.getBlockAt(targetX.toInt(), targetY.toInt(), targetZ.toInt()).type.isSolid) {
            targetY--
        }

        val targetLoc = Location(world, targetX, targetY + 1, targetZ)

        val spawnFromSide = Random.nextBoolean()
        val lateralOffset = meteorSpawnDistance * lateralOffsetMultiplier
        val spawnX =
                if (spawnFromSide)
                        if (Random.nextBoolean()) minX - lateralOffset else maxX + lateralOffset
                else targetX + Random.nextDouble(-lateralOffset, lateralOffset)
        val spawnZ =
                if (!spawnFromSide)
                        if (Random.nextBoolean()) minZ - lateralOffset else maxZ + lateralOffset
                else targetZ + Random.nextDouble(-lateralOffset, lateralOffset)

        val spawnY = maxY + Random.nextDouble(spawnHeightMin, spawnHeightMax)
        val spawnLoc = Location(world, spawnX, spawnY, spawnZ)

        val size = Random.nextDouble(minSize, maxSize)
        val explosionPower = size.toFloat() * explosionMultiplier

        val stand = world.spawn(spawnLoc, ArmorStand::class.java)
        stand.isVisible = false
        stand.isSmall = size < 1.5
        stand.setGravity(false)
        stand.isMarker = true

        val skull = Head.fromBase64(meteorHeads.random())
        stand.equipment.helmet = skull

        stand.headPose =
                EulerAngle(
                        Random.nextDouble() * Math.PI,
                        Random.nextDouble() * Math.PI,
                        Random.nextDouble() * Math.PI
                )

        val direction =
                targetLoc.toVector().subtract(spawnLoc.toVector()).normalize().multiply(fallSpeed)

        if (enableSounds) {
            PS.playSound(spawnLoc, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.8f)
        }

        fallMeteor(stand, direction, explosionPower, arena, size)
    }

    private fun fallMeteor(
            stand: ArmorStand,
            direction: Vector,
            explosionPower: Float,
            arena: Arena,
            size: Double
    ) {
        var scheduledTask: BukkitTask? = null

        val task =
                object : BukkitRunnable() {
                    var ticks = 0
                    override fun run() {
                        if (!arenas.contains(arena)) {
                            stand.remove()
                            cancel()
                            scheduledTask?.let { activeMeteorTasks.remove(it) }
                            return
                        }

                        if (!stand.isValid) {
                            cancel()
                            scheduledTask?.let { activeMeteorTasks.remove(it) }
                            return
                        }

                        val loc = stand.location
                        val newLoc = loc.clone().add(direction)
                        stand.teleport(newLoc)

                        val currentPose = stand.headPose
                        stand.headPose =
                                EulerAngle(
                                        currentPose.x + 0.3,
                                        currentPose.y + 0.2,
                                        currentPose.z + 0.15
                                )

                        if (enableParticles) {
                            val trailLoc =
                                    newLoc.clone()
                                            .add(0.0, 0.5 + if (stand.isSmall) 0.25 else 0.0, 0.0)
                            spawnMeteorTrail(trailLoc, size)
                        }

                        if (newLoc.block.type.isSolid || newLoc.y <= 2.0) {
                            if (arenas.contains(arena)) {
                                newLoc.world.createExplosion(newLoc, explosionPower, false, true)

                                if (enableParticles) {
                                    spawnExplosionEffect(newLoc, size)
                                }

                                if (enableSounds) {
                                    PS.playSound(
                                            newLoc,
                                            Sound.ENTITY_GENERIC_EXPLODE,
                                            explosionPower * 0.5f,
                                            0.8f
                                    )
                                }

                                if (enableImpactWave) {
                                    spawnImpactWave(newLoc, size)
                                }
                            }
                            stand.remove()
                            cancel()
                            scheduledTask?.let { activeMeteorTasks.remove(it) }
                        }

                        ticks++
                        if (ticks > maxLifetimeTicks) {
                            stand.remove()
                            cancel()
                            scheduledTask?.let { activeMeteorTasks.remove(it) }
                        }
                    }

                    override fun cancel() {
                        super.cancel()
                        scheduledTask?.let { activeMeteorTasks.remove(it) }
                    }
                }

        val scheduled = task.runTaskTimer(Disasters.getInstance(), 0L, 1L)
        scheduledTask = scheduled
        activeMeteorTasks.add(scheduled)
    }

    private fun spawnMeteorTrail(location: Location, size: Double) {
        val particleCount = (8 * size).toInt()

        PS.spawnParticles(location, Particle.FLAME, particleCount, 0.2, 0.2, 0.2, 0.02)

        PS.spawnParticles(
                location,
                Particle.SMOKE,
                (particleCount * 0.3).toInt(),
                0.15,
                0.15,
                0.15,
                0.01
        )
    }

    private fun spawnExplosionEffect(location: Location, size: Double) {
        val particleCount = (30 * size).toInt()

        PS.spawnParticles(location, Particle.EXPLOSION, particleCount, 1.0, 1.0, 1.0, 0.0)
        PS.spawnParticles(location, Particle.FLAME, particleCount, 1.0, 1.0, 1.0, 0.1)
        PS.spawnParticles(location, Particle.SMOKE, particleCount / 2, 1.5, 1.5, 1.5, 0.05)
    }

    private fun spawnImpactWave(location: Location, size: Double) {
        PS.spawnCircularParticles(
                location,
                Particle.DUST,
                size * 2,
                (size * 15).toInt(),
                Color.fromRGB(255, 100, 0),
                1.5f
        )
    }
}
