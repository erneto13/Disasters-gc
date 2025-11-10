package me.hhitt.disasters.game.timer

import com.github.shynixn.mccoroutine.bukkit.launch
import me.clip.placeholderapi.PlaceholderAPI
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.game.celebration.CelebrationManager
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitRunnable

class GameTimer(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

    private val plugin = Disasters.getInstance()
    private val celebrationManager = CelebrationManager(plugin)
    var time = 0
    var remaining = arena.maxTime
    private var nextDisasterIn = arena.rate
    private val cooldownSeconds = 10
    private var lastSoundSecond = -1

    override fun run() {
        if (time >= arena.maxTime) {
            cancel()
            return
        }

        val requiredAlive = if (arena.isTestMode) 1 else arena.aliveToEnd

        if (arena.alive.size < requiredAlive) {
            cancel()
            return
        }

        if (time < cooldownSeconds) {
            val remainingCooldown = cooldownSeconds - time
            if (remainingCooldown <= 5 && remainingCooldown != lastSoundSecond) {
                playCooldownSound(remainingCooldown)
                lastSoundSecond = remainingCooldown
            }
        }

        if (time >= cooldownSeconds) {
            val timeUntilDisaster = nextDisasterIn
            if (timeUntilDisaster <= 5 &&
                            timeUntilDisaster > 0 &&
                            timeUntilDisaster != lastSoundSecond
            ) {
                playCooldownSound(timeUntilDisaster)
                lastSoundSecond = timeUntilDisaster
            }
        }

        if (time >= cooldownSeconds && (time - cooldownSeconds) % arena.rate == 0) {
            DisasterRegistry.addRandomDisaster(arena)
            nextDisasterIn = arena.rate
            lastSoundSecond = -1
        }

        time++
        remaining--
        if (nextDisasterIn > 0) {
            nextDisasterIn--
        }
    }

    private fun playCooldownSound(secondsLeft: Int) {
        arena.playing.forEach { player ->
            when (secondsLeft) {
                5, 4, 3, 2, 1 ->
                        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
            }
        }
    }

    fun getNextDisasterIn(): Int {
        if (time < cooldownSeconds) {
            return cooldownSeconds - time
        }
        return nextDisasterIn.coerceAtLeast(0)
    }

    override fun cancel() {
        super.cancel()

        // Immediately set restarting state
        arena.state = GameState.RESTARTING

        plugin.launch {
            arena.playing.forEach { player ->
                Data.increaseTotalPlayed(player.uniqueId)
                if (!arena.alive.contains(player)) {
                    Data.increaseDefeats(player.uniqueId)
                }
                if (arena.alive.contains(player)) {
                    Data.increaseWins(player.uniqueId)
                }
            }
        }

        executeCommands()

        // Clean disasters immediately
        DisasterRegistry.removeDisasters(arena)

        Notify.gameEnd(arena)
        Notify.winners(arena)

        celebrationManager.startCelebration(arena) { completeCelebrationAndReset() }
    }

    private fun completeCelebrationAndReset() {
        // Double check disasters are removed
        DisasterRegistry.removeDisasters(arena)

        // Teleport all players before cleanup
        Lobby.teleportAtEnd(arena)

        // Cleanup entities and fluids
        arena.entityCleanupService.cleanupMeteors()
        arena.entityCleanupService.cleanupFireworks()
        arena.entityCleanupService.cleanupExtendedArea(50)

        // Reset timer variables
        time = 0
        remaining = arena.maxTime
        nextDisasterIn = arena.rate
        lastSoundSecond = -1

        // Paste arena and set state back to recruiting
        arena.resetService.paste()
    }

    private fun executeCommands() {
        arena.playing.forEach { player ->
            if (!arena.alive.contains(player)) {
                for (command in arena.losersCommands) {
                    val commandParsed = PlaceholderAPI.setPlaceholders(player, command)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandParsed)
                }
            }
            if (arena.alive.contains(player)) {
                for (command in arena.winnersCommands) {
                    val commandParsed = PlaceholderAPI.setPlaceholders(player, command)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandParsed)
                }
            }
            for (command in arena.toAllCommands) {
                val commandParsed = PlaceholderAPI.setPlaceholders(player, command)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandParsed)
            }
        }
    }
}
