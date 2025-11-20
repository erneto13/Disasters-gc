package me.hhitt.disasters.disaster

import org.bukkit.scheduler.BukkitRunnable

class DisasterTask : BukkitRunnable() {
    private var time = 0

    override fun run() {
        try {
            DisasterRegistry.pulseAll(time)
            time++
            if (time >= 10000) {
                time = 0
            }
        } catch (e: Exception) {
            me.hhitt.disasters.Disasters.getInstance()
                    .logger
                    .severe("Critical error in DisasterTask: ${e.message}")
            e.printStackTrace()
        }
    }
}
