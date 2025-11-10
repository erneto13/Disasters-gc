package me.hhitt.disasters.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.game.celebration.CelebrationManager
import me.hhitt.disasters.game.countdown.Countdown
import me.hhitt.disasters.game.timer.GameTimer
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Notify
import org.bukkit.scheduler.BukkitTask

class GameSession(private val arena: Arena) {

    private val plugin = Disasters.getInstance()
    private var countdownTask: BukkitTask? = null
    private var timerTask: BukkitTask? = null
    private var countdown: Countdown? = null
    private var gameTimer: GameTimer? = null

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        // solo iniciar countdown si estamos en estado recruiting
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
        // prevenir multiples llamadas a stop
        if (arena.state == GameState.RESTARTING) {
            return
        }

        arena.state = GameState.RESTARTING

        // cancelar tareas
        countdownTask?.cancel()
        timerTask?.cancel()
        countdownTask = null
        timerTask = null
        countdown = null
        gameTimer = null

        DisasterRegistry.removeDisasters(arena)

        val hasPlayers = arena.playing.isNotEmpty()

        if (!hasPlayers) {
            plugin.logger.info(
                    "Arena ${arena.name} is empty, skipping celebration and resetting immediately"
            )
            performImmediateReset()
        } else {
            // jugadores presentes - final normal con celebracion
            performNormalEnd()
        }
    }

    private fun performImmediateReset() {
        // limpieza inmediata de entidades y fluidos
        arena.entityCleanupService.cleanupMeteors()
        arena.entityCleanupService.cleanupFireworks()
        arena.entityCleanupService.cleanupExtendedArea(50)
        arena.fluidCleanupService.cleanupFluids()
        arena.fluidCleanupService.cleanupExtendedArea(10)

        arena.clear()

        org.bukkit.Bukkit.getScheduler().runTask(plugin, Runnable {
            arena.resetService.paste()
            plugin.logger.info("Arena ${arena.name} reset completed (empty arena)")
        })
    }

    private fun performNormalEnd() {
        val celebrationManager = CelebrationManager(plugin)

        // notificar jugadores
        Notify.gameEnd(arena)
        Notify.winners(arena)

        // actualizar estadisticas
        coroutineScope.launch {
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

        // ejecutar comandos
        executeCommands()

        // iniciar celebracion
        celebrationManager.startCelebration(arena) { completeCelebrationAndReset() }
    }

    private fun completeCelebrationAndReset() {
        DisasterRegistry.removeDisasters(arena)

        Lobby.teleportAtEnd(arena)

        arena.entityCleanupService.cleanupMeteors()
        arena.entityCleanupService.cleanupFireworks()
        arena.entityCleanupService.cleanupExtendedArea(50)
        arena.fluidCleanupService.cleanupFluids()
        arena.fluidCleanupService.cleanupExtendedArea(10)

        arena.clear()

        org.bukkit.Bukkit.getScheduler().runTask(plugin, Runnable {
            arena.resetService.paste()
            plugin.logger.info("Arena ${arena.name} reset completed (after celebration)")
        })
    }

    private fun executeCommands() {
        arena.playing.forEach { player ->
            if (!arena.alive.contains(player)) {
                for (command in arena.losersCommands) {
                    val commandParsed =
                            me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, command)
                    org.bukkit.Bukkit.dispatchCommand(
                            org.bukkit.Bukkit.getConsoleSender(),
                            commandParsed
                    )
                }
            }
            if (arena.alive.contains(player)) {
                for (command in arena.winnersCommands) {
                    val commandParsed =
                            me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, command)
                    org.bukkit.Bukkit.dispatchCommand(
                            org.bukkit.Bukkit.getConsoleSender(),
                            commandParsed
                    )
                }
            }
            for (command in arena.toAllCommands) {
                val commandParsed =
                        me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, command)
                org.bukkit.Bukkit.dispatchCommand(
                        org.bukkit.Bukkit.getConsoleSender(),
                        commandParsed
                )
            }
        }
    }

    fun getTimeLeft(): Int = gameTimer?.remaining ?: 0
    fun getGameTime(): Int = gameTimer?.time ?: 0
    fun getCountdownTime(): Int = countdown?.time ?: 0
    fun getCountdownLeft(): Int = countdown?.remaining ?: 0
    fun getNextDisasterIn(): Int = gameTimer?.getNextDisasterIn() ?: 0
}