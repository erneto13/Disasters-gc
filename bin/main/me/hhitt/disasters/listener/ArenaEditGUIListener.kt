package me.hhitt.disasters.listener

import me.hhitt.disasters.gui.ArenaEditGUI
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class ArenaEditGUIListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val gui = ArenaEditGUI.getGUI(event.inventory) ?: return

        event.isCancelled = true

        val player = event.whoClicked as? Player ?: return
        val slot = event.rawSlot

        //just manage clicks in the GUI inventory (not in the player's inventory)
        if (slot < 0 || slot >= event.inventory.size) return

        gui.handleClick(player, slot)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val gui = ArenaEditGUI.getGUI(event.inventory) ?: return
        gui.handleClose()
    }
}