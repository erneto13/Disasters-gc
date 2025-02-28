package me.hhitt.disasters.game.countdown

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.util.Notify
import org.bukkit.scheduler.BukkitRunnable

class Countdown(private val arena: Arena): BukkitRunnable() {

    private var time = 0
    private var remaining = arena.countdown
    private val maxTime = arena.countdown

    fun start() {
        this.run()
        arena.state = GameState.COUNTDOWN
    }

    override fun cancel() {
        this.cancel()
        Notify.countdownCanceled(arena)
        time = 0
        remaining = arena.countdown
        arena.state = GameState.RESTARTING
    }

    override fun run() {

        if(time >= maxTime) {
            arena.start()
            cancel()
            return
        }

        if(arena.alive.size <= arena.aliveToEnd) {
            cancel()
            return
        }

        Notify.countdown(arena, remaining)
        time++
        remaining--
    }


}