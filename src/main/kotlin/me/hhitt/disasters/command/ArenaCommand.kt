package me.hhitt.disasters.command

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Msg
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor

/**
 * ArenaCommand class that handles the commands related to arena prefix.
 *
 * @param arenaManager The ArenaManager instance used to manage arenas.
 */
@Command("dg")
class ArenaCommand(private val arenaManager: ArenaManager) {

    private val lang = FileManager.get("lang")!!

    @Subcommand("arena join <arena>")
    fun join(actor: BukkitCommandActor, arena: String) {
        if (!actor.isPlayer) return
        val player = actor.asPlayer()!!

        arenaManager.getArena(player)?.let { currentArena ->
            if (currentArena.state == GameState.RESTARTING) {
                currentArena.removePlayer(player)
            } else {
                Msg.send(player, "messages.already-in-arena")
                return
            }
        }

        arenaManager.getArena(arena)?.let {
            if (it.isFull()) {
                Msg.send(player, "messages.arena-full")
                return
            }
            if (!it.isWaiting()) {
                Msg.send(player, "messages.arena-in-game")
                return
            }
            it.addPlayer(player)
        }
                ?: run { Msg.send(player, "messages.arena-not-found") }
    }

    @Subcommand("arena quickjoin")
    fun quickJoin(actor: BukkitCommandActor) {
        if (!actor.isPlayer) return
        val player = actor.asPlayer()!!

        arenaManager.getArena(player)?.let { currentArena ->
            if (currentArena.state == GameState.RESTARTING) {
                currentArena.removePlayer(player)
                Bukkit.getScheduler()
                        .runTaskLater(
                                Disasters.getInstance(),
                                Runnable { arenaManager.addPlayerToBestArena(player) },
                                5L
                        )
                return
            } else {
                Msg.send(player, "messages.already-in-arena")
                return
            }
        }

        arenaManager.addPlayerToBestArena(player)
    }

    @Subcommand("arena leave")
    fun leave(actor: BukkitCommandActor) {
        // Checking if the actor is a player and obtaining it
        if (!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(player)?.removePlayer(player)
                ?: run { Msg.send(player, "messages.not-in-arena") }
    }

    @Subcommand("arena forcestart")
    fun forceStart(actor: BukkitCommandActor) {
        // Checking if the actor is a player and obtaining it
        if (!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(player)?.let {
            // Checking if the player has permission
            if (!player.hasPermission("disasters.forcestart")) {
                Msg.send(player, "messages.no-permission")

                return
            }
            it.start()
        }
                ?: run {
                    // If the arena does not exist
                    Msg.send(player, "messages.not-in-arena")
                }
    }

    @Subcommand("arena forcestop")
    fun forceStop(actor: BukkitCommandActor) {
        // Checking if the actor is a player and obtaining it
        if (!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(player)?.let {
            // Checking if the player has permission
            if (!player.hasPermission("disasters.forcestop")) {
                Msg.send(player, "messages.no-permission")
                return
            }
            it.stop()
        }
                ?: run {
                    // If the arena does not exist
                    Msg.send(player, "messages.not-in-arena")
                }
    }

    @Subcommand("forcestart <arena>")
    fun forceStart(actor: BukkitCommandActor, arena: String) {
        val sender = actor.sender()

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the player has permission
            if (!sender.hasPermission("disasters.forcestart")) {
                Msg.send(sender, "messages.no-permission")
                return
            }
            it.start()
        }
                ?: run {
                    // If the arena does not exist
                    Msg.send(sender, "messages.arena-not-found")
                }
    }

    @Subcommand("arena forcestop <arena>")
    fun forceStop(actor: BukkitCommandActor, arena: String) {
        val sender = actor.sender()

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the player has permission
            if (!sender.hasPermission("disasters.forcestop")) {
                Msg.send(sender, "messages.no-permission")
                return
            }
            it.stop()
        }
                ?: run {
                    // If the arena does not exist
                    Msg.send(sender, "messages.arena-not-found")
                }
    }
}
