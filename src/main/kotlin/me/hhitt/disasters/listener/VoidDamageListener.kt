package me.hhitt.disasters.listener

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.game.GameState
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class VoidDamageListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onVoidDamage(event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.VOID) return

        val player = event.entity as? org.bukkit.entity.Player ?: return

        arenaManager.getArena(player)?.let { arena ->
            event.isCancelled = true

            Disasters.getInstance()
                    .server
                    .scheduler
                    .runTask(
                            Disasters.getInstance(),
                            Runnable {
                                if (arena.state == GameState.RECRUITING ||
                                                arena.state == GameState.COUNTDOWN
                                ) {
                                    player.teleport(arena.location)
                                    return@Runnable
                                }

                                if (arena.state == GameState.LIVE) {
                                    player.teleport(arena.location)
                                    player.damage(4.0)
                                }
                            }
                    )
        }
    }
}
