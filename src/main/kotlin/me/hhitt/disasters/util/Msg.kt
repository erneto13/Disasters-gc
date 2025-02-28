package me.hhitt.disasters.util

import me.clip.placeholderapi.PlaceholderAPI
import me.hhitt.disasters.storage.file.FileManager
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.TitlePart
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Msg {

    private val miniMsg = MiniMessage.miniMessage()

    fun parse(msg : String, player: Player) : TextComponent {
        return miniMsg.deserialize(placeholder(msg, player)) as TextComponent
    }

    fun parseList(lore: List<String>, player: Player) : List<TextComponent> {
        val components = mutableListOf<TextComponent>()
        lore.forEach {
            components.add(parse(it, player))
        }
        return components
    }


    fun sendParsed(player: Player, msg: String){
        player.sendMessage(parse(msg, player))
    }


    fun sendTitle(player: Player, title: String){
        player.sendTitlePart(TitlePart.TITLE, parse(title, player))
    }

    fun sendSubtitle(player: Player, subtitle: String){
        player.sendTitlePart(TitlePart.SUBTITLE, parse(subtitle, player))
    }

    fun sendActionbar(player: Player, bar: String){
        player.sendActionBar(parse(bar, player))
    }

    fun playSound(player: Player, sound: String){
        player.playSound(player.location, sound, 1f, 1f)
    }

    private fun getMsg(path: String) : String {
        return FileManager.get("lang")?.getString(path) ?: "Message not found"
    }

    private fun placeholder(msg: String, player: Player) : String {
        return PlaceholderAPI.setPlaceholders(player, msg)
    }

}