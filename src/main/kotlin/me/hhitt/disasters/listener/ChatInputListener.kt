package me.hhitt.disasters.listener

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.gui.ChatInputManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

/** ChatInputListener handles player chat events to capture input for various in-game prompts.
 *
 * This listener checks if a player is expected to provide input via chat and processes
 * that input accordingly. Since AsyncPlayerChatEvent runs asynchronously, any API calls
 * that modify game state must be scheduled on the main thread.
 */
class ChatInputListener : Listener {

    private val plugin = Disasters.getInstance()

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        //check if this player is waiting to provide input
        if (!ChatInputManager.isWaiting(player)) return

        //cancel the event so the message doesn't appear in chat
        event.isCancelled = true

        //schedule processing on the main thread for thread safety
        plugin.server.scheduler.runTask(plugin, Runnable {
            ChatInputManager.handleInput(player, event.message)
        })
    }
}