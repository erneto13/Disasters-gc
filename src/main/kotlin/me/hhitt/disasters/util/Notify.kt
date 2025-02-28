package me.hhitt.disasters.util

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.storage.file.FileManager
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.configuration.file.FileConfiguration

object Notify {

    private val config: FileConfiguration = FileManager.get("lang")!!

    fun countdown(arena: Arena, index: Int) {
        val path = "countdown.$index"
        val title = config.getString("$path.title") ?: ""
        val subtitle = config.getString("$path.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
    }

    fun countdownCanceled(arena: Arena) {
        val title = config.getString("countdown-canceled.title") ?: ""
        val subtitle = config.getString("countdown-canceled.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
    }

    fun gameStart(arena: Arena) {
        val title = config.getString("game-start.title") ?: ""
        val subtitle = config.getString("game-start.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
    }

    fun gameEnd(arena: Arena) {
        val title = config.getString("game-end.title") ?: ""
        val subtitle = config.getString("game-end.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
    }

    fun disaster(arena: Arena, disaster: String) {
        val path = "disaster.$disaster"
        val title = config.getString("$path.title") ?: ""
        val subtitle = config.getString("$path.subtitle") ?: ""
        val chatMessages = config.getStringList("$path.chat")

        sendTitleToArena(arena, title, subtitle)
        sendChatMessagesToArena(arena, chatMessages)
    }

    private fun sendTitleToArena(arena: Arena, title: String, subtitle: String) {
        arena.playing.forEach { player ->
            Msg.sendTitle(player, title)
            Msg.sendSubtitle(player, subtitle)
        }
    }

    private fun sendChatMessagesToArena(arena: Arena, messages: List<String>) {
        arena.playing.forEach {
            player -> messages.forEach {
                message -> Msg.sendParsed(player, message)
            }
        }
    }


}
