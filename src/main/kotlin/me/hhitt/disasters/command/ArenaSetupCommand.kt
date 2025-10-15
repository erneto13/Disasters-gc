package me.hhitt.disasters.command

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.arena.ArenaSetupManager
import me.hhitt.disasters.arena.ArenaValidator
import me.hhitt.disasters.gui.ArenaEditGUI
import me.hhitt.disasters.util.Msg
import me.hhitt.disasters.util.SelectionTool
import me.hhitt.disasters.visual.CuboidVisualizer
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

/**
 * Command handler for arena setup and management.
 *
 * Responsibilities:
 * - Handle command execution and permission checks
 * - Delegate business logic to appropriate managers
 * - Send feedback messages to players
 */
@Command("arena")
@CommandPermission("disasters.admin")
class ArenaSetupCommand(
    private val arenaManager: ArenaManager,
    private val setupManager: ArenaSetupManager,
    private val visualizer: CuboidVisualizer
) {

    @Command("arena setup")
    fun setup(actor: BukkitCommandActor, @Named("arena") arenaName: String) {
        if (!actor.isPlayer) {
            Msg.send(actor.sender(), "only-players")
            return
        }

        val player = actor.asPlayer()!!

        if (!ArenaValidator.isValidArenaName(arenaName)) {
            Msg.send(player, "arena-setup.invalid-arena-name")
            return
        }

        if (arenaManager.getArena(arenaName) != null) {
            Msg.send(player, "arena-setup.arena-already-exists")
            return
        }

        setupManager.createSession(player, arenaName)

        val axe = SelectionTool.createSelectionAxe()
        player.inventory.addItem(axe)

        Msg.send(player, "arena-setup.setup-activated", "arena" to arenaName)
        Msg.sendList(player, "arena-setup.setup-instructions-axe")
    }

    @Command("arena setspawn")
    fun setSpawn(actor: BukkitCommandActor) {
        if (!actor.isPlayer) {
            Msg.send(actor.sender(), "only-players")
            return
        }

        val player = actor.asPlayer()!!
        val session = setupManager.getSession(player)

        if (session == null) {
            Msg.send(player, "arena-setup.not-in-setup")
            return
        }

        session.spawn = player.location
        Msg.send(player, "arena-setup.arena-set-spawn-success")

        if (session.corner1 != null && session.corner2 != null) {
            val error = ArenaValidator.validateSpawnLocation(session)
            if (error != null) {
                Msg.send(player, "arena-setup.spawn-warning", "reason" to error)
            }
        }
    }

    @Command("arena save")
    fun save(actor: BukkitCommandActor) {
        if (!actor.isPlayer) {
            Msg.send(actor.sender(), "only-players")
            return
        }

        val player = actor.asPlayer()!!
        val session = setupManager.getSession(player)

        if (session == null) {
            Msg.send(player, "arena-setup.not-arena-setup")
            return
        }

        if (!ArenaValidator.validateSession(session, player)) {
            return
        }

        val boundsError = ArenaValidator.validateArenaBounds(session)
        if (boundsError != null) {
            Msg.send(player, "arena-setup.invalid-bounds", "reason" to boundsError)
            return
        }

        val spawnError = ArenaValidator.validateSpawnLocation(session)
        if (spawnError != null) {
            Msg.send(player, "arena-setup.invalid-spawn", "reason" to spawnError)
            return
        }

        setupManager.saveArena(session)
        setupManager.removeSession(player)

        visualizer.stopVisualization(player.name)
        SelectionTool.removeSelectionAxe(player.inventory)

        Msg.send(player, "arena-setup.arena-saved", "arena" to session.arenaName)
        Msg.send(player, "arena-setup.edit-instructions", "arena" to session.arenaName)

        arenaManager.reloadArenas()
    }

    @Command("arena cancel")
    fun cancel(actor: BukkitCommandActor) {
        if (!actor.isPlayer) {
            Msg.send(actor.sender(), "only-players")
            return
        }

        val player = actor.asPlayer()!!
        val session = setupManager.getSession(player)

        if (session == null) {
            Msg.send(player, "arena-setup.not-arena-setup")
            return
        }

        setupManager.removeSession(player)
        visualizer.stopVisualization(player.name)
        SelectionTool.removeSelectionAxe(player.inventory)

        Msg.send(player, "arena-setup.cancel-arena")
    }

    @Command("arena edit")
    fun edit(actor: BukkitCommandActor, @Named("arena") arenaName: String) {
        if (!actor.isPlayer) {
            Msg.send(actor.sender(), "only-players")
            return
        }

        val player = actor.asPlayer()!!
        val arena = arenaManager.getArena(arenaName)

        if (arena == null) {
            Msg.send(player, "arena-setup.arena-not-found")
            return
        }

        ArenaEditGUI(arena, arenaManager).open(player)
    }

    @Command("arena delete")
    fun delete(actor: BukkitCommandActor, @Named("arena") arenaName: String) {
        if (!actor.isPlayer) {
            Msg.send(actor.sender(), "only-players")
            return
        }

        val player = actor.asPlayer()!!
        val arena = arenaManager.getArena(arenaName)

        if (arena == null) {
            Msg.send(player, "arena-setup.arena-not-found")
            return
        }

        arenaManager.removeArena(arenaName)
        Msg.send(player, "arena-setup.arena-deleted", "arena" to arenaName)
    }

    @Command("arena list")
    fun list(actor: BukkitCommandActor) {
        val arenas = arenaManager.getArenas()

        if (arenas.isEmpty()) {
            Msg.send(actor.sender(), "no-arenas")
            return
        }

        val player = actor.asPlayer()!!

        Msg.send(player, "arena-setup.available-arenas")
        arenas.forEach { arena ->
            Msg.send(
                player,
                "arena-setup.arena-list-entry",
                "arena" to arena.name,
                "current" to arena.playing.size.toString(),
                "max" to arena.maxPlayers.toString()
            )
        }
    }
}