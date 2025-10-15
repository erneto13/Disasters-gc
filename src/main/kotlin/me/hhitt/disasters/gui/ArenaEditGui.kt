package me.hhitt.disasters.gui

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.util.Msg
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File

class ArenaEditGUI(
    private val arena: Arena,
    private val arenaManager: ArenaManager
) {

    private lateinit var inventory: Inventory
    private val arenaFile = File("plugins/Disasters/Arenas/${arena.name}.yml")
    private val config = YamlConfiguration.loadConfiguration(arenaFile)

    //companion object to manage active GUIs
    companion object {
        private val activeGUIs = mutableMapOf<Inventory, ArenaEditGUI>()

        fun getGUI(inventory: Inventory): ArenaEditGUI? = activeGUIs[inventory]

        fun removeGUI(inventory: Inventory) {
            activeGUIs.remove(inventory)
        }
    }

    fun open(player: Player) {
        val title = Msg.parse(
            Msg.getMsg("arena-edit.title")
                .replace("%arena%", arena.name)
        )
        inventory = Bukkit.createInventory(null, 54, title)

        setupItems()

        //register this GUI as active
        activeGUIs[inventory] = this

        player.openInventory(inventory)
    }

    private fun setupItems() {
        //max players
        inventory.setItem(
            10, createConfigItem(
                Material.PLAYER_HEAD,
                "gui.items.max-players.name",
                "gui.items.max-players.lore",
                "value" to config.getInt("max-players").toString()
            )
        )

        //min players
        inventory.setItem(
            11, createConfigItem(
                Material.SKELETON_SKULL,
                "gui.items.min-players.name",
                "gui.items.min-players.lore",
                "value" to config.getInt("min-players").toString()
            )
        )

        //alive to end
        inventory.setItem(
            12, createConfigItem(
                Material.TOTEM_OF_UNDYING,
                "gui.items.alive-to-end.name",
                "gui.items.alive-to-end.lore",
                "value" to config.getInt("alive-to-end").toString()
            )
        )

        //countdown
        inventory.setItem(
            13, createConfigItem(
                Material.CLOCK,
                "gui.items.countdown.name",
                "gui.items.countdown.lore",
                "value" to config.getInt("countdown").toString()
            )
        )

        //game time
        inventory.setItem(
            14, createConfigItem(
                Material.REDSTONE_TORCH,
                "gui.items.game-time.name",
                "gui.items.game-time.lore",
                "value" to config.getInt("game-time").toString()
            )
        )

        //disaster rate
        inventory.setItem(
            15, createConfigItem(
                Material.TNT,
                "gui.items.disaster-rate.name",
                "gui.items.disaster-rate.lore",
                "value" to config.getInt("disaster-rate").toString()
            )
        )

        //max disasters
        inventory.setItem(
            16, createConfigItem(
                Material.FIRE_CHARGE,
                "gui.items.max-disasters.name",
                "gui.items.max-disasters.lore",
                "value" to config.getInt("max-disasters").toString()
            )
        )

        //display name
        inventory.setItem(
            20, createConfigItem(
                Material.NAME_TAG,
                "gui.items.display-name.name",
                "gui.items.display-name.lore",
                "value" to (config.getString("display-name") ?: "N/A")
            )
        )

        //spawn location
        inventory.setItem(
            21, createConfigItem(
                Material.COMPASS,
                "gui.items.spawn-location.name",
                "gui.items.spawn-location.lore",
                "world" to (config.getString("spawn.world") ?: "N/A"),
                "x" to config.getDouble("spawn.x").toInt().toString(),
                "y" to config.getDouble("spawn.y").toInt().toString(),
                "z" to config.getDouble("spawn.z").toInt().toString()
            )
        )

        //corner 1
        inventory.setItem(
            22, createConfigItem(
                Material.EMERALD_BLOCK,
                "gui.items.corner1.name",
                "gui.items.corner1.lore",
                "world" to (config.getString("corner1.world") ?: "N/A"),
                "x" to config.getInt("corner1.x").toString(),
                "y" to config.getInt("corner1.y").toString(),
                "z" to config.getInt("corner1.z").toString()
            )
        )

        //corner 2
        inventory.setItem(
            23, createConfigItem(
                Material.REDSTONE_BLOCK,
                "gui.items.corner2.name",
                "gui.items.corner2.lore",
                "world" to (config.getString("corner2.world") ?: "N/A"),
                "x" to config.getInt("corner2.x").toString(),
                "y" to config.getInt("corner2.y").toString(),
                "z" to config.getInt("corner2.z").toString()
            )
        )

        //winner commands
        inventory.setItem(
            30, createConfigItem(
                Material.GOLD_INGOT,
                "gui.items.winner-commands.name",
                "gui.items.winner-commands.lore"
            )
        )

        //loser commands
        inventory.setItem(
            31, createConfigItem(
                Material.COAL,
                "gui.items.loser-commands.name",
                "gui.items.loser-commands.lore"
            )
        )

        //all commands
        inventory.setItem(
            32, createConfigItem(
                Material.PAPER,
                "gui.items.all-commands.name",
                "gui.items.all-commands.lore"
            )
        )

        //save and close
        inventory.setItem(
            49, createConfigItem(
                Material.LIME_CONCRETE,
                "gui.items.save.name",
                "gui.items.save.lore"
            )
        )

        //save without saving
        inventory.setItem(
            48, createConfigItem(
                Material.RED_CONCRETE,
                "gui.items.close.name",
                "gui.items.close.lore"
            )
        )
    }

    fun handleClick(player: Player, slot: Int) {
        when (slot) {
            10 -> openNumberInput(player, "max-players", "Max Players")
            11 -> openNumberInput(player, "min-players", "Min Players")
            12 -> openNumberInput(player, "alive-to-end", "Alive to End")
            13 -> openNumberInput(player, "countdown", "Countdown")
            14 -> openNumberInput(player, "game-time", "Game Time")
            15 -> openNumberInput(player, "disaster-rate", "Disaster Rate")
            16 -> openNumberInput(player, "max-disasters", "Max Disasters")
            20 -> openTextInput(player, "display-name", "Display Name")
            21 -> teleportToSpawn(player)
            22 -> teleportToCorner1(player)
            23 -> teleportToCorner2(player)
            30 -> openCommandsEditor(player, "winners")
            31 -> openCommandsEditor(player, "losers")
            32 -> openCommandsEditor(player, "to-all")
            48 -> {
                player.closeInventory()
                Msg.send(player, "arena-edit.changes-discarded")
            }

            49 -> {
                saveChanges()
                player.closeInventory()
                Msg.send(player, "arena-edit.changes-saved")
            }
        }
    }

    fun handleClose() {
        activeGUIs.remove(inventory)
    }

    private fun openNumberInput(player: Player, path: String, displayName: String) {
        player.closeInventory()
        Msg.send(player, "arena-edit.input-number", "field" to displayName)
        Msg.send(player, "arena-edit.current-value", "value" to config.getInt(path).toString())
        Msg.send(player, "arena-edit.cancel-hint")

        ChatInputManager.requestInput(player, path) { input ->
            if (input.equals("cancel", ignoreCase = true)) {
                Msg.send(player, "arena-edit.cancelled")
                open(player)
                return@requestInput
            }

            val value = input.toIntOrNull()
            if (value != null) {
                config.set(path, value)
                Msg.send(
                    player, "arena-edit.field-updated",
                    "field" to displayName,
                    "value" to value.toString()
                )
                open(player)
            } else {
                Msg.send(player, "arena-edit.invalid-number")
                open(player)
            }
        }
    }

    private fun openTextInput(player: Player, path: String, displayName: String) {
        player.closeInventory()
        Msg.send(player, "arena-edit.input-text", "field" to displayName)
        Msg.send(player, "arena-edit.current-value", "value" to (config.getString(path) ?: "N/A"))
        Msg.send(player, "arena-edit.cancel-hint")

        ChatInputManager.requestInput(player, path) { input ->
            if (input.equals("cancel", ignoreCase = true)) {
                Msg.send(player, "arena-edit.cancelled")
                open(player)
                return@requestInput
            }

            config.set(path, input)
            Msg.send(
                player, "arena-edit.field-updated",
                "field" to displayName,
                "value" to input
            )
            open(player)
        }
    }

    private fun openCommandsEditor(player: Player, type: String) {
        player.closeInventory()
        val commands = config.getStringList("commands.$type")

        Msg.send(player, "arena-edit.commands-current", "type" to type)
        commands.forEachIndexed { index, cmd ->
            Msg.sendParsed(player, "<gray>$index. <yellow>$cmd")
        }
        Msg.send(player, "arena-edit.commands-add-hint")
        Msg.send(player, "arena-edit.commands-remove-hint")
        Msg.send(player, "arena-edit.commands-done-hint")
    }

    private fun teleportToSpawn(player: Player) {
        val world = Bukkit.getWorld(config.getString("spawn.world")!!)
        val x = config.getDouble("spawn.x")
        val y = config.getDouble("spawn.y")
        val z = config.getDouble("spawn.z")
        val yaw = config.getDouble("spawn.yaw").toFloat()
        val pitch = config.getDouble("spawn.pitch").toFloat()

        player.teleport(Location(world, x, y, z, yaw, pitch))
        Msg.playSound(player, "ENTITY_ENDERMAN_TELEPORT")
        Msg.send(player, "arena-edit.teleported-spawn")
    }

    private fun teleportToCorner1(player: Player) {
        val world = Bukkit.getWorld(config.getString("corner1.world")!!)
        val x = config.getInt("corner1.x").toDouble()
        val y = config.getInt("corner1.y").toDouble()
        val z = config.getInt("corner1.z").toDouble()

        player.teleport(Location(world, x, y, z))
        Msg.playSound(player, "ENTITY_ENDERMAN_TELEPORT")
        Msg.send(player, "arena-edit.teleported-corner1")
    }

    private fun teleportToCorner2(player: Player) {
        val world = Bukkit.getWorld(config.getString("corner2.world")!!)
        val x = config.getInt("corner2.x").toDouble()
        val y = config.getInt("corner2.y").toDouble()
        val z = config.getInt("corner2.z").toDouble()

        player.teleport(Location(world, x, y, z))
        Msg.playSound(player, "ENTITY_ENDERMAN_TELEPORT")
        Msg.send(player, "arena-edit.teleported-corner2")
    }

    private fun saveChanges() {
        config.save(arenaFile)
        arenaManager.reloadArenas()
    }

    //function to create an item from the config with placeholders replaced
    private fun createConfigItem(
        material: Material,
        namePath: String,
        lorePath: String,
        vararg replacements: Pair<String, String>
    ): ItemStack {
        //representation of the item
        val item = ItemStack(material)
        val meta = item.itemMeta!!

        var name = Msg.getMsg(namePath)
        replacements.forEach { (key, value) ->
            name = name.replace("%$key%", value)
        }
        meta.displayName(Msg.parse(name))

        //lore with replacements
        val loreList = Msg.getMsgList(lorePath)
        val parsedLore = loreList.map { line ->
            var parsedLine = line
            replacements.forEach { (key, value) ->
                parsedLine = parsedLine.replace("%$key%", value)
            }
            Msg.parse(parsedLine)
        }
        meta.lore(parsedLore)

        item.itemMeta = meta
        return item
    }
}

//ChatInputManager object to handle chat inputs
object ChatInputManager {
    //map of players to a pair of context string and callback function
    private val waitingInput = mutableMapOf<Player, Pair<String, (String) -> Unit>>()

    //context can be used to identify what the input is for
    fun requestInput(player: Player, context: String, callback: (String) -> Unit) {
        waitingInput[player] = context to callback
    }

    //returns true if the input was handled, false otherwise
    fun handleInput(player: Player, message: String): Boolean {
        val pair = waitingInput.remove(player) ?: return false
        pair.second(message)
        return true
    }

    //check if a player is currently waiting for input
    fun isWaiting(player: Player) = waitingInput.containsKey(player)
}