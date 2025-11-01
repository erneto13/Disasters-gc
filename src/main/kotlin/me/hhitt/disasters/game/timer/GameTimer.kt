package me.hhitt.disasters.game.timer

import com.github.shynixn.mccoroutine.bukkit.launch
import me.clip.placeholderapi.PlaceholderAPI
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.disaster.impl.BlockDisappear
import me.hhitt.disasters.disaster.impl.FloorIsLava
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.game.celebration.CelebrationManager
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class GameTimer(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

    private val plugin = Disasters.getInstance()
    private val celebrationManager = CelebrationManager(plugin)
    var time = 0
    var remaining = arena.maxTime
    private var nextDisasterIn = arena.rate

    override fun run() {
        if (time >= arena.maxTime) {
            cancel()
            session.stop()
            return
        }

        val requiredAlive = if (arena.isTestMode) 1 else arena.aliveToEnd

        if (arena.alive.size < requiredAlive) {
            cancel()
            session.stop()
            return
        }

        val cooldownSeconds = 10
        if (time >= cooldownSeconds && (time - cooldownSeconds) % arena.rate == 0) {
            DisasterRegistry.addRandomDisaster(arena)
            nextDisasterIn = arena.rate
        }

        if (arena.disasters.contains(FloorIsLava())) {
            arena.alive.forEach { player ->
                DisasterRegistry.addBlockToFloorIsLava(arena, player.location)
            }
        }

        if (arena.disasters.contains(BlockDisappear())) {
            arena.alive.forEach { player ->
                DisasterRegistry.addBlockToDisappear(arena, player.location)
            }
        }

        time++
        remaining--
        nextDisasterIn--
    }

    fun getNextDisasterIn(): Int {
        return nextDisasterIn.coerceAtLeast(0)
    }

    override fun cancel() {
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

        DisasterRegistry.removeDisasters(arena)

        arena.state = GameState.RESTARTING
        super.cancel()

        Notify.gameEnd(arena)

        celebrationManager.startCelebration(arena) { completeCelebrationAndReset() }
    }

    private fun completeCelebrationAndReset() {
        DisasterRegistry.removeDisasters(arena)

        arena.entityCleanupService.cleanupMeteors()
        arena.entityCleanupService.cleanupFireworks()
        arena.entityCleanupService.cleanupExtendedArea(50)

        Lobby.teleportAtEnd(arena)

        time = 0
        remaining = arena.maxTime
        nextDisasterIn = arena.rate

        arena.state = GameState.RECRUITING

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
