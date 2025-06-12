package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class PlayerDamageListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        val arena = arenaManager.getArena(victim) ?: return
        val attackerArena = arenaManager.getArena(attacker)

        // Both in same arena
        if (arena != attackerArena) {
            event.isCancelled = true
            return
        }

        // Check if pvp is allowed
        val allowFight = DisasterRegistry.isAllowedToFight(arena, attacker)
        val isMurder = DisasterRegistry.isMurder(arena, attacker)

        if (!allowFight || !isMurder) {
            event.isCancelled = true
            return
        }

        val damage = event.finalDamage
        val health = victim.health

        if (health - damage > 0) return

        // Manage death
        arena.playerDied(victim)
        Data.increaseDefeats(victim.uniqueId)
        Data.increaseTotalPlayed(victim.uniqueId)

        Data.increaseWins(attacker.uniqueId)
        Notify.playerKilled(victim, attacker, arena)
    }
}