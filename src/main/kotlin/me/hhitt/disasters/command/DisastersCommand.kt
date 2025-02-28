package me.hhitt.disasters.command

import me.hhitt.disasters.storage.file.FileManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("disasters")
@CommandPermission("disasters.admin")
class DisastersCommand {

    @Subcommand("reload")
    fun reload() {
        FileManager.get("config")!!.reloadFile()
        FileManager.get("lang")!!.reloadFile()
    }
}