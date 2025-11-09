package me.hhitt.disasters.util

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

object Notify {

    private val config: FileConfiguration = FileManager.get("lang")!!

    fun countdown(arena: Arena, index: Int) {
        val path = "countdown.$index"
        val title = config.getString("$path.title") ?: ""
        val subtitle = config.getString("$path.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.UI_BUTTON_CLICK, 1.0f, 1.5f)
    }

    fun countdownCanceled(arena: Arena) {
        val title = config.getString("countdown-canceled.title") ?: ""
        val subtitle = config.getString("countdown-canceled.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BASS)
    }

    fun gameStart(arena: Arena) {
        val title = config.getString("game-start.title") ?: ""
        val subtitle = config.getString("game-start.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BELL)
    }

    fun gameEnd(arena: Arena) {
        val title = config.getString("game-end.title") ?: ""
        val subtitle = config.getString("game-end.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BASS)
    }

    fun disaster(arena: Arena, disaster: Disaster) {
        val key =
                disaster.javaClass.simpleName.replace(Regex("([a-z])([A-Z])"), "$1-$2").lowercase()

        val name = config.getString("disaster.$key.name") ?: key
        val description = config.getString("disaster.$key.description") ?: ""

        val title = config.getString("disaster.announcement.title") ?: ""
        val subtitle = config.getString("disaster.announcement.subtitle") ?: ""

        sendTitleToArena(arena, title, subtitle)

        val chatMessages =
                config.getStringList("disaster.announcement.chat").map { line ->
                    line.replace("%disaster_name%", name)
                            .replace("%disaster_description%", description)
                }

        sendChatMessagesToArena(arena, chatMessages)
    }

    fun playerJoined(player: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-joined") ?: ""
        val msg = message.replace("%joined%", player.name)
        sendChatMessageToArena(arena, msg)
        sound(arena, Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
    }

    fun playerLeft(player: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-left") ?: ""
        val msg = message.replace("%left%", player.name)
        sendChatMessageToArena(arena, msg)
        sound(arena, Sound.ENTITY_ALLAY_DEATH)
    }

    fun playerKilled(player: Player, killer: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-killed") ?: ""
        val msg = message.replace("%killer%", player.name)
        sendChatMessageToArena(arena, msg)
    }

    fun playerDied(player: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-died") ?: ""
        val msg = message.replace("%dead%", player.name)
        sendChatMessageToArena(arena, msg)
    }

    fun winners(arena: Arena) {
        if (arena.alive.isEmpty()) {
            // no hay ganadores
            val noWinnersMessages = config.getStringList("game-winners.no-winners")
            sendChatMessagesToArena(arena, noWinnersMessages)
            return
        }

        // construir lista de ganadores
        val winnerEntry =
                config.getString("game-winners.winner-entry") ?: "<yellow>★ <white>%player_name%"
        val winnersList =
                arena.alive.joinToString("\n") { player -> Msg.placeholder(winnerEntry, player) }

        // title
        val title = config.getString("game-winners.title") ?: ""
        sendTitleToArena(arena, title, "")

        // mensajes de chat
        val chatMessages =
                config.getStringList("game-winners.chat").map { line ->
                    line.replace("%winners_list%", winnersList)
                }

        sendChatMessagesToArena(arena, chatMessages)
    }

    private fun sendTitleToArena(arena: Arena, title: String, subtitle: String) {
        arena.playing.forEach { player ->
            Msg.sendTitle(player, title)
            Msg.sendSubtitle(player, subtitle)
        }
    }

    private fun sendChatMessagesToArena(arena: Arena, messages: List<String>) {
        arena.playing.forEach { player ->
            messages.forEach { message -> Msg.sendParsed(player, message) }
        }
    }

    private fun sendChatMessageToArena(arena: Arena, message: String) {
        arena.playing.forEach { player -> Msg.sendParsed(player, message) }
    }

    private fun sound(arena: Arena, sound: Sound, volume: Float = 1.0f, pitch: Float = 1.0f) {
        arena.playing.forEach { player -> player.playSound(player.location, sound, volume, pitch) }
    }
}
