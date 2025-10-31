package me.hhitt.disasters.command

import me.hhitt.disasters.disaster.DisasterConfig
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.util.Msg
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

/** Comando para administrar la configuración de desastres. */
@Command("dg disasters")
@CommandPermission("disasters.admin")
class DisasterAdminCommand {

    @Subcommand("list")
    fun list(actor: BukkitCommandActor) {
        val sender = actor.sender()

        Msg.sendParsed(
                sender as Player,
                "<gold>========== <yellow>Desastres Disponibles <gold>=========="
        )

        val allDisasters =
                listOf(
                        "AcidRain",
                        "Apocalypse",
                        "Blind",
                        "Wither",
                        "AllowFight",
                        "BlockDisappear",
                        "HotSun",
                        "Murder",
                        "ZeroGravity",
                        "ExplosiveSheep",
                        "FloorIsLava",
                        "Grounded",
                        "Lightning",
                        "OneHearth",
                        "Swap",
                        "WorldBorder",
                        "NoJump"
                )

        for (disasterName in allDisasters) {
            val disasterClass = DisasterRegistry.getDisasterClassByName(disasterName)
            if (disasterClass != null) {
                val enabled = if (DisasterConfig.isEnabled(disasterClass)) "<green>✓" else "<red>✗"
                val priority = DisasterConfig.getPriority(disasterClass).name
                val color =
                        when (priority) {
                            "LOW" -> "<gray>"
                            "MEDIUM" -> "<yellow>"
                            "HIGH" -> "<red>"
                            else -> "<white>"
                        }

                Msg.sendParsed(sender, "$enabled $color$disasterName <dark_gray>[$priority]")
            }
        }

        Msg.sendParsed(sender, "<gold>=====================================")
    }

    @Subcommand("info")
    fun info(actor: BukkitCommandActor, @Named("disaster") disasterName: String) {
        val sender = actor.sender()
        val disasterClass = DisasterRegistry.getDisasterClassByName(disasterName)

        if (disasterClass == null) {
            Msg.sendParsed(sender as Player, "<red>Desastre '$disasterName' no encontrado!")
            return
        }

        val enabled =
                if (DisasterConfig.isEnabled(disasterClass)) "<green>Habilitado"
                else "<red>Deshabilitado"
        val priority = DisasterConfig.getPriority(disasterClass).name

        Msg.sendParsed(sender as Player, "<gold>========== <yellow>$disasterName <gold>==========")
        Msg.sendParsed(sender, "<gray>Estado: $enabled")
        Msg.sendParsed(sender, "<gray>Prioridad: <yellow>$priority")

        // Mostrar incompatibilidades
        val allDisasters =
                listOf(
                        "AcidRain",
                        "Apocalypse",
                        "Blind",
                        "Wither",
                        "AllowFight",
                        "BlockDisappear",
                        "HotSun",
                        "Murder",
                        "ZeroGravity",
                        "ExplosiveSheep",
                        "FloorIsLava",
                        "Grounded",
                        "Lightning",
                        "OneHearth",
                        "Swap",
                        "WorldBorder",
                        "NoJump"
                )

        val incompatible =
                allDisasters.mapNotNull { name ->
                    val otherClass = DisasterRegistry.getDisasterClassByName(name)
                    if (otherClass != null &&
                                    otherClass != disasterClass &&
                                    !DisasterConfig.areCompatible(disasterClass, otherClass)
                    ) {
                        name
                    } else null
                }

        if (incompatible.isNotEmpty()) {
            Msg.sendParsed(
                    sender,
                    "<gray>Incompatible con: <red>${incompatible.joinToString(", ")}"
            )
        } else {
            Msg.sendParsed(sender, "<gray>Sin incompatibilidades")
        }

        Msg.sendParsed(sender, "<gold>=====================================")
    }

    @Subcommand("priorities")
    fun priorities(actor: BukkitCommandActor) {
        val sender = actor.sender()

        Msg.sendParsed(
                sender as Player,
                "<gold>========== <yellow>Prioridades de Desastres <gold>=========="
        )
        Msg.sendParsed(sender, "")
        Msg.sendParsed(sender, "<gray>LOW (0-30% del juego) - Desastres ligeros:")
        Msg.sendParsed(sender, "  <white>Blind, NoJump, Grounded, Lag, ZeroGravity, Swap, Cobweb")
        Msg.sendParsed(sender, "")
        Msg.sendParsed(sender, "<yellow>MEDIUM (30-70% del juego) - Desastres normales:")
        Msg.sendParsed(
                sender,
                "  <white>HotSun, Lightning, AllowFight, Murder, BlockDisappear, OneHearth"
        )
        Msg.sendParsed(sender, "")
        Msg.sendParsed(sender, "<red>HIGH (70-100% del juego) - Desastres destructivos:")
        Msg.sendParsed(
                sender,
                "  <white>AcidRain, Apocalypse, ExplosiveSheep, FloorIsLava, WorldBorder, Wither"
        )
        Msg.sendParsed(sender, "")
        Msg.sendParsed(
                sender,
                "<gray>Edita <yellow>disasters.yml <gray>para modificar las prioridades"
        )
        Msg.sendParsed(sender, "<gold>=============================================")
    }

    @Subcommand("reload")
    fun reloadDisasters(actor: BukkitCommandActor) {
        DisasterRegistry.reloadConfig()
        Msg.sendParsed(actor.sender() as Player, "<green>Configuración de desastres recargada!")
    }
}
