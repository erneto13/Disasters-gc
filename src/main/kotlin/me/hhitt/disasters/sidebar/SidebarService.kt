package me.hhitt.disasters.sidebar

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.file.FileManager
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary

class SidebarService(private val scoreboardLibrary: ScoreboardLibrary, private val arenaManager: ArenaManager) {

    private val plugin = Disasters.getInstance()
    private var sidebarManager: SidebarManager = SidebarManager(scoreboardLibrary)
    private var sidebarTask: SidebarTask = SidebarTask(arenaManager, sidebarManager)

    init {
        if(FileManager.get("config")!!.getBoolean("enable-scoreboard")) {
            sidebarTask.runTaskTimerAsynchronously(plugin, 0, 20L)
        }
    }

    fun updateSidebar() {
        if(FileManager.get("config")!!.getBoolean("enable-scoreboard")) {
            if(!sidebarTask.isCancelled) return
            sidebarTask.runTaskTimerAsynchronously(plugin, 0, 20L)
            return
        }
        sidebarTask.cancel()
    }
}