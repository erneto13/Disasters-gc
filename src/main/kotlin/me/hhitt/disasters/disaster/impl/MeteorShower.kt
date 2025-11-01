import java.util.*
import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Head
import me.hhitt.disasters.util.Notify
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class MeteorShower : Disaster {
    private val arenas = mutableListOf<Arena>()
    private var spawnRate = 5
    private var intensity = 40
    private val meteorSpawnDistance = 30.0

    private val meteorHeads =
            listOf(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2IwODJhOTFjZjRkM2M2YThjNmM5YjQwNzQzZmMwNzlhY2JhYWE0YzczMDM0YTQ3Mjc0MzA4NzIyY2QxZmNiOSJ9fX0=",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQyNjAwYmE5YTk3YzNhMTk3NTljZjk5M2U5Yzk3Nzk2Y2U2OGNiNmQ0NTg3NTYxOTJkMzM1ZmZmZWQ1ZDJkZSJ9fX0=",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2I3NTliNDczNmNlOWY2NjZjNjllZTgxNzcyMTgzYTZkNGEyYTAzY2QxYWQ2N2I0NTM0Yzg4NTdjNTk2NDIxOCJ9fX0="
            )

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "meteor-shower")
    }

    override fun pulse(time: Int) {
        if (time % 60 == 0 && intensity < 40) intensity += 2
        if (time % spawnRate != 0) return

        for (arena in arenas.toList()) {
            repeat(intensity) { spawnMeteor(arena) }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        spawnRate = 8
        intensity = 10
    }

    private fun spawnMeteor(arena: Arena) {
        val world = arena.corner1.world
        val minX = minOf(arena.corner1.x, arena.corner2.x)
        val maxX = maxOf(arena.corner1.x, arena.corner2.x)
        val minZ = minOf(arena.corner1.z, arena.corner2.z)
        val maxZ = maxOf(arena.corner1.z, arena.corner2.z)
        val centerY = (arena.corner1.y + arena.corner2.y) / 2.0
        val maxY = maxOf(arena.corner1.y, arena.corner2.y) + 60

        // punto de impacto dentro del área
        val targetX = Random.nextDouble(minX, maxX)
        val targetZ = Random.nextDouble(minZ, maxZ)
        val targetY = centerY
        val targetLoc = Location(world, targetX, targetY, targetZ)

        // generar desde un lateral (más lejos)
        val spawnFromSide = Random.nextBoolean()
        val lateralOffset = meteorSpawnDistance * 2
        val spawnX =
                if (spawnFromSide) {
                    if (Random.nextBoolean()) minX - lateralOffset else maxX + lateralOffset
                } else targetX + Random.nextDouble(-lateralOffset, lateralOffset)
        val spawnZ =
                if (!spawnFromSide) {
                    if (Random.nextBoolean()) minZ - lateralOffset else maxZ + lateralOffset
                } else targetZ + Random.nextDouble(-lateralOffset, lateralOffset)

        // viene alto y en diagonal
        val spawnY = maxY + Random.nextDouble(20.0, 60.0)
        val spawnLoc = Location(world, spawnX, spawnY, spawnZ)

        // tamaño y poder
        val size = Random.nextDouble(0.8, 2.5)
        val explosionPower = size.toFloat() * 3.5f

        val headData = meteorHeads.random()
        val skull = Head.fromBase64(headData)

        val stand = world.spawn(spawnLoc, ArmorStand::class.java)
        stand.isVisible = false
        stand.isSmall = false
        stand.setGravity(false)
        stand.equipment.helmet = skull

        // dirección diagonal más lenta y más plana
        val direction = targetLoc.toVector().subtract(spawnLoc.toVector()).normalize().multiply(0.4)
        direction.y = -0.6 // fuerza hacia abajo

        fallMeteor(stand, direction, explosionPower)
    }

    private fun fallMeteor(stand: ArmorStand, direction: Vector, explosionPower: Float) {
        object : BukkitRunnable() {
                    var ticks = 0
                    override fun run() {
                        if (!stand.isValid || stand.location.y <= 0) {
                            stand.remove()
                            cancel()
                            return
                        }

                        val loc = stand.location
                        loc.add(direction)
                        stand.teleport(loc)

                        loc.world.spawnParticle(Particle.SMOKE, loc, 6, 0.4, 0.4, 0.4, 0.02)
                        loc.world.spawnParticle(Particle.FLAME, loc, 10, 0.3, 0.3, 0.3, 0.02)

                        // Detecta contacto con bloque sólido o si ya llegó al suelo
                        if (loc.block.type.isSolid || loc.y <= 2.0) {
                            loc.world.createExplosion(loc, explosionPower, true, true)
                            stand.remove()
                            cancel()
                        }

                        ticks++
                        if (ticks > 300) { // seguridad
                            stand.remove()
                            cancel()
                        }
                    }
                }
                .runTaskTimer(Disasters.getInstance(), 1L, 1L)
    }
}
