package me.hhitt.disasters.arena

import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

/** ArenaSetupManager class manages arena setup sessions for players.
 *
 * It allows creating, retrieving, and removing setup sessions,
 * as well as saving the configured arena to a YAML file.
 *
 **/
class ArenaSetupManager {

    // Map to store active setup sessions keyed by player UUID
    private val sessions = mutableMapOf<UUID, ArenaSetupSession>()

    fun createSession(player: Player, arenaName: String) {
        sessions[player.uniqueId] = ArenaSetupSession(arenaName)
    }

    fun getSession(player: Player): ArenaSetupSession? {
        return sessions[player.uniqueId]
    }

    fun removeSession(player: Player) {
        sessions.remove(player.uniqueId)
    }

    // Save the arena configuration to a YAML file
    fun saveArena(session: ArenaSetupSession) {
        val arenaFile = File("plugins/Disasters/arenas/${session.arenaName}.yml")
        arenaFile.parentFile.mkdirs()

        val config = YamlConfiguration()

        // Default settings
        config.set("max-players", 10)
        config.set("min-players", 2)
        config.set("alive-to-end", 1)
        config.set("countdown", 10)
        config.set("game-time", 300)
        config.set("disaster-rate", 30)
        config.set("max-disasters", 4)
        config.set("display-name", "<rainbow>${session.arenaName}")

        // Spawn
        val spawn = session.spawn!!
        config.set("spawn.x", spawn.x)
        config.set("spawn.y", spawn.y)
        config.set("spawn.z", spawn.z)
        config.set("spawn.yaw", spawn.yaw)
        config.set("spawn.pitch", spawn.pitch)
        config.set("spawn.world", spawn.world.name)

        // Corners
        val corner1 = session.corner1!!
        config.set("corner1.x", corner1.blockX)
        config.set("corner1.y", corner1.blockY)
        config.set("corner1.z", corner1.blockZ)
        config.set("corner1.world", corner1.world.name)

        val corner2 = session.corner2!!
        config.set("corner2.x", corner2.blockX)
        config.set("corner2.y", corner2.blockY)
        config.set("corner2.z", corner2.blockZ)
        config.set("corner2.world", corner2.world.name)

        // Default commands
        config.set("commands.winners", listOf("eco give %player_name% 50"))
        config.set("commands.losers", listOf("eco take %player_name% 10"))
        config.set("commands.to-all", listOf("say %player_name% finished the game!"))

        config.save(arenaFile)
    }
}