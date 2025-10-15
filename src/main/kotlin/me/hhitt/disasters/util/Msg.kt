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

    fun parse(msg : String) : TextComponent {
        return miniMsg.deserialize(msg) as TextComponent
    }

    fun send(player: Player, path: String){
        player.sendMessage(parse(getMsg(path), player))
    }

    fun send(player: Player, path: String, vararg replacements: Pair<String, String>){
        val msg = replacePlaceholders(getMsg(path), *replacements)
        player.sendMessage(parse(msg, player))
    }

    fun send(sender: CommandSender, path: String){
        sender.sendMessage(parse(getMsg(path)))
    }

    fun send(sender: CommandSender, path: String, vararg replacements: Pair<String, String>){
        val msg = replacePlaceholders(getMsg(path), *replacements)
        if (sender is Player) {
            sender.sendMessage(parse(msg, sender))
        } else {
            sender.sendMessage(parse(msg))
        }
    }

    fun sendList(sender: CommandSender, path: String) {
        val messages = getMsgList(path)
        if (sender is Player) {
            messages.forEach { msg ->
                sender.sendMessage(parse(msg, sender))
            }
        } else {
            messages.forEach { msg ->
                sender.sendMessage(parse(msg))
            }
        }
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

    fun placeholder(msg: String, player: Player) : String {
        return PlaceholderAPI.setPlaceholders(player, msg)
    }

    fun sendActionbar(player: Player, bar: String){
        player.sendActionBar(parse(bar, player))
    }

    fun playSound(player: Player, sound: String){
        player.playSound(player.location, sound, 1f, 1f)
    }

    fun getMsg(path: String): String {
        return FileManager.get("lang")?.getString(path) ?: "Message not found: $path"
    }

    fun getMsgList(path: String): List<String> {
        return FileManager.get("lang")?.getStringList(path)
            ?: listOf("Message list not found: $path")
    }

    private fun replacePlaceholders(msg: String, vararg replacements: Pair<String, String>): String {
        var result = msg
        replacements.forEach { (key, value) ->
            result = result.replace("%$key%", value)
        }
        return result
    }
}