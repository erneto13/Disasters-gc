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

    fun start() {
        if (arena.state == GameState.RECRUITING) {
            stop()
            startCountdown()
        }
    }

    private fun startCountdown() {
        countdownTask = Countdown(arena, this).runTaskTimer(plugin, 0, 20L)
    }

    fun startGameTimer() {
        arena.state = GameState.LIVE
        countdownTask?.cancel()
        countdownTask = null
        arena.resetService.save()
        timerTask = GameTimer(arena, this).runTaskTimer(plugin, 0, 20L)
    }

    fun stop() {
        countdownTask?.cancel()
        timerTask?.cancel()
        countdownTask = null
        timerTask = null
        arena.state = GameState.RECRUITING
    }


    fun getTimeLeft(): Int {
        return if (countdownTask != null) {
            (countdownTask as Countdown).remaining
        } else {
            0
        }
    }

    fun getGameTime(): Int {
        return if (timerTask != null) {
            (timerTask as GameTimer).time
        } else {
            0
        }
    }

    fun getCountdownTime(): Int {
        return if (countdownTask != null) {
            (countdownTask as Countdown).time
        } else {
            0
        }
    }

    fun getCountdownLeft(): Int {
        return if (countdownTask != null) {
            (countdownTask as Countdown).remaining
        } else {
            0
        }
    }
}

