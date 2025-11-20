package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class PlayerDamageListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDamageGeneral(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val arena = arenaManager.getArena(player) ?: return

        // cancel all damage while waiting
        if (arena.state == GameState.RECRUITING || arena.state == GameState.COUNTDOWN) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        val arena = arenaManager.getArena(victim) ?: return
        val attackerArena = arenaManager.getArena(attacker)

        if (arena != attackerArena) {
            event.isCancelled = true
            return
        }

        // cancel damage during waiting states
        if (arena.state == GameState.RECRUITING || arena.state == GameState.COUNTDOWN) {
            event.isCancelled = true
            return
        }

        val allowFight = DisasterRegistry.isAllowedToFight(arena, attacker)
        val isMurder = DisasterRegistry.isMurder(arena, attacker)

        if (!allowFight || !isMurder) {
            event.isCancelled = true
            return
        }

        val damage = event.finalDamage
        val health = victim.health

        if (health - damage > 0) return

        arena.playerDied(victim)
        Data.increaseDefeats(victim.uniqueId)
        Data.increaseTotalPlayed(victim.uniqueId)

        Data.increaseWins(attacker.uniqueId)
        Notify.playerKilled(victim, attacker, arena)
    }
}
