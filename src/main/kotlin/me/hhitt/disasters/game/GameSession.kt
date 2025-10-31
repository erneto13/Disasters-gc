package me.hhitt.disasters.game

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
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
        if (arena.state == GameState.RECRUITING) {
            stop()
            startCountdown()
        }
    }

    private fun startCountdown() {
        countdown = Countdown(arena, this)
        countdownTask = countdown!!.runTaskTimer(plugin, 0, 20L)
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
        countdownTask?.cancel()
        timerTask?.cancel()
        countdownTask = null
        timerTask = null
        countdown = null
        gameTimer = null
        arena.state = GameState.RECRUITING
    }

    fun getTimeLeft(): Int = gameTimer?.remaining ?: 0
    fun getGameTime(): Int = gameTimer?.time ?: 0
    fun getCountdownTime(): Int = countdown?.time ?: 0
    fun getCountdownLeft(): Int = countdown?.remaining ?: 0
    fun getNextDisasterIn(): Int = gameTimer?.getNextDisasterIn() ?: 0  
}