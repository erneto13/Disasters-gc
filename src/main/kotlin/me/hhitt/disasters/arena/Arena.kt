package me.hhitt.disasters.arena

import com.sk89q.worldedit.bukkit.WorldEditPlugin
import me.hhitt.disasters.arena.service.*
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.impl.WorldBorder
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Notify
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

class Arena(
        val name: String,
        val displayName: String,
        val minPlayers: Int,
        val maxPlayers: Int,
        val aliveToEnd: Int,
        val maxTime: Int,
        val countdown: Int,
        val rate: Int,
        val maxDisasters: Int,
        val location: Location,
        val corner1: Location,
        val corner2: Location,
        val winnersCommands: List<String>,
        val losersCommands: List<String>,
        val toAllCommands: List<String>,
        worldEdit: WorldEditPlugin?,
        val isTestMode: Boolean = false
) {

    val playing: MutableList<Player> = mutableListOf()
    val alive: MutableList<Player> = mutableListOf()
    val disasters: MutableList<Disaster> = mutableListOf()
    var state = GameState.RECRUITING
    val borderService = BorderService(corner1, corner2)
    val resetService = ResetArenaService(this, worldEdit)
    val entityCleanupService = EntityCleanupService(this)
    val fluidCleanupService = FluidCleanupService(this)
    private val respawnService = RespawnService(this)
    private val gameSession = GameSession(this)

    fun addPlayer(player: Player) {
        // Don't allow joining if arena is restarting
        if (state == GameState.RESTARTING) {
            Notify.arenaRestarting(player)
            return
        }

        Lobby.savePlayerState(player)
        player.inventory.clear()

        playing.add(player)
        alive.add(player)
        player.teleport(location)
        Notify.playerJoined(player, this)

        val requiredPlayers = if (isTestMode) 1 else minPlayers

        // Only start countdown if we have enough players AND we're still in RECRUITING state
        if (playing.size >= requiredPlayers && state == GameState.RECRUITING) {
            start()
        }
    }

    fun playerDied(player: Player) {
        alive.remove(player)
        when (state) {
            GameState.LIVE -> {
                respawnService.setSpectator(player)
            }
            else -> {
                respawnService.respawnAtArena(player)
            }
        }
    }

    fun removePlayer(player: Player) {
        // Clean world border for this player if active
        if (disasters.contains(WorldBorder())) {
            resetWorldBorder(player)
        }

        Lobby.teleportPlayer(player)
        playing.remove(player)
        alive.remove(player)

        // Don't trigger stop if already restarting
        if (state == GameState.RESTARTING) {
            return
        }

        val requiredPlayers = if (isTestMode) 1 else minPlayers

        // Only cancel/stop if we don't have enough players anymore
        if (isTestMode) {
            if (playing.isEmpty()) {
                stop()
            }
        } else {
            // If in countdown or recruiting, check if we still have enough players
            if (state == GameState.RECRUITING || state == GameState.COUNTDOWN) {
                if (playing.size < requiredPlayers) {
                    // Cancel countdown if active, return to recruiting
                    if (state == GameState.COUNTDOWN) {
                        gameSession.cancelCountdown()
                        state = GameState.RECRUITING
                    }
                }
            } else if (state == GameState.LIVE) {
                // In live game, check alive count
                if (alive.size < aliveToEnd) {
                    stop()
                }
            }
        }

        Notify.playerLeft(player, this)
    }

    fun isFull(): Boolean {
        return (playing.size == maxPlayers)
    }

    fun isEmpty(): Boolean {
        return playing.size == 0
    }

    fun isWaiting(): Boolean {
        return state == GameState.RECRUITING || state == GameState.COUNTDOWN
    }

    fun isPlayerValid(player: Player): Boolean {
        return playing.contains(player)
    }

    fun start() {
        // Only start if we're in RECRUITING state
        if (state == GameState.RECRUITING) {
            gameSession.start()
        }
    }

    fun stop() {
        // Prevent multiple stop calls
        if (state == GameState.RESTARTING) {
            return
        }

        state = GameState.RESTARTING
        gameSession.stop()
    }

    fun clear() {
        playing.clear()
        alive.clear()
        disasters.clear()
    }

    fun getTimeLeft(): Int {
        return gameSession.getTimeLeft()
    }

    fun getGameTime(): Int {
        return gameSession.getGameTime()
    }

    fun getNextDisasterIn(): Int {
        return gameSession.getNextDisasterIn()
    }

    fun getCountdownTime(): Int {
        return gameSession.getCountdownTime()
    }

    fun getCountdownLeft(): Int {
        return gameSession.getCountdownLeft()
    }

    private fun resetWorldBorder(player: Player) {
        val craftWorld = player.world as CraftWorld
        val worldServer = craftWorld.handle
        val worldBorder = net.minecraft.world.level.border.WorldBorder()
        worldBorder.world = worldServer
        worldBorder.setCenter(player.world.spawnLocation.x, player.world.spawnLocation.z)
        worldBorder.size = player.world.worldBorder.maxSize

        val packet = ClientboundInitializeBorderPacket(worldBorder)
        (player as CraftPlayer).handle.connection.send(packet)
    }
}
