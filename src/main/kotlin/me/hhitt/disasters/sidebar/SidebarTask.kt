package me.hhitt.disasters.sidebar

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class SidebarTask(private val arenaManager: ArenaManager, private val sidebarManager: SidebarManager) : BukkitRunnable() {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player: Player ->
            arenaManager.getArena(player)?.let { arena ->
                sidebarManager.updateSidebar(player, arena.state)
                return
            }
            sidebarManager.updateSidebar(player, null)
        }
    }

}
