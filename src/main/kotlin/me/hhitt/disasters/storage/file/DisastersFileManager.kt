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
        this.disastersFolder = File(plugin.dataFolder, "DisastersConfigs")

        if (!disastersFolder.exists()) {
            disastersFolder.mkdirs()
        }

        loadDisasterConfigs()
    }

    private fun loadDisasterConfigs() {
        createDefaultDisasterConfig("meteor-shower")

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
        config.addComment(
                "spawn-rate",
                "Cada cuántos ticks se generan meteoritos (20 ticks = 1 segundo)"
        )

        config.set("initial-intensity", 10)
        config.addComment("initial-intensity", "Intensidad inicial (meteoritos por oleada)")

        config.set("max-intensity", 40)
        config.addComment("max-intensity", "Intensidad máxima")

        config.set("intensity-increase-rate", 60)
        config.addComment("intensity-increase-rate", "Cada cuántos ticks aumenta la intensidad")

        config.set("intensity-increase-amount", 2)
        config.addComment("intensity-increase-amount", "Cuánto aumenta la intensidad cada vez")

        // spawn settings
        config.set("spawn-distance", 30.0)
        config.addComment(
                "spawn-distance",
                "Distancia base desde la arena donde aparecen los meteoritos"
        )

        config.set("lateral-offset-multiplier", 2.0)
        config.addComment(
                "lateral-offset-multiplier",
                "Multiplicador de distancia lateral (cubre más área)"
        )

        config.set("spawn-height-min", 20.0)
        config.addComment("spawn-height-min", "Altura mínima sobre el punto máximo de la arena")

        config.set("spawn-height-max", 60.0)
        config.addComment("spawn-height-max", "Altura máxima sobre el punto máximo de la arena")

        // meteor settings
        config.set("min-size", 0.8)
        config.addComment("min-size", "Tamaño mínimo del meteorito")

        config.set("max-size", 2.5)
        config.addComment("max-size", "Tamaño máximo del meteorito")

        config.set("explosion-multiplier", 3.5)
        config.addComment(
                "explosion-multiplier",
                "Multiplicador de poder de explosión basado en tamaño"
        )

        // physics settings
        config.set("fall-speed", 0.4)
        config.addComment("fall-speed", "Velocidad de caída horizontal")

        config.set("downward-force", -0.6)
        config.addComment("downward-force", "Fuerza hacia abajo (negativo = hacia abajo)")

        config.set("max-lifetime-ticks", 300)
        config.addComment(
                "max-lifetime-ticks",
                "Tiempo máximo de vida del meteorito en ticks (seguridad)"
        )

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

private fun YamlConfiguration.addComment(key: String, comment: String) {}
