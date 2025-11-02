package me.hhitt.disasters.util

import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object PS {

    // generic particle spawner
    fun spawnParticles(
            location: Location,
            particle: Particle,
            count: Int = 1,
            offsetX: Double = 0.0,
            offsetY: Double = 0.0,
            offsetZ: Double = 0.0,
            speed: Double = 0.0,
            data: Any? = null
    ) {
        if (data != null) {
            location.world.spawnParticle(
                    particle,
                    location,
                    count,
                    offsetX,
                    offsetY,
                    offsetZ,
                    speed,
                    data
            )
        } else {
            location.world.spawnParticle(
                    particle,
                    location,
                    count,
                    offsetX,
                    offsetY,
                    offsetZ,
                    speed
            )
        }
    }

    // generic sound player
    fun playSound(location: Location, sound: Sound, volume: Float = 1.0f, pitch: Float = 1.0f) {
        location.world.playSound(location, sound, volume, pitch)
    }

    fun playSound(player: Player, sound: Sound, volume: Float = 1.0f, pitch: Float = 1.0f) {
        player.playSound(player.location, sound, volume, pitch)
    }

    // multi-particle effect (for complex effects)
    fun spawnMultiParticle(location: Location, effects: List<ParticleEffect>) {
        effects.forEach { effect ->
            spawnParticles(
                    location.clone().add(effect.offsetLocation),
                    effect.particle,
                    effect.count,
                    effect.spreadX,
                    effect.spreadY,
                    effect.spreadZ,
                    effect.speed,
                    effect.data
            )
        }
    }

    // multi-sound effect
    fun playSoundSet(location: Location, sounds: List<SoundEffect>) {
        sounds.forEach { sound -> playSound(location, sound.sound, sound.volume, sound.pitch) }
    }

    // helper: circular particle ring
    fun spawnCircularParticles(
            center: Location,
            particle: Particle,
            radius: Double,
            points: Int = 32,
            color: Color? = null,
            size: Float = 1.0f
    ) {
        val world = center.world

        for (i in 0 until points) {
            val angle = (2 * Math.PI * i) / points
            val x = center.x + radius * cos(angle)
            val z = center.z + radius * sin(angle)
            val y = center.y

            val data =
                    if (color != null && particle == Particle.DUST) {
                        Particle.DustOptions(color, size)
                    } else null

            spawnParticles(Location(world, x, y, z), particle, 1, 0.0, 0.0, 0.0, 0.0, data)
        }
    }

    // helper: line of particles
    fun spawnParticleLine(
            start: Location,
            end: Location,
            particle: Particle,
            spacing: Double = 0.5,
            color: Color? = null,
            size: Float = 1.0f
    ) {
        val direction = end.toVector().subtract(start.toVector())
        val distance = direction.length()
        direction.normalize()

        val points = (distance / spacing).toInt()

        for (i in 0..points) {
            val point = start.clone().add(direction.clone().multiply(i * spacing))

            val data =
                    if (color != null && particle == Particle.DUST) {
                        Particle.DustOptions(color, size)
                    } else null

            spawnParticles(point, particle, 1, 0.0, 0.0, 0.0, 0.0, data)
        }
    }

    data class ParticleEffect(
            val particle: Particle,
            val count: Int = 1,
            val spreadX: Double = 0.0,
            val spreadY: Double = 0.0,
            val spreadZ: Double = 0.0,
            val speed: Double = 0.0,
            val data: Any? = null,
            val offsetLocation: Vector = Vector(0.0, 0.0, 0.0)
    )

    data class SoundEffect(val sound: Sound, val volume: Float = 1.0f, val pitch: Float = 1.0f)
}
