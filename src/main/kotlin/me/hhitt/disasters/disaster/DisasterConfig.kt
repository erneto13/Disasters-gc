package me.hhitt.disasters.disaster

import kotlin.reflect.KClass
import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.configuration.file.FileConfiguration

/** DisasterConfig manages disaster configuration including priorities, exclusions, and patterns. */
object DisasterConfig {

    enum class Priority {
        LOW, // Ligeros
        MEDIUM, // Normales
        HIGH // Destructivos
    }

    data class DisasterWave(val priority: Priority, val count: Int)

    private lateinit var config: FileConfiguration

    // Mapa de desastres incompatibles entre sí
    private val exclusions = mutableMapOf<String, Set<String>>()

    // Mapa de prioridades de desastres
    private val priorities = mutableMapOf<String, Priority>()

    // Desastres deshabilitados
    private val disabledDisasters = mutableSetOf<String>()

    // Patrón de oleadas de desastres
    private val disasterPattern = mutableListOf<DisasterWave>()

    // Si se debe repetir el patrón
    private var repeatPattern = true

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
        loadDisasterPattern()
    }

    private fun createDefaultConfig() {
        // El archivo será creado con valores por defecto
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

    private fun loadDisasterPattern() {
        disasterPattern.clear()
        val patternList = config.getStringList("disaster-pattern")
        repeatPattern = config.getBoolean("repeat-pattern", true)

        for (entry in patternList) {
            val parts = entry.split(":")
            if (parts.size == 2) {
                val priorityStr = parts[0].uppercase()
                val count = parts[1].toIntOrNull() ?: 1

                val priority =
                        try {
                            Priority.valueOf(priorityStr)
                        } catch (e: IllegalArgumentException) {
                            Priority.MEDIUM
                        }

                disasterPattern.add(DisasterWave(priority, count))
            }
        }

        // Si no hay patrón definido, usar uno por defecto
        if (disasterPattern.isEmpty()) {
            disasterPattern.add(DisasterWave(Priority.HIGH, 1))
            disasterPattern.add(DisasterWave(Priority.MEDIUM, 2))
            disasterPattern.add(DisasterWave(Priority.LOW, 3))
        }
    }

    /** Verifica si un desastre está habilitado */
    fun isEnabled(disasterClass: KClass<out Disaster>): Boolean {
        val name = disasterClass.simpleName?.lowercase() ?: return false
        val enabled = !disabledDisasters.contains(name)
        if (!enabled) {
            println("[DisasterConfig] ${disasterClass.simpleName} está DESHABILITADO")
        }
        return enabled
    }

    /** Obtiene la prioridad de un desastre */
    fun getPriority(disasterClass: KClass<out Disaster>): Priority {
        val name = disasterClass.simpleName?.lowercase() ?: return Priority.MEDIUM
        return priorities[name] ?: Priority.MEDIUM
    }

    /** Verifica si dos desastres son compatibles */
    fun areCompatible(disaster1: KClass<out Disaster>, disaster2: KClass<out Disaster>): Boolean {
        val name1 = disaster1.simpleName?.lowercase() ?: return true
        val name2 = disaster2.simpleName?.lowercase() ?: return true

        val excluded1 = exclusions[name1] ?: emptySet()
        val excluded2 = exclusions[name2] ?: emptySet()

        return !excluded1.contains(name2) && !excluded2.contains(name1)
    }

    /** Verifica si un desastre es compatible con los desastres activos */
    fun isCompatibleWithActive(
            disasterClass: KClass<out Disaster>,
            activeDisasters: List<Disaster>
    ): Boolean {
        return activeDisasters.all { active -> areCompatible(disasterClass, active::class) }
    }

    /**
     * Obtiene la oleada actual según el número de activaciones
     * @param activationCount Número de veces que se han activado desastres
     * @return La oleada correspondiente
     */
    fun getCurrentWave(activationCount: Int): DisasterWave {
        if (disasterPattern.isEmpty()) {
            return DisasterWave(Priority.MEDIUM, 1)
        }

        val index =
                if (repeatPattern) {
                    activationCount % disasterPattern.size
                } else {
                    activationCount.coerceAtMost(disasterPattern.size - 1)
                }

        return disasterPattern[index]
    }

    fun getDisastersByPriority(
            allDisasters: List<KClass<out Disaster>>,
            priority: Priority
    ): List<KClass<out Disaster>> {
        return allDisasters.filter { disaster ->
            isEnabled(disaster) && getPriority(disaster) == priority
        }
    }

    /** Obtiene configuración específica de un desastre */
    fun getDisasterConfig(disasterClass: KClass<out Disaster>, key: String): Any? {
        val name = disasterClass.simpleName?.lowercase() ?: return null
        return config.get("disaster-settings.$name.$key")
    }

    /** Obtiene el patrón actual como string para mostrar */
    fun getPatternDescription(): List<String> {
        return disasterPattern.mapIndexed { index, wave ->
            "Oleada ${index + 1}: ${wave.count}x ${wave.priority}"
        }
    }

    /** Recarga la configuración */
    fun reload() {
        FileManager.reload("disasters")
        load()
    }
}
