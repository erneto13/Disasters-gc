package me.hhitt.disasters.util

import java.util.UUID
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object Lobby {

    /*
     * May this class is not the best way to set the lobby location,
     * but is really helpful.
     */

    private var location: Location = Location(null, 0.0, 0.0, 0.0)

    // inventory snapshot per player to restore after leaving an arena
    private data class InventorySnapshot(
            val contents: Array<ItemStack?>,
            val armor: Array<ItemStack?>,
            val offhand: ItemStack?
    )

    private val inventorySnapshots = mutableMapOf<UUID, InventorySnapshot>()

    fun setLocation() {
        val loc =
                Location(
                        Bukkit.getWorld(FileManager.get("config")!!.getString("lobby.world")!!),
                        FileManager.get("config")!!.getDouble("lobby.x"),
                        FileManager.get("config")!!.getDouble("lobby.y"),
                        FileManager.get("config")!!.getDouble("lobby.z"),
                        FileManager.get("config")!!.getDouble("lobby.yaw").toFloat(),
                        FileManager.get("config")!!.getDouble("lobby.pitch").toFloat()
                )
        this.location = loc
    }

    fun savePlayerState(player: Player) {
        val inv = player.inventory
        val snapshot =
                InventorySnapshot(
                        inv.contents.map { it?.clone() }.toTypedArray(),
                        inv.armorContents.map { it?.clone() }.toTypedArray(),
                        inv.itemInOffHand.clone()
                )
        inventorySnapshots[player.uniqueId] = snapshot
    }

    /**
     * Restores player's saved inventory if present.
     * @return true if restored, false if there was nothing to restore
     */
    fun restorePlayerState(player: Player): Boolean {
        val snapshot = inventorySnapshots.remove(player.uniqueId) ?: return false
        val inv = player.inventory
        inv.clear()
        inv.contents = snapshot.contents
        inv.armorContents = snapshot.armor
        inv.setItemInOffHand(snapshot.offhand)
        return true
    }

    fun teleportPlayer(player: Player) {
        val maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH)
        maxHealthAttribute?.baseValue = 20.0

        player.teleport(location)
        player.activePotionEffects.clear()

        if (!restorePlayerState(player)) {
            player.inventory.clear()
        }

        player.health = 20.0
        player.foodLevel = 20
        player.saturation = 20.0f
        player.level = 0
        player.exp = 0.0f
        player.gameMode = GameMode.SURVIVAL
        player.activePotionEffects.clear()
    }

    fun teleportAtEnd(arena: Arena) {
        Bukkit.getScheduler()
                .runTaskLater(
                        Disasters.getInstance(),
                        Runnable {
                            arena.playing.forEach { teleportPlayer(it) }
                            arena.clear()
                        },
                        60L
                )
    }
}
