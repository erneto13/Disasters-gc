package me.hhitt.disasters.command

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.sidebar.SidebarService
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Msg
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("dg")
@CommandPermission("disasters.admin")
class DisastersCommand(
        private val plugin: Disasters,
        private val arenaManager: ArenaManager,
        private val sidebarService: SidebarService
) {

    val version = plugin.pluginMeta.version
    val customBuild = "build-2"

    @Command("dg")
    fun disasters(actor: BukkitCommandActor) {
        val sender = actor.sender()

        if (sender !is Player) {
            Msg.send(actor.sender(), "messages.only-players")
            return
        }

        Msg.sendList(actor.sender(), "messages.help")
        Msg.sendParsed(
                actor.sender() as Player,
                "<#c1c1c1><i> Version $version ($customBuild) - by erneto13"
        )
        Msg.sendParsed(actor.sender() as Player, "")
    }

    @Subcommand("help")
    fun disastersHelp(actor: BukkitCommandActor) {
        disasters(actor)
    }

    @Subcommand("reload")
    fun reload(actor: BukkitCommandActor) {
        FileManager.get("config")!!.reloadFile()
        FileManager.get("lang")!!.reloadFile()
        FileManager.get("disasters")!!.reloadFile()
        arenaManager.reloadArenas()
        sidebarService.updateSidebar()
        DisasterRegistry.reloadConfig()
        Msg.send(actor.sender(), "messages.reload-success")
    }

    @Subcommand("setspawn")
    fun setSpawn(actor: BukkitCommandActor) {
        if (!actor.isPlayer) return
        val player = actor.asPlayer()!!
        val config = FileManager.get("config")!!

        config.set("lobby.world", player.world.name)
        config.set("lobby.x", player.location.x)
        config.set("lobby.y", player.location.y)
        config.set("lobby.z", player.location.z)
        config.set("lobby.yaw", player.location.yaw)
        config.set("lobby.pitch", player.location.pitch)
        config.save()

        FileManager.reload("config")
        Lobby.setLocation()
        Msg.send(actor.sender(), "messages.lobby-set")
    }
}
