package me.hhitt.disasters.disaster

import kotlin.reflect.KClass
import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.configuration.file.FileConfiguration

object DisasterConfig {
    private lateinit var config: FileConfiguration
    private val exclusions = mutableMapOf<String, Set<String>>()
    private val priorities = mutableMapOf<String, Priority>()
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

    private fun createDefaultConfig() {}

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

    fun getAvailableDisastersByTime(
            allDisasters: List<KClass<out Disaster>>,
            gameTimePercent: Int
    ): List<KClass<out Disaster>> {
        val targetPriority =
                when {
                    gameTimePercent < 30 -> Priority.LOW
                    gameTimePercent < 70 -> Priority.MEDIUM
                    else -> Priority.HIGH
                }

        return allDisasters.filter { disaster ->
            isEnabled(disaster) &&
                    (getPriority(disaster) == targetPriority ||
                            (targetPriority == Priority.MEDIUM &&
                                    getPriority(disaster) == Priority.LOW) ||
                            (targetPriority == Priority.HIGH &&
                                    getPriority(disaster) != Priority.HIGH))
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
