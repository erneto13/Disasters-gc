package me.hhitt.disasters.game.timer

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.game.GameSession
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitRunnable

class GameTimer(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

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

        if (arena.alive.size <= requiredAlive && arena.alive.size > 0) {
            cancel()
            return
        }

        if (arena.alive.isEmpty()) {
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
        session.stop()
    }
}
