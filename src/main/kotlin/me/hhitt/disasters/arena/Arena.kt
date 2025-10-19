package me.hhitt.disasters.arena

import com.sk89q.worldedit.bukkit.WorldEditPlugin
import me.hhitt.disasters.arena.service.BorderService
import me.hhitt.disasters.arena.service.ResetArenaService
import me.hhitt.disasters.arena.service.RespawnService
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
    private val respawnService = RespawnService(this)
    private val gameSession = GameSession(this)

    fun addPlayer(player: Player) {
        // Save player inventory/state before entering the game and clear it for arena gameplay
        Lobby.savePlayerState(player)
        player.inventory.clear()

        playing.add(player)
        alive.add(player)
        player.teleport(location)
        Notify.playerJoined(player, this)

        val requiredPlayers = if (isTestMode) 1 else minPlayers
        if(playing.size >= requiredPlayers) {
            start()
        }
    }

    fun playerDied(player: Player) {
        alive.remove(player)
        when(state) {
            GameState.LIVE -> {
                respawnService.setSpectator(player)
            }
            else -> {
                respawnService.respawnAtArena(player)
            }
        }
    }

    fun removePlayer(player: Player) {
        if(disasters.contains(WorldBorder())) {
            resetWorldBorder(player)
        }
        Lobby.teleportPlayer(player)
        playing.remove(player)
        alive.remove(player)

        if(isWaiting()) {
            val requiredPlayers = if (isTestMode) 1 else minPlayers
            if(playing.size < requiredPlayers) {
                stop()
            }
        } else {
            if(isTestMode && alive.isEmpty()) {
                stop()
            } else if(!isTestMode && alive.size < aliveToEnd) {
                stop()
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
        gameSession.start()
    }

    fun stop() {
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