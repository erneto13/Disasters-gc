package me.hhitt.disasters.util

import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

/**
 * Manages the creation and validation of the arena selection tool.
 *
 * This class is responsible for:
 * - Creating the golden axe used for arena corner selection
 * - Validating if an item is a selection tool
 */
object SelectionTool {

    private const val TOOL_NAME = "Arena Selection Wand"

    /**
     * Creates a new selection axe with proper enchantments and lore.
     */
    fun createSelectionAxe(): ItemStack {
        val axe = ItemStack(Material.GOLDEN_AXE)
        val meta = axe.itemMeta!!

        meta.displayName(Msg.parse("<#FF8800>$TOOL_NAME"))
        meta.lore(listOf(
            Msg.parse("<#c1c1c1>Left click: Set Corner 1"),
            Msg.parse("<#c1c1c1>Right click: Set Corner 2")
        ))

        meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.isUnbreakable = true
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)

        axe.itemMeta = meta
        return axe
    }

    /**
     * Checks if the given item is a selection axe.
     */
    fun isSelectionAxe(item: ItemStack): Boolean {
        if (item.type != Material.GOLDEN_AXE) return false
        val meta = item.itemMeta ?: return false
        val name = meta.displayName() ?: return false
        return (name as? TextComponent)?.content()?.contains(TOOL_NAME) == true
    }

    /**
     * Removes all selection axes from a player's inventory.
     */
    fun removeSelectionAxe(inventory: PlayerInventory) {
        inventory.contents.forEach { item ->
            if (item != null && isSelectionAxe(item)) {
                inventory.remove(item)
            }
        }
    }
}