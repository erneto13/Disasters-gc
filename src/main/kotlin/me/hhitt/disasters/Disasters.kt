package me.hhitt.disasters

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.command.ArenaCommand
import me.hhitt.disasters.command.DisastersCommand
import me.hhitt.disasters.listener.PlayerJoinListener
import me.hhitt.disasters.listener.PlayerLeaveListener
import me.hhitt.disasters.listener.PlayerMoveListener
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Filer
import me.hhitt.disasters.util.Lobby
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

class Disasters : JavaPlugin() {

    companion object {
        private lateinit var instance: Disasters

        fun getInstance(): Disasters = instance
    }

    private lateinit var arenaManager: ArenaManager

    override fun onEnable() {

        instance = this
        Filer.createFolders()
        FileManager.initialize()
        Lobby.setLocation()
        Data.load()

        arenaManager = ArenaManager()

        val lamp: Lamp<BukkitCommandActor> = BukkitLamp.builder(this)
            .build()
        lamp.register(ArenaCommand(arenaManager), DisastersCommand())

        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(PlayerLeaveListener(), this)
        server.pluginManager.registerEvents(PlayerMoveListener(arenaManager), this)

    }

    override fun onDisable() {
    }

}
