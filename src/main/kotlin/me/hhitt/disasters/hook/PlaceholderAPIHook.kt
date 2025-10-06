package me.hhitt.disasters.hook

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.entity.Player

/**
 * PlaceholderAPI Hook for Disasters plugin.
 *
 * @param arenaManager The ArenaManager instance to manage arenas.
 */

class PlaceholderAPIHook(private val arenaManager: ArenaManager) : PlaceholderExpansion() {

    private val plugin = Disasters.getInstance()

    override fun getIdentifier(): String {
        return "disasters"
    }

    override fun getAuthor(): String {
        return "hhitt"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onPlaceholderRequest(player: Player, params: String): String {
        // Dynamic arena placeholders: <arena-id>_state, _players, _alive, _max, _min, _is_full
        if (params.endsWith("_state") || params.endsWith("_players") || params.endsWith("_alive") ||
            params.endsWith("_max") || params.endsWith("_min") || params.endsWith("_is_full")) {
            val arenaId = params.substringBeforeLast("_")
            val arena = arenaManager.getArena(arenaId) ?: return ""
            return when {
                params.endsWith("_state") -> getStateString(arena.state)
                params.endsWith("_players") -> arena.playing.size.toString()
                params.endsWith("_alive") -> arena.alive.size.toString()
                params.endsWith("_max") -> arena.maxPlayers.toString()
                params.endsWith("_min") -> arena.minPlayers.toString()
                params.endsWith("_is_full") -> arena.isFull().toString()
                else -> ""
            }
        }

        return when (params) {
            // Player stats (cached)
            "wins", "player_wins" -> Data.getWinsFromCache(player.uniqueId).toString()
            "defeats", "player_defeats" -> Data.getDefeatsFromCache(player.uniqueId).toString()
            "total_played", "player_total_played" -> Data.getTotalPlayedFromCache(player.uniqueId).toString()
            "player_wlr" -> {
                val wins = Data.getWinsFromCache(player.uniqueId)
                val defeats = Data.getDefeatsFromCache(player.uniqueId)
                if (defeats == 0) wins.toString() else String.format("%.2f", wins.toDouble() / defeats.toDouble())
            }

            // Global info
            "total_playing", "global_players_total" -> arenaManager.getArenas().sumOf { it.playing.size }.toString()
            "disasters_players_alive_total" -> arenaManager.getArenas().sumOf { it.alive.size }.toString()
            "global_arenas_count" -> arenaManager.getArenas().size.toString()
            "global_arenas_waiting" -> arenaManager.getArenas().count { it.isWaiting() }.toString()
            "global_arenas_running" -> arenaManager.getArenas().count { it.state == GameState.LIVE }.toString()

            // Player arena context
            "arena", "player_arena_name" -> arenaManager.getArena(player)?.displayName ?: "Not in an arena"
            "player_arena_id" -> arenaManager.getArena(player)?.name ?: ""
            "arena_playing", "game_players" -> arenaManager.getArena(player)?.playing?.size?.toString() ?: "0"
            "is_in_arena", "player_is_in_arena" -> (arenaManager.getArena(player) != null).toString()

            // Current game (player arena)
            "game_max_players" -> arenaManager.getArena(player)?.maxPlayers?.toString() ?: "0"
            "game_min_players" -> arenaManager.getArena(player)?.minPlayers?.toString() ?: "0"
            "game_state" -> arenaManager.getArena(player)?.state?.let { getStateString(it) } ?: ""
            "game_time" -> arenaManager.getArena(player)?.getGameTime()?.toString() ?: "0"
            "game_time_left" -> arenaManager.getArena(player)?.getTimeLeft()?.toString() ?: "0"
            "countdown_time" -> arenaManager.getArena(player)?.getCountdownTime()?.toString() ?: "0"
            "countdown_time_left" -> arenaManager.getArena(player)?.getCountdownLeft()?.toString() ?: "0"
            "game_alive" -> arenaManager.getArena(player)?.alive?.size?.toString() ?: "0"
            "game_spectators" -> {
                val arena = arenaManager.getArena(player)
                if (arena != null) (arena.playing.size - arena.alive.size).coerceAtLeast(0).toString() else "0"
            }
            "game_disasters_count" -> arenaManager.getArena(player)?.disasters?.size?.toString() ?: "0"
            "game_disasters_list" -> arenaManager.getArena(player)?.disasters?.joinToString(", ") { it.javaClass.simpleName } ?: ""
            "game_is_full" -> arenaManager.getArena(player)?.isFull()?.toString() ?: "false"
            "player_is_alive" -> {
                val arena = arenaManager.getArena(player)
                arena?.alive?.contains(player)?.toString() ?: "false"
            }

            else -> "Invalid placeholder"
        }
    }

    private fun getStateString(state: GameState): String {
        val lang = FileManager.get("lang")
        return when (state) {
            GameState.RECRUITING -> lang?.getString("game-state-placeholders.recruiting")
            GameState.COUNTDOWN -> lang?.getString("game-state-placeholders.countdown")
            GameState.LIVE -> lang?.getString("game-state-placeholders.live")
            GameState.RESTARTING -> lang?.getString("game-state-placeholders.restarting")
        } ?: state.name
    }
}