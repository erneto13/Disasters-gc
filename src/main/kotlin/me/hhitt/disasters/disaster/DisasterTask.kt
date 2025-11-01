package me.hhitt.disasters.disaster

import org.bukkit.scheduler.BukkitRunnable

/** DisasterTask is a BukkitRunnable that pulses all disasters every second. */
class DisasterTask : BukkitRunnable() {
    private var time = 0

    override fun run() {
        DisasterRegistry.pulseAll(time)
        time++
        if (time >= 10000) {
            time = 0
        }
    }
}
