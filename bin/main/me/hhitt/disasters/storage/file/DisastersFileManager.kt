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
        val disasterFiles =
                disastersFolder.listFiles { _, name -> name.endsWith(".yml", ignoreCase = true) }
                        ?: return

        for (file in disasterFiles) {
            val disasterName = file.nameWithoutExtension
            loadDisasterConfig(disasterName)
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
