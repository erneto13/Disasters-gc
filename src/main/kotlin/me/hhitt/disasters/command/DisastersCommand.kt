package me.hhitt.disasters.command

import com.sk89q.worldedit.regions.CuboidRegion
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.sidebar.SidebarService
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Msg
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Suggest
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission
import kotlin.collections.subtract

@Command("disasters")
@CommandPermission("disasters.admin")
class DisastersCommand(
    private val plugin: Disasters,
    private val arenaManager: ArenaManager,
    private val sidebarService: SidebarService
) {

    val version = plugin.pluginMeta.version
    val customBuild = "build-2"

    @Command("disasters")
    fun disasters(actor: BukkitCommandActor) {
        val sender = actor.sender()

        if (sender !is Player) {
            Msg.send(actor.sender(), "messages.only-players")
            return
        }

        Msg.sendList(actor.sender(), "messages.help")
        Msg.sendParsed(actor.sender() as Player, "<#c1c1c1><i> Version $version ($customBuild) - by erneto13")
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
        arenaManager.reloadArenas()
        sidebarService.updateSidebar()
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

    @Subcommand("test")
    fun testDisaster(
        actor: BukkitCommandActor,
        @Named("disaster_name") disasterName: String,
        @Named("duration") duration: Int?
    ) {
        if (!actor.isPlayer) {
            Msg.send(actor.sender(), "messages.only-players")
            return
        }
        val player = actor.asPlayer()!!
        val disasterClass = DisasterRegistry.getDisasterClassByName(disasterName)
        if (disasterClass == null) {
            Msg.sendParsed(player, "<#e33131>Disaster '$disasterName' does not exists.")
            return
        }

        val testArena = Arena(
            "temp", "temp-arena",
            1, 1, 1,
            300, 0, 0, 1,
            player.location, player.location, player.location,
            emptyList(), emptyList(), emptyList(), null
        )
        testArena.alive.add(player)

        val disaster = disasterClass.constructors.first().call()
        disaster.start(testArena)
        Msg.sendParsed(player, "<#fbed3a>Starting test disaster: ${disasterClass.simpleName}")

        val durationTicks = (duration ?: 30) * 20L
        var ticksPassed = 0L

        val task = object : BukkitRunnable() {
            override fun run() {
                if (ticksPassed >= durationTicks || testArena.alive.isEmpty()) {
                    disaster.stop(testArena)
                    Msg.sendParsed(player, "<#fbed3a>The test disaster is over.")
                    cancel()
                    return
                }

                disaster.pulse(ticksPassed.toInt())
                ticksPassed++
            }
        }
        task.runTaskTimer(plugin, 0L, 1L)
    }
}