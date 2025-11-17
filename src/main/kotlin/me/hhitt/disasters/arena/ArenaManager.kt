package me.hhitt.disasters.arena

import com.sk89q.worldedit.bukkit.WorldEditPlugin
import java.io.File
import me.hhitt.disasters.Disasters
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

class ArenaManager(private val worldEdit: WorldEditPlugin?) {

    private val plugin = Disasters.getInstance()
    private val arenas = mutableListOf<Arena>()

    init {
        createDefaultArenaFile()
        loadArenas()
    }

    private fun createDefaultArenaFile() {
        val arenasFolder = File(plugin.dataFolder, "arenas")
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs()
        }
    }

    private fun loadArenas() {
        val arenasFolder = File(plugin.dataFolder, "arenas")

        if (!arenasFolder.exists() || !arenasFolder.isDirectory) {
            plugin.logger.severe("Arenas folder does not exist or is not a directory.")
            return
        }

        val arenaFiles =
                arenasFolder.listFiles { _, name -> name.endsWith(".yml", ignoreCase = true) }
                        ?: return
        if (arenaFiles.isEmpty()) {
            plugin.logger.warning("No arena files found in the arenas folder.")
            return
        }

        for (arenaFile in arenaFiles) {
            val arenaConfig = YamlConfiguration.loadConfiguration(arenaFile)
            val arenaID = arenaFile.nameWithoutExtension
            val gameTime = arenaConfig.getInt("game-time")
            val countdown = arenaConfig.getInt("countdown")
            val maxPlayers = arenaConfig.getInt("max-players")
            val minPlayers = arenaConfig.getInt("min-players")
            val aliveToEnd = arenaConfig.getInt("alive-to-end")
            val disasterRate = arenaConfig.getInt("disaster-rate")
            val maxDisasters = arenaConfig.getInt("max-disasters")
            val displayName = arenaConfig.getString("display-name")!!

            // load test mode (default false if it doesn't exist)
            val isTestMode = arenaConfig.getBoolean("test-mode", false)

            val location =
                    Location(
                            Bukkit.getWorld(arenaConfig.getString("spawn.world")!!),
                            arenaConfig.getDouble("spawn.x"),
                            arenaConfig.getDouble("spawn.y"),
                            arenaConfig.getDouble("spawn.z"),
                            arenaConfig.getInt("spawn.yaw").toFloat(),
                            arenaConfig.getInt("spawn.pitch").toFloat()
                    )
            val corner1 =
                    Location(
                            plugin.server.getWorld(arenaConfig.getString("corner1.world")!!),
                            arenaConfig.getDouble("corner1.x"),
                            arenaConfig.getDouble("corner1.y"),
                            arenaConfig.getDouble("corner1.z")
                    )
            val corner2 =
                    Location(
                            plugin.server.getWorld(arenaConfig.getString("corner2.world")!!),
                            arenaConfig.getDouble("corner2.x"),
                            arenaConfig.getDouble("corner2.y"),
                            arenaConfig.getDouble("corner2.z")
                    )

            val winnersCommands = arenaConfig.getStringList("commands.winners")
            val losersCommands = arenaConfig.getStringList("commands.losers")
            val toAllCommands = arenaConfig.getStringList("commands.to-all")

            val arena =
                    Arena(
                            arenaID,
                            displayName,
                            minPlayers,
                            maxPlayers,
                            aliveToEnd,
                            gameTime,
                            countdown,
                            disasterRate,
                            maxDisasters,
                            location,
                            corner1,
                            corner2,
                            winnersCommands,
                            losersCommands,
                            toAllCommands,
                            worldEdit,
                            isTestMode
                    )

            arenas.add(arena)

            if (isTestMode) {
                plugin.logger.info("Arena '$arenaID' loaded in TEST MODE")
            }
        }
    }

    fun getArenas(): List<Arena> = arenas

    fun getArena(player: Player): Arena? {
        return arenas.firstOrNull { it.isPlayerValid(player) }
    }

    fun getArena(arenaId: String): Arena? {
        return arenas.firstOrNull { it.name.equals(arenaId, ignoreCase = true) }
    }

    fun addPlayerToBestArena(player: Player) {
        val bestArena =
                arenas
                        .filter { it.isWaiting() }
                        .sortedWith(
                                compareByDescending<Arena> { it.isFull() }.thenBy { it.isEmpty() }
                        )
                        .firstOrNull()

        bestArena?.addPlayer(player)
    }

    fun reloadArena(arenaFile: File) {
        val arenaID = arenaFile.nameWithoutExtension
        val arena = getArena(arenaID)
        arena?.let { loadArenas() }
    }

    fun removeArena(arenaID: String): Boolean {
        val arena = getArena(arenaID) ?: return false
        return arenas.remove(arena)
    }

    fun reloadArenas() {
        arenas.clear()
        loadArenas()
    }

    fun getArena(location: Location): Arena? {
        return arenas.find { it.borderService.isLocationInArena(location) }
    }
}
