package me.hhitt.disasters.game

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.game.countdown.Countdown
import me.hhitt.disasters.game.timer.GameTimer
import org.bukkit.scheduler.BukkitTask

class GameSession(private val arena: Arena) {

    private val plugin = Disasters.getInstance()
    private var countdownTask: BukkitTask? = null
    private var timerTask: BukkitTask? = null
    private var countdown: Countdown? = null
    private var gameTimer: GameTimer? = null

    fun start() {
        // Only start countdown if we're in RECRUITING state
        if (arena.state == GameState.RECRUITING) {
            startCountdown()
        }
    }

    private fun startCountdown() {
        arena.state = GameState.COUNTDOWN
        countdown = Countdown(arena, this)
        countdownTask = countdown!!.runTaskTimer(plugin, 0, 20L)
    }

    fun cancelCountdown() {
        countdown?.cancel()
        countdownTask?.cancel()
        countdownTask = null
        countdown = null
        arena.state = GameState.RECRUITING
    }

    fun startGameTimer() {
        arena.state = GameState.LIVE
        countdownTask?.cancel()
        countdownTask = null
        countdown = null
        arena.resetService.save()
        gameTimer = GameTimer(arena, this)
        timerTask = gameTimer!!.runTaskTimer(plugin, 0, 20L)
    }

    fun stop() {
        // Prevent multiple stop calls
        if (arena.state == GameState.RESTARTING) {
            return
        }

        arena.state = GameState.RESTARTING

        // Cancel tasks
        countdownTask?.cancel()
        timerTask?.cancel()
        countdownTask = null
        timerTask = null
        countdown = null
        gameTimer = null

        // Clean disasters immediately
        DisasterRegistry.removeDisasters(arena)

        // If no players remain, reset immediately
        if (arena.playing.isEmpty()) {
            arena.entityCleanupService.cleanupMeteors()
            arena.entityCleanupService.cleanupFireworks()
            arena.entityCleanupService.cleanupExtendedArea(50)
            arena.fluidCleanupService.cleanupFluids()
            arena.fluidCleanupService.cleanupExtendedArea(10)

            // Reset arena state
            arena.clear()
            arena.state = GameState.RECRUITING
        }
    }

    fun getTimeLeft(): Int = gameTimer?.remaining ?: 0
    fun getGameTime(): Int = gameTimer?.time ?: 0
    fun getCountdownTime(): Int = countdown?.time ?: 0
    fun getCountdownLeft(): Int = countdown?.remaining ?: 0
    fun getNextDisasterIn(): Int = gameTimer?.getNextDisasterIn() ?: 0
}
