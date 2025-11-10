package me.hhitt.disasters.game.countdown

import kotlin.random.Random
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.util.Notify
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

/**
 * The Countdown class is responsible for managing the countdown timer before the game starts. It
 * handles the countdown time, remaining time, and notifies players about the countdown status.
 *
 * @param arena The arena where the countdown is taking place.
 * @param session The game session associated with the arena.
 */
class Countdown(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

    var time = 0
    var remaining = arena.countdown

    override fun run() {
        if (time >= arena.countdown) {
            if (time >= (arena.countdown + 2)) {
                // Teleport all players to spawn with random offsets before starting
                teleportPlayersToSpawn()

                Notify.gameStart(arena)
                cancel()
                session.startGameTimer()
            }
            time++
            return
        }

        val requiredAlive = if (arena.isTestMode) 1 else arena.minPlayers

        if (arena.alive.size < requiredAlive) {
            cancel()
            return
        }

        Notify.countdown(arena, remaining)
        time++
        remaining--
    }

    override fun cancel() {
        super.cancel()
        Notify.countdownCanceled(arena)
        time = 0
        remaining = arena.countdown
    }

    /**
     * Teleports all players to the spawn location with random offsets to spread them out around the
     * spawn point.
     */
    private fun teleportPlayersToSpawn() {
        val baseSpawn = arena.location.clone()

        arena.playing.forEachIndexed { index, player ->
            val spawnLocation =
                    if (index == 0) {
                        // First player goes to exact spawn
                        baseSpawn.clone()
                    } else {
                        // Other players get random offsets within 3 blocks
                        getRandomSpawnLocation(baseSpawn)
                    }

            player.teleport(spawnLocation)
        }
    }

    /**
     * Generates a random spawn location within 3 blocks of the base spawn.
     *
     * @param baseSpawn The original spawn location
     * @return A new location with random offsets
     */
    private fun getRandomSpawnLocation(baseSpawn: Location): Location {
        val offsetX = Random.nextDouble(-3.0, 3.0)
        val offsetY = Random.nextDouble(-3.0, 3.0)
        val offsetZ = Random.nextDouble(-3.0, 3.0)

        return baseSpawn.clone().add(offsetX, offsetY, offsetZ)
    }
}
