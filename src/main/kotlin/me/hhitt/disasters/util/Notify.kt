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
        sound(arena, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f)
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

    // single disaster announcement (kept for backwards compatibility)
    fun disaster(arena: Arena, disaster: Disaster) {
        disasters(arena, listOf(disaster))
    }

    // grouped disaster announcements
    fun disasters(arena: Arena, disasterList: List<Disaster>) {
        if (disasterList.isEmpty()) return

        val title = config.getString("disaster.announcement.title") ?: ""
        val subtitle = config.getString("disaster.announcement.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)

        // build disaster list text
        val disasterNames =
                disasterList.map { disaster ->
                    val key =
                            disaster.javaClass
                                    .simpleName
                                    .replace(Regex("([a-z])([A-Z])"), "$1-$2")
                                    .lowercase()
                    config.getString("disaster.$key.name") ?: key
                }

        val disasterDescriptions =
                disasterList.map { disaster ->
                    val key =
                            disaster.javaClass
                                    .simpleName
                                    .replace(Regex("([a-z])([A-Z])"), "$1-$2")
                                    .lowercase()
                    config.getString("disaster.$key.description") ?: ""
                }

        // build chat messages
        val chatTemplate =
                if (disasterList.size == 1) {
                    config.getStringList("disaster.announcement.chat-single")
                } else {
                    config.getStringList("disaster.announcement.chat-multiple")
                }

        // create disaster list string
        val disasterListStr =
                disasterNames
                        .mapIndexed { index, name -> "$name ${disasterDescriptions[index]}" }
                        .joinToString("\n")

        val chatMessages =
                chatTemplate.map { line ->
                    line.replace("%disaster_name%", disasterNames.firstOrNull() ?: "")
                            .replace(
                                    "%disaster_description%",
                                    disasterDescriptions.firstOrNull() ?: ""
                            )
                            .replace("%disaster_list%", disasterListStr)
                            .replace("%disaster_count%", disasterList.size.toString())
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
            val noWinnersMessages = config.getStringList("game-winners.no-winners")
            sendChatMessagesToArena(arena, noWinnersMessages)
            return
        }

        val winnerEntry =
                config.getString("game-winners.winner-entry") ?: "<yellow>★ <white>%player_name%"
        val winnersList =
                arena.alive.joinToString("\n") { player -> Msg.placeholder(winnerEntry, player) }

        val title = config.getString("game-winners.title") ?: ""
        sendTitleToArena(arena, title, "")

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
