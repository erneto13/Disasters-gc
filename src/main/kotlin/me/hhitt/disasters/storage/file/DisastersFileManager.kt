package me.hhitt.disasters.storage.file

import java.io.File
import java.util.logging.Level
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.util.Filer.fixName
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

object DisasterFileManager {

    private val disasterConfigs = mutableMapOf<String, Configuration>()
    private lateinit var plugin: JavaPlugin
    private lateinit var disastersFolder: File

    fun initialize() {
        this.plugin = Disasters.getInstance()
        this.disastersFolder = File(plugin.dataFolder, "config")

        if (!disastersFolder.exists()) {
            disastersFolder.mkdirs()
        }

        loadDisasterConfigs()
    }

    private fun loadDisasterConfigs() {
        createDefaultDisasterConfig("meteor-shower")
        createDefaultDisasterConfig("acid-rain")
        createDefaultDisasterConfig("floor-is-lava")
        createDefaultDisasterConfig("dragons")

        val disasterFiles =
                disastersFolder.listFiles { _, name -> name.endsWith(".yml", ignoreCase = true) }
                        ?: return

        for (file in disasterFiles) {
            val disasterName = file.nameWithoutExtension
            loadDisasterConfig(disasterName)
        }
    }

    private fun createDefaultDisasterConfig(disasterName: String) {
        val fileName = fixName(disasterName)
        val file = File(disastersFolder, fileName)

        if (!file.exists()) {
            when (disasterName.lowercase()) {
                "meteor-shower" -> createMeteorShowerConfig(file)
                else -> {
                    plugin.logger.info("No default config template for disaster: $disasterName")
                }
            }
        }
    }

    private fun createMeteorShowerConfig(file: File) {
        val config = YamlConfiguration()

        // spawn settings
        config.set("spawn-rate", 5)

        config.set("initial-intensity", 10)

        config.set("max-intensity", 40)

        config.set("intensity-increase-rate", 60)

        config.set("intensity-increase-amount", 2)

        // spawn settings
        config.set("spawn-distance", 30.0)

        config.set("lateral-offset-multiplier", 2.0)

        config.set("spawn-height-min", 20.0)

        config.set("spawn-height-max", 60.0)

        // meteor settings
        config.set("min-size", 0.8)

        config.set("max-size", 2.5)

        config.set("explosion-multiplier", 3.5)

        // physics settings
        config.set("fall-speed", 0.4)

        config.set("downward-force", -0.6)

        config.set("max-lifetime-ticks", 300)

        config.set("config-version", 1)

        try {
            config.save(file)
            plugin.logger.info("Created default config for MeteorShower at: ${file.path}")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to create MeteorShower config", e)
        }
    }

    private fun loadDisasterConfig(disasterName: String) {
        val fileName = fixName(disasterName)
        val file = File(disastersFolder, fileName)

        if (!file.exists()) {
            return
        }

        val config = Configuration(disastersFolder, fileName)
        disasterConfigs[disasterName.lowercase()] = config
        plugin.logger.info("Loaded disaster config: $disasterName")
    }

    fun getDisasterConfig(disasterName: String): Configuration? {
        return disasterConfigs[disasterName.lowercase()]
    }

    fun reloadDisasterConfig(disasterName: String) {
        val config = disasterConfigs[disasterName.lowercase()]
        config?.reloadFile()
    }

    fun reloadAll() {
        disasterConfigs.values.forEach { it.reloadFile() }
    }

    fun getConfigValue(disasterName: String, key: String): Any? {
        return getDisasterConfig(disasterName)?.get(key)
    }
}
