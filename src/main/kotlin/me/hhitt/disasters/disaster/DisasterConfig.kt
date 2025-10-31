package me.hhitt.disasters.disaster

import kotlin.reflect.KClass
import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.configuration.file.FileConfiguration

/**
 * DisasterConfig manages disaster configuration including priorities and exclusions. Disasters are
 * organized by priority levels:
 * - LOW: Light disasters that happen first (60-100% game time)
 * - MEDIUM: Normal disasters (40-60% game time)
 * - HIGH: Destructive disasters that happen last (15-20% game time)
 */
object DisasterConfig {

    private lateinit var config: FileConfiguration

    // map of exclusions between disasters
    private val exclusions = mutableMapOf<String, Set<String>>()

    // map of disaster name to priority
    private val priorities = mutableMapOf<String, Priority>()

    // map of disabled disasters
    private val disabledDisasters = mutableSetOf<String>()

    fun load() {
        config =
                FileManager.get("disasters")
                        ?: run {
                            createDefaultConfig()
                            FileManager.get("disasters")!!
                        }

        loadExclusions()
        loadPriorities()
        loadDisabledDisasters()
    }

    private fun createDefaultConfig() {
        // file will be created with default values
    }

    private fun loadExclusions() {
        exclusions.clear()
        val exclusionsSection = config.getConfigurationSection("exclusions") ?: return

        for (key in exclusionsSection.getKeys(false)) {
            val excluded = config.getStringList("exclusions.$key").toSet()
            exclusions[key.lowercase()] = excluded.map { it.lowercase() }.toSet()
        }
    }

    private fun loadPriorities() {
        priorities.clear()
        val prioritiesSection = config.getConfigurationSection("priorities") ?: return

        for (priority in Priority.values()) {
            val disasters = config.getStringList("priorities.${priority.name.lowercase()}")
            disasters.forEach { disaster -> priorities[disaster.lowercase()] = priority }
        }
    }

    private fun loadDisabledDisasters() {
        disabledDisasters.clear()
        val disabled = config.getStringList("disabled-disasters")
        disabledDisasters.addAll(disabled.map { it.lowercase() })
    }

    fun isEnabled(disasterClass: KClass<out Disaster>): Boolean {
        val name = disasterClass.simpleName?.lowercase() ?: return false
        return !disabledDisasters.contains(name)
    }

    fun getPriority(disasterClass: KClass<out Disaster>): Priority {
        val name = disasterClass.simpleName?.lowercase() ?: return Priority.MEDIUM
        return priorities[name] ?: Priority.MEDIUM
    }

    fun areCompatible(disaster1: KClass<out Disaster>, disaster2: KClass<out Disaster>): Boolean {
        val name1 = disaster1.simpleName?.lowercase() ?: return true
        val name2 = disaster2.simpleName?.lowercase() ?: return true

        val excluded1 = exclusions[name1] ?: emptySet()
        val excluded2 = exclusions[name2] ?: emptySet()

        return !excluded1.contains(name2) && !excluded2.contains(name1)
    }

    fun isCompatibleWithActive(
            disasterClass: KClass<out Disaster>,
            activeDisasters: List<Disaster>
    ): Boolean {
        return activeDisasters.all { active -> areCompatible(disasterClass, active::class) }
    }

    /**
     * filter available disasters based on game time and priority
     * @param gameTimePercent percentage of game time elapsed (0-100)
     */
    fun getAvailableDisastersByTime(
            allDisasters: List<KClass<out Disaster>>,
            gameTimePercent: Int
    ): List<KClass<out Disaster>> {
        val targetPriority =
                when {
                    gameTimePercent < 30 -> Priority.HIGH // 0-30% = destructive disasters
                    gameTimePercent < 70 -> Priority.MEDIUM // 30-70% = normal disasters
                    else -> Priority.LOW // 70-100% = light disasters
                }

        return allDisasters.filter { disaster ->
            isEnabled(disaster) &&
                    (getPriority(disaster) == targetPriority ||
                            // allow higher priority disasters as well (from more destructive to
                            // less)
                            (targetPriority == Priority.MEDIUM &&
                                    getPriority(disaster) == Priority.HIGH) ||
                            (targetPriority == Priority.LOW &&
                                    getPriority(disaster) != Priority.LOW))
        }
    }

    fun getDisasterConfig(disasterClass: KClass<out Disaster>, key: String): Any? {
        val name = disasterClass.simpleName?.lowercase() ?: return null
        return config.get("disaster-settings.$name.$key")
    }

    fun reload() {
        FileManager.reload("disasters")
        load()
    }
}
