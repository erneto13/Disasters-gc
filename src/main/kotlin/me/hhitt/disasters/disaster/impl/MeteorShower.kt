package me.hhitt.disasters.disaster.impl

import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.DisasterFileManager
import me.hhitt.disasters.util.Head
import me.hhitt.disasters.util.Notify
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
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
    private var fallSpeed = 0.4
    private var downwardForce = -0.6
    private var spawnHeightMin = 20.0
    private var spawnHeightMax = 60.0
    private var lateralOffsetMultiplier = 1.0
    private var maxLifetimeTicks = 300
    private var intensityIncreaseRate = 60
    private var intensityIncreaseAmount = 2
    private var maxIntensity = 40

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
        fallSpeed = config.getDouble("fall-speed", 0.4)
        downwardForce = config.getDouble("downward-force", -0.6)
        spawnHeightMin = config.getDouble("spawn-height-min", 20.0)
        spawnHeightMax = config.getDouble("spawn-height-max", 60.0)
        lateralOffsetMultiplier = config.getDouble("lateral-offset-multiplier", 1.0)
        maxLifetimeTicks = config.getInt("max-lifetime-ticks", 300)
        intensityIncreaseRate = config.getInt("intensity-increase-rate", 60)
        intensityIncreaseAmount = config.getInt("intensity-increase-amount", 2)
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

        val skull = Head.fromBase64(meteorHeads.random())

        val stand = world.spawn(spawnLoc, ArmorStand::class.java)
        stand.isVisible = false
        stand.isSmall = false
        stand.setGravity(false)
        stand.equipment.helmet = skull

        val direction =
                targetLoc.toVector().subtract(spawnLoc.toVector()).normalize().multiply(fallSpeed)
        direction.y = downwardForce

        debugMeteorSpawn(arena, spawnLoc, targetLoc)

        fallMeteor(stand, direction, explosionPower, arena)
    }

    private fun debugMeteorSpawn(arena: Arena, spawnLoc: Location, targetLoc: Location) {
        val world = spawnLoc.world

        // Azul = spawn, Rojo = impacto
        world.spawnParticle(
                Particle.DUST,
                spawnLoc,
                20,
                0.4,
                0.4,
                0.4,
                0.0,
                Particle.DustOptions(Color.fromRGB(64, 128, 255), 1.5f)
        )
        world.spawnParticle(
                Particle.DUST,
                targetLoc,
                20,
                0.4,
                0.4,
                0.4,
                0.0,
                Particle.DustOptions(Color.fromRGB(255, 64, 64), 1.5f)
        )

        for (player in world.players) {
            if (player.isOp || player.hasPermission("disasters.debug")) {
                player.sendMessage(
                        "§8[§cMeteorDebug§8] §7Spawn §b${spawnLoc.blockX}, ${spawnLoc.blockY}, ${spawnLoc.blockZ}§7 → Target §e${targetLoc.blockX}, ${targetLoc.blockY}, ${targetLoc.blockZ}"
                )
            }
        }
    }

    private fun fallMeteor(
            stand: ArmorStand,
            direction: Vector,
            explosionPower: Float,
            arena: Arena
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

                        if (!stand.isValid || stand.location.y <= 0) {
                            stand.remove()
                            cancel()
                            scheduledTask?.let { activeMeteorTasks.remove(it) }
                            return
                        }

                        val loc = stand.location
                        loc.add(direction)
                        stand.teleport(loc)

                        loc.world.spawnParticle(Particle.SMOKE, loc, 6, 0.4, 0.4, 0.4, 0.02)
                        loc.world.spawnParticle(Particle.FLAME, loc, 10, 0.3, 0.3, 0.3, 0.02)

                        if (loc.block.type.isSolid || loc.y <= 2.0) {
                            if (arenas.contains(arena))
                                    loc.world.createExplosion(loc, explosionPower, true, true)
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

        val scheduled = task.runTaskTimer(Disasters.getInstance(), 1L, 1L)
        scheduledTask = scheduled
        activeMeteorTasks.add(scheduled)
    }
}
