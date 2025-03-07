package me.hhitt.disasters.game.countdown

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.util.Notify
import org.bukkit.scheduler.BukkitRunnable

class Countdown(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

    var time = 0
    var remaining = arena.countdown

    override fun run() {
        if (time >= arena.countdown) {
            if(time >= (arena.countdown + 2)) {
                Notify.gameStart(arena)
                cancel()
                session.startGameTimer()
            }
            time++
            return
        }

        if (arena.alive.size <= arena.aliveToEnd) {
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
}
