# Disasters Plugin - Guía de Configuración Rápida

## 🚀 Configuración Inicial (5 minutos)

### 1. Instalación Básica

```bash
# 1. Descargar el plugin
# Colocar el archivo .jar en la carpeta plugins/

# 2. Reiniciar el servidor
# El plugin creará las carpetas necesarias automáticamente
```

### 2. Configuración Mínima

Edita `plugins/Disasters/config.yml`:

```yaml
# REQUERIDO: Obtener clave de licencia en https://discord.smartshub.dev/
license: "TU_LICENCIA_AQUI"

# Configuración de base de datos (H2 es suficiente para empezar)
database:
  driver: h2

# Ubicación del lobby (donde regresan los jugadores)
lobby:
  world: world
  x: 0
  y: 64
  z: 0
  yaw: 0
  pitch: 0

enable-scoreboard: true
```

### 3. Crear tu Primera Arena

```bash
# 1. El plugin crea automáticamente un archivo de ejemplo:
# plugins/Disasters/Arenas/example_arena.yml

# 2. Copia y renombra el archivo:
cp plugins/Disasters/Arenas/example_arena.yml plugins/Disasters/Arenas/mi_arena.yml
```

Edita `plugins/Disasters/Arenas/mi_arena.yml`:

```yaml
max-players: 8
min-players: 2
alive-to-end: 1
countdown: 10
game-time: 300
disaster-rate: 30
max-disasters: 3
display-name: "<green>Mi Arena"

# IMPORTANTE: Configura las ubicaciones
spawn:
  x: 100
  y: 64
  z: 100
  yaw: 0
  pitch: 0
  world: world

corner1:
  x: 50
  y: 0
  z: 50
  world: world

corner2:
  x: 150
  y: 100
  z: 150
  world: world
```

### 4. Configurar el Lobby

```bash
# En el juego, ejecuta:
/disasters setspawn
```

### 5. ¡Listo para Jugar!

```bash
# Los jugadores pueden usar:
/arena join mi_arena
/arena quickjoin
/arena leave
```

---

## 🎮 Comandos Esenciales

### Para Jugadores
```bash
/arena join <arena>     # Unirse a una arena específica
/arena quickjoin        # Unirse automáticamente a la mejor arena
/arena leave           # Salir de la arena actual
```

### Para Administradores
```bash
/disasters setspawn     # Establecer ubicación del lobby
/disasters reload       # Recargar configuración
/arena forcestart       # Forzar inicio del juego
/arena forcestop        # Detener el juego
```

---

## 🏗️ Configuración de Arena Paso a Paso

### Paso 1: Construir la Arena

1. **Construye tu arena** en el mundo
2. **Anota las coordenadas** de dos esquinas opuestas
3. **Elige un punto de spawn** para los jugadores

### Paso 2: Configurar el Archivo

```yaml
# Nombre del archivo = nombre de la arena
# Ejemplo: arena_pvp.yml = /arena join arena_pvp

max-players: 10          # Máximo de jugadores
min-players: 2           # Mínimo para iniciar
countdown: 15            # Segundos antes de iniciar
game-time: 600           # Duración del juego (10 minutos)
disaster-rate: 45        # Desastre cada 45 segundos
max-disasters: 4         # Máximo 4 desastres simultáneos

# Ubicación donde aparecen los jugadores
spawn:
  world: world
  x: 100.5
  y: 65
  z: 100.5
  yaw: 180
  pitch: 0

# Región de la arena (dos esquinas opuestas)
corner1:
  world: world
  x: 50
  y: 0
  z: 50

corner2:
  world: world
  x: 150
  y: 100
  z: 150

# Comandos al final del juego (opcional)
commands:
  winners:
    - "eco give %player_name% 50"
    - "say ¡%player_name% ganó!"
  losers:
    - "eco take %player_name% 5"
  to-all:
    - "say ¡Gracias por jugar!"
```

### Paso 3: Probar la Arena

```bash
# 1. Recargar configuración
/disasters reload

# 2. Unirse a la arena
/arena join mi_arena

# 3. Forzar inicio (si tienes permisos)
/arena forcestart
```

---

## ⚙️ Configuraciones Avanzadas

### Base de Datos MySQL

```yaml
database:
  driver: mysql
  host: localhost
  port: 3306
  db-name: disasters
  username: minecraft
  password: password123
```

### Múltiples Mundos

```yaml
# Arena en mundo personalizado
spawn:
  world: minigames_world
  x: 0
  y: 64
  z: 0
  yaw: 0
  pitch: 0

corner1:
  world: minigames_world
  x: -50
  y: 0
  z: -50

corner2:
  world: minigames_world
  x: 50
  y: 100
  z: 50
```

### Integración con Economía

```yaml
commands:
  winners:
    - "eco give %player_name% 100"
    - "lp user %player_name% permission set disasters.vip true"
    - "give %player_name% diamond 1"
  
  losers:
    - "eco take %player_name% 10"
  
  to-all:
    - "give %player_name% bread 2"
```

---

## 🔧 Solución de Problemas Comunes

### Problema: "Arena not found"
```bash
# Verificar que el archivo existe:
ls plugins/Disasters/Arenas/

# Verificar sintaxis del archivo:
# Usar un editor que valide YAML
```

### Problema: "You are not in an arena"
```bash
# El jugador debe unirse primero:
/arena join nombre_arena
```

### Problema: Arena no se inicia
```bash
# Verificar configuración mínima:
min-players: 2  # Debe ser menor que max-players

# Verificar que hay suficientes jugadores
# Usar /arena forcestart si tienes permisos
```

### Problema: Jugadores no pueden salir
```bash
# Usar comando de salida:
/arena leave

# O teleportar manualmente:
/tp nombre_jugador x y z mundo
```

### Problema: Desastres no funcionan
```bash
# Verificar configuración:
disaster-rate: 30    # Desastre cada 30 segundos
max-disasters: 3     # Máximo 3 desastres

# Recargar configuración:
/disasters reload
```

---

## 📊 Permisos Recomendados

### Para Jugadores Normales
```yaml
# No necesitan permisos especiales
# Pueden usar comandos básicos de arena
```

### Para Moderadores
```yaml
disasters.forcestart: true    # Forzar inicio de juegos
disasters.forcestop: true     # Detener juegos
```

### Para Administradores
```yaml
disasters.admin: true         # Todos los comandos admin
disasters.forcestart: true
disasters.forcestop: true
```

---

## 🎯 Tips de Optimización

### Para Servidores Pequeños (< 20 jugadores)
```yaml
# Configuración recomendada:
max-players: 8
min-players: 2
game-time: 300        # 5 minutos
disaster-rate: 45     # Desastre cada 45 segundos
max-disasters: 3
```

### Para Servidores Grandes (20+ jugadores)
```yaml
# Configuración recomendada:
max-players: 16
min-players: 4
game-time: 600        # 10 minutos
disaster-rate: 30     # Desastre cada 30 segundos
max-disasters: 4
```

### Para Mejor Rendimiento
```yaml
# Usar H2 para servidores pequeños
database:
  driver: h2

# Usar MySQL para servidores grandes
database:
  driver: mysql
  # ... configuración MySQL
```

---

## 📝 Checklist de Configuración

- [ ] Plugin instalado y servidor reiniciado
- [ ] Licencia configurada en `config.yml`
- [ ] Ubicación del lobby establecida (`/disasters setspawn`)
- [ ] Al menos una arena creada y configurada
- [ ] Coordenadas de arena verificadas
- [ ] Permisos configurados para staff
- [ ] Comandos de prueba ejecutados
- [ ] Jugadores pueden unirse y jugar

---

## 🆘 Soporte

Si tienes problemas:

1. **Verifica la configuración** usando esta guía
2. **Revisa los logs** del servidor para errores
3. **Contacta soporte** en https://discord.smartshub.dev/
   - Solo para usuarios con licencia válida
   - Incluye logs y configuración

---

¡Listo! Tu servidor Disasters está configurado y funcionando. 🎉
