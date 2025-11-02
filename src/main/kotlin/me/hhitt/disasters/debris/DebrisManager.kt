package me.hhitt.disasters.debris

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import me.hhitt.disasters.Disasters
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
        val config = me.hhitt.disasters.storage.file.DisasterFileManager.getDisasterConfig("debris")

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
        if (blocks.isEmpty()) return

        val maxDebris = 15
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
                        material == Material.VOID_AIR
        ) {
            return
        }

        if (material == Material.WATER || material == Material.LAVA) {
            return
        }

        val world = blockLocation.world ?: return

        val direction = blockLocation.toVector().subtract(explosionCenter.toVector()).normalize()

        val randomX = Random.nextDouble(-0.3, 0.3)
        val randomY = Random.nextDouble(0.1, 0.5)
        val randomZ = Random.nextDouble(-0.3, 0.3)

        direction.add(Vector(randomX, randomY, randomZ)).normalize()

        val distance = blockLocation.distance(explosionCenter)
        val velocityMultiplier = (force / 4.0) * (1.0 - (distance / 20.0).coerceIn(0.0, 0.8))
        val velocity = direction.multiply(velocityMultiplier.coerceIn(0.3, 2.0))

        val spawnLocation = blockLocation.clone().add(0.5, 0.5, 0.5)

        val fallingBlock =
                world.spawn(spawnLocation, FallingBlock::class.java) { fb ->
                    fb.blockData = material.createBlockData()
                }

        fallingBlock.dropItem = false
        fallingBlock.setHurtEntities(true)
        fallingBlock.velocity = velocity

        fallingBlock.setGravity(true)

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
    }

    fun createDebrisFromBlock(blockLocation: Location, material: Material, force: Float = 1.5f) {
        if (material == Material.AIR ||
                        material == Material.CAVE_AIR ||
                        material == Material.VOID_AIR
        ) {
            return
        }

        val world = blockLocation.world ?: return

        val angle = Random.nextDouble(0.0, 2 * Math.PI)
        val upwardForce = Random.nextDouble(0.5, 1.0)

        val direction = Vector(cos(angle) * 0.5, upwardForce, sin(angle) * 0.5).normalize()

        val velocity = direction.multiply(force * Random.nextDouble(0.8, 1.2))

        val spawnLocation = blockLocation.clone().add(0.5, 0.5, 0.5)
        val fallingBlock =
                world.spawn(spawnLocation, FallingBlock::class.java) { fb ->
                    fb.blockData = material.createBlockData()
                }

        fallingBlock.dropItem = false
        fallingBlock.setHurtEntities(true)
        fallingBlock.velocity = velocity
        fallingBlock.setGravity(true)

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
    }

    fun createDebrisBurst(
            center: Location,
            material: Material,
            count: Int = 10,
            force: Float = 2.0f
    ) {
        repeat(count) { createDebrisFromBlock(center, material, force) }
    }
}
