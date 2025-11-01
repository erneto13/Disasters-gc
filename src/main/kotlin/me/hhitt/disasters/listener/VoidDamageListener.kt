package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.game.GameState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class VoidDamageListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onVoidDamage(event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.VOID) return

        val player = event.entity as? org.bukkit.entity.Player ?: return

        arenaManager.getArena(player)?.let { arena ->
            event.isCancelled = true

            player.teleport(arena.location)

            if (arena.state == GameState.RECRUITING || arena.state == GameState.COUNTDOWN) {
                return
            }

            if (arena.state == GameState.LIVE) {
                player.damage(20.0)
            }
        }
    }
}
