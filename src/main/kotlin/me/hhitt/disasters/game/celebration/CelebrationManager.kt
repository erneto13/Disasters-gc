package me.hhitt.disasters.game.celebration

import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.util.Msg
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.scheduler.BukkitRunnable

class CelebrationManager(private val plugin: Disasters) {

    companion object {
        private const val CELEBRATION_DURATION_SECONDS = 10
        private const val FIREWORKS_PER_SECOND = 3
    }

    fun startCelebration(arena: Arena, onComplete: () -> Unit) {
        arena.playing.forEach { player ->
            Msg.send(player, "celebration.start")
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
        }

        val fireworkTask = FireworkTask(arena)
        fireworkTask.runTaskTimer(plugin, 0L, 20L)

        Bukkit.getScheduler()
                .runTaskLater(
                        plugin,
                        Runnable {
                            fireworkTask.cancel()

                            arena.playing.forEach { player ->
                                Msg.send(player, "celebration.end")
                                player.playSound(
                                        player.location,
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        1.0f,
                                        1.0f
                                )
                            }

                            onComplete()
                        },
                        (CELEBRATION_DURATION_SECONDS * 20L)
                )
    }

    private inner class FireworkTask(private val arena: Arena) : BukkitRunnable() {
        private var ticksElapsed = 0

        override fun run() {
            if (ticksElapsed >= CELEBRATION_DURATION_SECONDS) {
                cancel()
                return
            }

            repeat(FIREWORKS_PER_SECOND) { spawnRandomFirework(arena) }

            ticksElapsed++
        }
    }

    private fun spawnRandomFirework(arena: Arena) {
        val world = arena.location.world
        val minX = minOf(arena.corner1.x, arena.corner2.x)
        val maxX = maxOf(arena.corner1.x, arena.corner2.x)
        val minZ = minOf(arena.corner1.z, arena.corner2.z)
        val maxZ = maxOf(arena.corner1.z, arena.corner2.z)
        val maxY = maxOf(arena.corner1.y, arena.corner2.y)

        val x = Random.nextDouble(minX, maxX)
        val z = Random.nextDouble(minZ, maxZ)
        val location = Location(world, x, maxY + 5, z)

        val firework = world.spawn(location, Firework::class.java)
        val meta = firework.fireworkMeta

        val effect =
                FireworkEffect.builder()
                        .with(getRandomFireworkType())
                        .withColor(getRandomColor())
                        .withFade(getRandomColor())
                        .flicker(Random.nextBoolean())
                        .trail(Random.nextBoolean())
                        .build()

        meta.addEffect(effect)
        meta.power = Random.nextInt(1, 3)
        firework.fireworkMeta = meta
    }

    private fun getRandomFireworkType(): FireworkEffect.Type {
        return FireworkEffect.Type.values().random()
    }

    private fun getRandomColor(): Color {
        return when (Random.nextInt(10)) {
            0 -> Color.RED
            1 -> Color.BLUE
            2 -> Color.GREEN
            3 -> Color.YELLOW
            4 -> Color.ORANGE
            5 -> Color.PURPLE
            6 -> Color.WHITE
            7 -> Color.AQUA
            8 -> Color.FUCHSIA
            else -> Color.LIME
        }
    }
}
