package me.hhitt.disasters.debris

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.storage.file.DisasterFileManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class DebrisManager(private val plugin: Disasters) {

    private var enabled = true
    private var maxDebrisPerExplosion = 15
    private var explosionForceMultiplier = 1.0f
    private var randomVelocityVariation = true
    private var velocityRandomness = 0.3
    private var individualBlockForce = 1.5f
    private var upwardBias = 0.6
    private var hurtEntities = true
    private var enableGravity = true
    private var maxLifetimeSeconds = 10
    private var placeOnLanding = true
    private val blacklistedMaterials = mutableSetOf<Material>()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val config = DisasterFileManager.getDisasterConfig("debris")

        if (config == null) {
            plugin.logger.warning("Debris config not found! Using default values.")
            return
        }

        enabled = config.getBoolean("enabled", true)
        maxDebrisPerExplosion = config.getInt("max-debris-per-explosion", 15)
        explosionForceMultiplier = config.getDouble("explosion-force-multiplier", 1.0).toFloat()
        randomVelocityVariation = config.getBoolean("random-velocity-variation", true)
        velocityRandomness = config.getDouble("velocity-randomness", 0.3)
        individualBlockForce = config.getDouble("individual-block-force", 1.5).toFloat()
        upwardBias = config.getDouble("upward-bias", 0.6)
        hurtEntities = config.getBoolean("hurt-entities", true)
        enableGravity = config.getBoolean("enable-gravity", true)
        maxLifetimeSeconds = config.getInt("max-lifetime-seconds", 10)
        placeOnLanding = config.getBoolean("place-on-landing", true)

        blacklistedMaterials.clear()
        config.getStringList("blacklisted-materials").forEach { materialName ->
            try {
                blacklistedMaterials.add(Material.valueOf(materialName.uppercase()))
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Invalid material in debris blacklist: $materialName")
            }
        }
    }

    fun createDebrisFromExplosion(center: Location, blocks: List<Block>, force: Float) {
        if (!enabled || blocks.isEmpty()) return

        val maxDebris = maxDebrisPerExplosion
        val debrisBlocks =
                if (blocks.size > maxDebris) {
                    blocks.shuffled().take(maxDebris)
                } else {
                    blocks
                }

        debrisBlocks.forEach { block ->
            createFlyingDebris(center, block.location, block.type, force)
        }
    }

    private fun createFlyingDebris(
            explosionCenter: Location,
            blockLocation: Location,
            material: Material,
            force: Float
    ) {
        if (material == Material.AIR ||
                        material == Material.CAVE_AIR ||
                        material == Material.VOID_AIR ||
                        material == Material.WATER ||
                        material == Material.LAVA ||
                        blacklistedMaterials.contains(material)
        ) {
            return
        }

        val world = blockLocation.world ?: return

        val direction = blockLocation.toVector().subtract(explosionCenter.toVector())

        val distance = direction.length()

        if (distance < 0.1) {
            val angle = Random.nextDouble() * 2 * Math.PI
            val pitch = Random.nextDouble() * Math.PI / 4
            direction.x = cos(angle) * cos(pitch)
            direction.y = sin(pitch) + upwardBias
            direction.z = sin(angle) * cos(pitch)
        } else {
            direction.normalize()
        }

        if (randomVelocityVariation) {
            val randomX = Random.nextDouble(-velocityRandomness, velocityRandomness)
            val randomY = Random.nextDouble(0.1, velocityRandomness + upwardBias)
            val randomZ = Random.nextDouble(-velocityRandomness, velocityRandomness)
            direction.add(Vector(randomX, randomY, randomZ))
        }

        if (!direction.isFinite()) {
            plugin.logger.warning("Invalid direction calculated for debris, skipping")
            return
        }

        direction.normalize()

        val velocityMultiplier =
                (force * explosionForceMultiplier / 4.0) *
                        (1.0 - (distance / 20.0).coerceIn(0.0, 0.8))
        val velocity = direction.multiply(velocityMultiplier.coerceIn(0.3, 2.0))

        if (!velocity.isFinite()) {
            plugin.logger.warning("Invalid velocity calculated for debris, using default")
            velocity.x = 0.0
            velocity.y = 1.0
            velocity.z = 0.0
        }

        val spawnLocation = blockLocation.clone().add(0.5, 0.5, 0.5)

        try {
            val fallingBlock =
                    world.spawn(spawnLocation, FallingBlock::class.java) { fb ->
                        fb.blockData = material.createBlockData()
                    }

            fallingBlock.dropItem = false
            fallingBlock.setHurtEntities(hurtEntities)
            fallingBlock.velocity = velocity
            fallingBlock.setGravity(enableGravity)

            object : BukkitRunnable() {
                        var ticksAlive = 0
                        val maxTicks = maxLifetimeSeconds * 20

                        override fun run() {
                            ticksAlive++

                            if (!fallingBlock.isValid || ticksAlive > maxTicks) {
                                fallingBlock.remove()
                                cancel()
                            }
                        }
                    }
                    .runTaskTimer(plugin, 0L, 1L)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to spawn debris: ${e.message}")
        }
    }

    fun createDebrisFromBlock(blockLocation: Location, material: Material, force: Float = 1.5f) {
        if (material == Material.AIR ||
                        material == Material.CAVE_AIR ||
                        material == Material.VOID_AIR ||
                        blacklistedMaterials.contains(material)
        ) {
            return
        }

        val world = blockLocation.world ?: return

        val angle = Random.nextDouble(0.0, 2 * Math.PI)
        val upwardForce = Random.nextDouble(0.5, 1.0)

        val direction = Vector(cos(angle) * 0.5, upwardForce, sin(angle) * 0.5).normalize()
        val velocity = direction.multiply(force * Random.nextDouble(0.8, 1.2))

        val spawnLocation = blockLocation.clone().add(0.5, 0.5, 0.5)

        try {
            val fallingBlock =
                    world.spawn(spawnLocation, FallingBlock::class.java) { fb ->
                        fb.blockData = material.createBlockData()
                    }

            fallingBlock.dropItem = false
            fallingBlock.setHurtEntities(hurtEntities)
            fallingBlock.velocity = velocity
            fallingBlock.setGravity(enableGravity)

            object : BukkitRunnable() {
                        var ticksAlive = 0

                        override fun run() {
                            ticksAlive++
                            if (!fallingBlock.isValid || ticksAlive > 200) {
                                fallingBlock.remove()
                                cancel()
                            }
                        }
                    }
                    .runTaskTimer(plugin, 0L, 1L)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to spawn debris from block: ${e.message}")
        }
    }

    fun createDebrisBurst(
            center: Location,
            material: Material,
            count: Int = 10,
            force: Float = 2.0f
    ) {
        repeat(count) { createDebrisFromBlock(center, material, force) }
    }

    private fun Vector.isFinite(): Boolean {
        return x.isFinite() && y.isFinite() && z.isFinite()
    }
}
