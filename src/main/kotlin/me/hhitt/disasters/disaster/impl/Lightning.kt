package me.hhitt.disasters.disaster.impl

import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.DisasterFileManager
import me.hhitt.disasters.util.Notify

class Lightning : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val random = Random
    private val debrisManager = Disasters.getInstance().getDebrisManager()

    private var strikeInterval = 3
    private var radius = 5
    private var explosionPower = 3.0f
    private var explosionBreaksBlocks = true
    private var explosionSetsFire = false
    private var damageMultiplier = 1.0
    private var targetRandomPlayers = true

    override fun start(arena: Arena) {
        loadConfig()
        arenas.add(arena)
        Notify.disaster(arena, "lightning")
    }

    override fun pulse(time: Int) {
        if (time % strikeInterval != 0) return

        arenas.toList().forEach { arena ->
            if (arena.alive.isEmpty()) return@forEach

            val target =
                    if (targetRandomPlayers) {
                        arena.alive.random()
                    } else {
                        arena.alive.first()
                    }

            val location = target.location

            val offsetX = (random.nextDouble() - 0.5) * 2 * radius
            val offsetZ = (random.nextDouble() - 0.5) * 2 * radius

            val strikeLocation = location.clone().add(offsetX, 0.0, offsetZ)
            val highestBlockY =
                    strikeLocation.world?.getHighestBlockYAt(strikeLocation)?.toDouble()
                            ?: strikeLocation.y
            strikeLocation.y = highestBlockY

            strikeLocation.world?.strikeLightning(strikeLocation)

            strikeLocation.world?.createExplosion(
                    strikeLocation,
                    explosionPower,
                    explosionSetsFire,
                    explosionBreaksBlocks
            )

            if (damageMultiplier > 0) {
                arena.alive.forEach { player ->
                    val distance = player.location.distance(strikeLocation)
                    if (distance <= radius) {
                        val damage = ((radius - distance) / radius) * 4.0 * damageMultiplier
                        player.damage(damage.coerceAtLeast(0.5))
                    }
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }

    private fun loadConfig() {
        val config = DisasterFileManager.getDisasterConfig("lightning")

        if (config == null) {
            Disasters.getInstance()
                    .logger
                    .warning("Lightning config not found! Using default values.")
            return
        }

        strikeInterval = config.getInt("strike-interval", 3)
        radius = config.getInt("radius", 5)
        explosionPower = config.getDouble("explosion-power", 3.0).toFloat()
        explosionBreaksBlocks = config.getBoolean("explosion-breaks-blocks", true)
        explosionSetsFire = config.getBoolean("explosion-sets-fire", false)
        damageMultiplier = config.getDouble("damage-multiplier", 1.0)
        targetRandomPlayers = config.getBoolean("target-random-players", true)
    }
}
