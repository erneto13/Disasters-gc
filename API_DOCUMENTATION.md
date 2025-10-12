# Disasters Plugin - Documentación de API

## 📋 Tabla de Contenidos

1. [API Principal](#api-principal)
2. [Sistema de Arenas](#sistema-de-arenas)
3. [Sistema de Desastres](#sistema-de-desastres)
4. [Gestión de Datos](#gestión-de-datos)
5. [Eventos y Listeners](#eventos-y-listeners)
6. [Utilidades](#utilidades)
7. [Ejemplos de Uso](#ejemplos-de-uso)

---

## 🎯 API Principal

### Clase `Disasters`

```kotlin
class Disasters : JavaPlugin() {
    companion object {
        fun getInstance(): Disasters
    }
}
```

**Métodos principales:**
- `getInstance()`: Obtiene la instancia del plugin
- `onEnable()`: Inicialización del plugin
- `onDisable()`: Limpieza al deshabilitar

---

## 🏟️ Sistema de Arenas

### Clase `Arena`

```kotlin
class Arena(
    val name: String,
    val displayName: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val aliveToEnd: Int,
    val gameTime: Int,
    val countdown: Int,
    val disasterRate: Int,
    val maxDisasters: Int,
    val spawnLocation: Location,
    val corner1: Location,
    val corner2: Location,
    val winnersCommands: List<String>,
    val losersCommands: List<String>,
    val toAllCommands: List<String>,
    private val worldEdit: WorldEditPlugin?
)
```

**Propiedades principales:**
- `name`: ID único de la arena
- `displayName`: Nombre mostrado a los jugadores
- `playing`: Lista de jugadores en la arena
- `alive`: Lista de jugadores vivos
- `state`: Estado actual del juego
- `disasters`: Lista de desastres activos

**Métodos principales:**
```kotlin
// Gestión de jugadores
fun addPlayer(player: Player): Boolean
fun removePlayer(player: Player): Boolean
fun isPlayerValid(player: Player): Boolean

// Estados del juego
fun isWaiting(): Boolean
fun isFull(): Boolean
fun isEmpty(): Boolean

// Control del juego
fun start()
fun stop()

// Servicios
val borderService: BorderService
val resetArenaService: ResetArenaService
val respawnService: RespawnService
```

### Clase `ArenaManager`

```kotlin
class ArenaManager(private val worldEdit: WorldEditPlugin?)
```

**Métodos principales:**
```kotlin
// Gestión de arenas
fun getArenas(): List<Arena>
fun getArena(player: Player): Arena?
fun getArena(arenaId: String): Arena?
fun getArena(location: Location): Arena?

// Gestión de jugadores
fun addPlayerToBestArena(player: Player)
fun reloadArenas()
fun removeArena(arenaID: String): Boolean
```

---

## 🌪️ Sistema de Desastres

### Interfaz `Disaster`

```kotlin
interface Disaster {
    fun start(arena: Arena)
    fun pulse(time: Int)
    fun stop(arena: Arena)
}
```

### Clase `DisasterRegistry`

```kotlin
object DisasterRegistry
```

**Métodos principales:**
```kotlin
// Gestión de desastres
fun addRandomDisaster(arena: Arena)
fun pulseAll(time: Int)
fun removeDisasters(arena: Arena)

// Desastres específicos
fun addBlockToDisappear(arena: Arena, location: Location)
fun removeBlockFromDisappear(arena: Arena, block: DisappearBlock)
fun addBlockToFloorIsLava(arena: Arena, location: Location)
fun removeBlockFromFloorIsLava(arena: Arena, block: DisasterFloor)

// Verificaciones de estado
fun isGrounded(arena: Arena, player: Player): Boolean
fun isAllowedToFight(arena: Arena, player: Player): Boolean
fun isMurder(arena: Arena, player: Player): Boolean
```

### Tipos de Desastres Disponibles

#### 1. AcidRain
```kotlin
class AcidRain : Disaster
```
- **Efecto**: Lluvia ácida que daña a jugadores sin cobertura
- **Mecánica**: Daña 2 corazones cada pulse si no hay bloque encima

#### 2. Apocalypse
```kotlin
class Apocalypse : Disaster
```
- **Efecto**: Combina múltiples efectos destructivos
- **Mecánica**: Aplica varios efectos simultáneamente

#### 3. Blind
```kotlin
class Blind : Disaster
```
- **Efecto**: Los jugadores quedan ciegos
- **Mecánica**: Aplica ceguera a todos los jugadores

#### 4. Cobweb
```kotlin
class Cobweb : Disaster
```
- **Efecto**: Telarañas aparecen cada 5 segundos
- **Mecánica**: Spawna telarañas en ubicaciones aleatorias

#### 5. Lag
```kotlin
class Lag : Disaster
```
- **Efecto**: Simula lag severo
- **Mecánica**: Aplica efectos que simulan lag

#### 6. Wither
```kotlin
class Wither : Disaster
```
- **Efecto**: Spawn de un boss Wither
- **Mecánica**: Crea un Wither en el centro de la arena

#### 7. AllowFight
```kotlin
class AllowFight : Disaster
```
- **Efecto**: Habilita combate entre jugadores
- **Mecánica**: Permite PvP entre jugadores

#### 8. BlockDisappear
```kotlin
class BlockDisappear : Disaster
```
- **Efecto**: Los bloques bajo los jugadores desaparecen
- **Mecánica**: Los bloques se convierten en aire gradualmente

#### 9. HotSun
```kotlin
class HotSun : Disaster
```
- **Efecto**: El sol quema a jugadores expuestos
- **Mecánica**: Daña a jugadores sin cobertura durante el día

#### 10. Murder
```kotlin
class Murder : Disaster
```
- **Efecto**: Un jugador aleatorio recibe una espada
- **Mecánica**: Selecciona un jugador para ser el asesino

#### 11. ZeroGravity
```kotlin
class ZeroGravity : Disaster
```
- **Efecto**: Efecto de levitación
- **Mecánica**: Aplica levitación a todos los jugadores

#### 12. ExplosiveSheep
```kotlin
class ExplosiveSheep : Disaster
```
- **Efecto**: Ovejas explosivas aparecen
- **Mecánica**: Spawna ovejas que explotan al contacto

#### 13. FloorIsLava
```kotlin
class FloorIsLava : Disaster
```
- **Efecto**: El suelo se convierte en lava
- **Mecánica**: Convierte bloques del suelo en lava

#### 14. Grounded
```kotlin
class Grounded : Disaster
```
- **Efecto**: Los jugadores no pueden saltar
- **Mecánica**: Deshabilita el salto

#### 15. Lightning
```kotlin
class Lightning : Disaster
```
- **Efecto**: Rayos aleatorios
- **Mecánica**: Genera rayos en ubicaciones aleatorias

#### 16. OneHeart
```kotlin
class OneHeart : Disaster
```
- **Efecto**: Todos tienen solo 1 corazón
- **Mecánica**: Establece la salud máxima a 1 corazón

#### 17. Swap
```kotlin
class Swap : Disaster
```
- **Efecto**: Los jugadores intercambian posiciones
- **Mecánica**: Cambia las ubicaciones de jugadores aleatoriamente

#### 18. WorldBorder
```kotlin
class WorldBorder : Disaster
```
- **Efecto**: Frontera que se reduce gradualmente
- **Mecánica**: Crea una frontera que se encoge y daña a jugadores fuera

---

## 💾 Gestión de Datos

### Clase `PlayerStats`

```kotlin
data class PlayerStats(
    val playerId: String,
    val wins: Int,
    val defeats: Int,
    val totalPlayed: Int
)
```

### Clase `PlayerStatsDAO`

```kotlin
class PlayerStatsDAO(private val dataSource: DataSource)
```

**Métodos principales:**
```kotlin
fun createTable()
fun insertOrUpdateStats(stats: PlayerStats)
fun getStats(playerId: String): PlayerStats?
fun getAllStats(): List<PlayerStats>
```

### Clase `Cache`

```kotlin
class Cache<T>(private val maxSize: Int = 100)
```

**Métodos principales:**
```kotlin
fun get(key: String): T?
fun put(key: String, value: T)
fun remove(key: String)
fun clear()
```

---

## 🎧 Eventos y Listeners

### Listeners Principales

#### `PlayerJoinListener`
- Maneja cuando un jugador se une al servidor
- Actualiza estadísticas y scoreboard

#### `PlayerLeaveListener`
- Maneja cuando un jugador sale del servidor
- Limpia datos temporales

#### `PlayerDeathListener`
- Maneja muertes de jugadores
- Actualiza estado del juego

#### `PlayerDamageListener`
- Maneja daño a jugadores
- Aplica lógica específica de desastres

#### `PlayerMoveListener`
- Maneja movimiento de jugadores
- Verifica límites de arena y efectos

#### `BlockBreakListener`
- Maneja rotura de bloques
- Aplica reglas específicas de arena

#### `BlockPlaceListener`
- Maneja colocación de bloques
- Aplica restricciones de arena

---

## 🛠️ Utilidades

### Clase `Msg`

```kotlin
object Msg
```

**Métodos principales:**
```kotlin
fun send(sender: CommandSender, messageKey: String, vararg replacements: Pair<String, String>)
fun broadcast(arena: Arena, messageKey: String, vararg replacements: Pair<String, String>)
```

### Clase `Notify`

```kotlin
object Notify
```

**Métodos principales:**
```kotlin
fun disaster(arena: Arena, disasterKey: String)
fun title(player: Player, title: String, subtitle: String)
```

### Clase `Lobby`

```kotlin
object Lobby
```

**Métodos principales:**
```kotlin
fun setLocation()
fun teleport(player: Player)
```

### Clase `Filer`

```kotlin
object Filer
```

**Métodos principales:**
```kotlin
fun getFile(name: String): File?
fun createFile(name: String): File?
```

---

## 📝 Ejemplos de Uso

### Crear un Desastre Personalizado

```kotlin
class CustomDisaster : Disaster {
    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "custom-disaster")
        
        // Lógica de inicio del desastre
        arena.playing.forEach { player ->
            player.sendMessage("¡Desastre personalizado activado!")
        }
    }

    override fun pulse(time: Int) {
        arenas.forEach { arena ->
            // Lógica que se ejecuta cada segundo
            arena.alive.forEach { player ->
                // Aplicar efectos
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        
        // Limpiar efectos
        arena.playing.forEach { player ->
            // Remover efectos
        }
    }
}
```

### Registrar un Desastre Personalizado

```kotlin
// En el archivo DisasterRegistry.kt, agregar a la lista:
private val disasterClasses = listOf(
    // ... desastres existentes
    CustomDisaster::class
)
```

### Crear un Comando Personalizado

```kotlin
@Command("custom")
class CustomCommand(private val arenaManager: ArenaManager) {
    
    @Subcommand("info")
    fun info(actor: BukkitCommandActor) {
        if (!actor.isPlayer) return
        val player = actor.asPlayer()!!
        
        val arena = arenaManager.getArena(player)
        if (arena != null) {
            player.sendMessage("Estás en la arena: ${arena.displayName}")
            player.sendMessage("Estado: ${arena.state}")
            player.sendMessage("Jugadores: ${arena.playing.size}/${arena.maxPlayers}")
        } else {
            player.sendMessage("No estás en ninguna arena")
        }
    }
}
```

### Acceder a Estadísticas de Jugador

```kotlin
fun getPlayerStats(player: Player): PlayerStats? {
    val dao = PlayerStatsDAO(dataSource)
    return dao.getStats(player.uniqueId.toString())
}

fun updatePlayerWin(player: Player) {
    val dao = PlayerStatsDAO(dataSource)
    val currentStats = dao.getStats(player.uniqueId.toString()) ?: PlayerStats(
        playerId = player.uniqueId.toString(),
        wins = 0,
        defeats = 0,
        totalPlayed = 0
    )
    
    val updatedStats = currentStats.copy(
        wins = currentStats.wins + 1,
        totalPlayed = currentStats.totalPlayed + 1
    )
    
    dao.insertOrUpdateStats(updatedStats)
}
```

### Crear un Placeholder Personalizado

```kotlin
// En PlaceholderAPIHook.kt
class CustomPlaceholder : PlaceholderExpansion() {
    
    override fun getIdentifier(): String = "disasters_custom"
    
    override fun onRequest(player: Player?, params: String): String {
        return when (params.lowercase()) {
            "custom_value" -> "valor personalizado"
            else -> ""
        }
    }
}
```

---

## 🔧 Configuración Avanzada

### Variables de Entorno para Comandos

Los comandos en las arenas soportan las siguientes variables:

- `%player_name%`: Nombre del jugador
- `%player_uuid%`: UUID del jugador
- `%arena_name%`: Nombre de la arena
- `%game_time%`: Tiempo del juego
- `%players_count%`: Cantidad de jugadores

### Ejemplo de Configuración de Comandos

```yaml
commands:
  winners:
    - "eco give %player_name% 100"
    - "lp user %player_name% permission set disasters.vip true"
    - "say ¡%player_name% ha ganado en %arena_name%!"
  
  losers:
    - "eco take %player_name% 10"
    - "say %player_name% ha sido eliminado"
  
  to-all:
    - "say Gracias por jugar Disasters!"
    - "give %player_name% bread 1"
```

---

## 📚 Notas de Desarrollo

### Mejores Prácticas

1. **Manejo de Errores**: Siempre verifica si los objetos son null
2. **Performance**: Usa cache para operaciones frecuentes
3. **Threading**: Usa corrutinas para operaciones asíncronas
4. **Memoria**: Limpia referencias en el método `stop()`

### Depuración

```kotlin
// Habilitar logs detallados
plugin.logger.info("Debug: Arena ${arena.name} state: ${arena.state}")
plugin.logger.info("Debug: Player ${player.name} in arena: ${arenaManager.getArena(player)?.name}")
```

### Testing

```kotlin
// Crear arena de prueba
val testArena = Arena(
    name = "test",
    displayName = "Test Arena",
    minPlayers = 1,
    maxPlayers = 2,
    // ... otros parámetros
)

// Agregar jugador de prueba
testArena.addPlayer(player)
testArena.start()
```

---

Esta documentación cubre las APIs principales del plugin Disasters. Para más detalles específicos, consulta el código fuente en el repositorio.
