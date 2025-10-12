# Disasters Plugin - Tipos de Desastres

## 📋 Tabla de Contenidos

1. [Desastres Ambientales](#desastres-ambientales)
2. [Desastres de Movimiento](#desastres-de-movimiento)
3. [Desastres de Visibilidad](#desastres-de-visibilidad)
4. [Desastres de PvP](#desastres-de-pvp)
5. [Desastres de Entidades](#desastres-de-entidades)
6. [Desastres de Bloques](#desastres-de-bloques)
7. [Desastres de Efectos](#desastres-de-efectos)
8. [Configuración de Desastres](#configuración-de-desastres)

---

## 🌧️ Desastres Ambientales

### 1. Acid Rain (Lluvia Ácida)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Lluvia ácida que daña a jugadores sin cobertura
- **Mecánica**: 
  - Aplica efecto de lluvia a todos los jugadores
  - Daña 2 corazones cada segundo si no hay bloque encima
  - Los bloques de cobertura se rompen automáticamente
- **Contraindicaciones**: Buscar refugio bajo bloques sólidos
- **Mensaje**: "Acid Rain! Take cover!"

### 2. Hot Sun (Sol Ardiente)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: El sol quema a jugadores expuestos
- **Mecánica**:
  - Daña a jugadores sin cobertura durante el día
  - Efecto similar a la lluvia ácida pero solo de día
- **Contraindicaciones**: Buscar sombra o refugio
- **Mensaje**: "Hot Sun! Now the sun will burn u!"

### 3. Lightning (Rayos)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Rayos aleatorios en el área
- **Mecánica**:
  - Genera rayos en ubicaciones aleatorias de la arena
  - Los rayos pueden impactar a jugadores
- **Contraindicaciones**: Moverse constantemente, evitar áreas abiertas
- **Mensaje**: "Lightning! Take cover!"

### 4. World Border (Frontera del Mundo)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Frontera que se reduce gradualmente
- **Mecánica**:
  - Crea una frontera visible alrededor del centro de la arena
  - La frontera se reduce cada segundo
  - Los jugadores fuera de la frontera reciben daño
- **Contraindicaciones**: Moverse hacia el centro constantemente
- **Mensaje**: "World Border! Run to the center!"

---

## 🏃 Desastres de Movimiento

### 5. Zero Gravity (Gravedad Cero)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Efecto de levitación a todos los jugadores
- **Mecánica**:
  - Aplica levitación a todos los jugadores
  - Los jugadores flotan en el aire
  - Dificulta el movimiento controlado
- **Contraindicaciones**: Usar bloques para estabilizarse
- **Mensaje**: "No Gravity! You will experiment some levitation!"

### 6. Grounded (Confinado al Suelo)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Los jugadores no pueden saltar
- **Mecánica**:
  - Deshabilita la capacidad de saltar
  - Los jugadores están confinados al suelo
  - Dificulta escapar de situaciones peligrosas
- **Contraindicaciones**: Usar escaleras o bloques para subir
- **Mensaje**: "Grounded! You can't jump!"

### 7. Floor is Lava (El Suelo es Lava)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: El suelo se convierte en lava
- **Mecánica**:
  - Los bloques del suelo se convierten en lava
  - Los jugadores que toquen la lava reciben daño
  - La lava se extiende gradualmente
- **Contraindicaciones**: Mantenerse en bloques elevados
- **Mensaje**: "Floor is Lava! Don't touch the ground!"

---

## 👁️ Desastres de Visibilidad

### 8. Blind (Ceguera)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Los jugadores quedan ciegos
- **Mecánica**:
  - Aplica ceguera total a todos los jugadores
  - La pantalla se vuelve completamente negra
  - Dificulta la navegación y supervivencia
- **Contraindicaciones**: Memorizar el mapa, usar sonidos
- **Mensaje**: "Blind! Your vision will be null!"

### 9. Cobweb (Telarañas)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Telarañas aparecen cada 5 segundos
- **Mecánica**:
  - Spawna telarañas en ubicaciones aleatorias
  - Las telarañas ralentizan el movimiento
  - Se acumulan con el tiempo
- **Contraindicaciones**: Llevar tijeras o espada para cortar
- **Mensaje**: "Cobweb! You will be caught by cobwebs!"

---

## ⚔️ Desastres de PvP

### 10. Allow Fight (Permitir Combate)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Habilita combate entre jugadores
- **Mecánica**:
  - Permite PvP entre todos los jugadores
  - Los jugadores pueden atacarse mutuamente
  - Cambia completamente la dinámica del juego
- **Contraindicaciones**: Estar preparado para combate
- **Mensaje**: "PvP Enabled! Now u can hit players!"

### 11. Murder (Asesino)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Un jugador aleatorio recibe una espada para matar
- **Mecánica**:
  - Selecciona un jugador aleatorio como asesino
  - Le da una espada de madera en el slot 5
  - Solo el asesino puede matar a otros jugadores
- **Contraindicaciones**: Identificar al asesino y evitarlo
- **Mensaje**: "Murder! Now players with sword can kill u!"

### 12. One Heart (Un Corazón)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Todos los jugadores tienen solo 1 corazón
- **Mecánica**:
  - Establece la salud máxima a 1 corazón
  - Cualquier daño elimina al jugador
  - Hace el juego extremadamente difícil
- **Contraindicaciones**: Evitar cualquier daño
- **Mensaje**: "One Hearth! Don't get hit!"

---

## 🐑 Desastres de Entidades

### 13. Wither (Wither Boss)
- **Duración**: Hasta que el Wither sea derrotado
- **Efecto**: Spawn de un boss Wither
- **Mecánica**:
  - Crea un Wither en el centro de la arena
  - El Wither ataca a todos los jugadores
  - Debe ser derrotado para detener el desastre
- **Contraindicaciones**: Trabajar en equipo para derrotarlo
- **Mensaje**: "Wither Attack! A wither boss has arrive!"

### 14. Explosive Sheep (Ovejas Explosivas)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Ovejas explosivas aparecen en el área
- **Mecánica**:
  - Spawna ovejas que explotan al contacto
  - Las ovejas persiguen a los jugadores
  - Las explosiones causan daño y destrucción
- **Contraindicaciones**: Mantener distancia, no tocar las ovejas
- **Mensaje**: "Explosive Sheep! Watch out!"

---

## 🧱 Desastres de Bloques

### 15. Block Disappear (Bloques Desaparecen)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Los bloques bajo los jugadores desaparecen
- **Mecánica**:
  - Los bloques bajo los jugadores se convierten en aire
  - Los jugadores caen constantemente
  - Crea caídas peligrosas
- **Contraindicaciones**: Moverse constantemente, evitar quedarse quieto
- **Mensaje**: "Blocks disappear! Now blocks under u will disappear!"

### 16. Swap (Intercambio)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Los jugadores intercambian posiciones aleatoriamente
- **Mecánica**:
  - Cambia las ubicaciones de jugadores aleatoriamente
  - Puede teleportar a ubicaciones peligrosas
  - Ocurre periódicamente durante el desastre
- **Contraindicaciones**: Estar preparado para cambios de ubicación
- **Mensaje**: "Swap! Switch places!"

---

## 🌪️ Desastres de Efectos

### 17. Lag (Lag Simulado)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Simula lag severo
- **Mecánica**:
  - Aplica efectos que simulan lag del servidor
  - Ralentiza el movimiento y acciones
  - Dificulta la jugabilidad
- **Contraindicaciones**: Adaptarse al lag, ser paciente
- **Mensaje**: "Lag! You will experiment lag!"

### 18. Apocalypse (Apocalipsis)
- **Duración**: Hasta que se detenga manualmente
- **Efecto**: Combina múltiples efectos destructivos
- **Mecánica**:
  - Aplica varios desastres simultáneamente
  - Efectos combinados de múltiples desastres
  - El desastre más peligroso
- **Contraindicaciones**: Supervivencia extrema requerida
- **Mensaje**: "Apocalypse! Run for your life!"

---

## ⚙️ Configuración de Desastres

### Parámetros de Arena

```yaml
# En el archivo de configuración de arena
disaster-rate: 30        # Desastre cada 30 segundos
max-disasters: 4         # Máximo 4 desastres simultáneos
```

### Frecuencia Recomendada por Tipo de Servidor

#### Servidor Casual
```yaml
disaster-rate: 45        # Desastre cada 45 segundos
max-disasters: 3         # Máximo 3 desastres
```

#### Servidor Competitivo
```yaml
disaster-rate: 20        # Desastre cada 20 segundos
max-disasters: 5         # Máximo 5 desastres
```

#### Servidor Hardcore
```yaml
disaster-rate: 15        # Desastre cada 15 segundos
max-disasters: 6         # Máximo 6 desastres
```

### Personalización de Mensajes

Los mensajes de desastres se configuran en `lang.yml`:

```yaml
disaster:
  acid-rain:
    title: "<red>Acid Rain!"
    subtitle: "<red>Take cover!"
    chat:
      - "<gold>----------------------------------"
      - "<red>Acid Rain is falling from the sky!"
      - "<red>Take cover!"
      - "<gold>----------------------------------"
```

### Orden de Prioridad de Desastres

Los desastres se seleccionan aleatoriamente, pero algunos son más comunes:

1. **Comunes**: Acid Rain, Hot Sun, Lightning
2. **Moderados**: Grounded, Zero Gravity, Blind
3. **Raros**: Wither, Apocalypse, Murder
4. **Especiales**: Allow Fight, One Heart

---

## 🎯 Estrategias por Desastre

### Para Desastres Ambientales
- **Acid Rain/Hot Sun**: Buscar refugio inmediatamente
- **Lightning**: Mantener movimiento constante
- **World Border**: Planificar ruta hacia el centro

### Para Desastres de Movimiento
- **Zero Gravity**: Usar bloques para estabilizarse
- **Grounded**: Construir escaleras o rampas
- **Floor is Lava**: Buscar plataformas elevadas

### Para Desastres de PvP
- **Allow Fight**: Preparar armas y armadura
- **Murder**: Identificar y evitar al asesino
- **One Heart**: Priorizar supervivencia sobre combate

### Para Desastres de Entidades
- **Wither**: Trabajar en equipo para derrotarlo
- **Explosive Sheep**: Mantener distancia y usar proyectiles

---

## 📊 Estadísticas de Desastres

### Desastres Más Peligrosos
1. **Apocalypse** - Múltiples efectos
2. **One Heart** - Sin margen de error
3. **Floor is Lava** - Daño constante
4. **World Border** - Presión de tiempo
5. **Wither** - Enemigo poderoso

### Desastres Más Comunes
1. **Acid Rain** - 15% de probabilidad
2. **Hot Sun** - 12% de probabilidad
3. **Lightning** - 10% de probabilidad
4. **Grounded** - 8% de probabilidad
5. **Blind** - 8% de probabilidad

### Tiempo Promedio de Supervivencia
- **Sin desastres**: 5-10 minutos
- **Con desastres**: 2-5 minutos
- **Con múltiples desastres**: 30 segundos - 2 minutos

---

Esta documentación cubre todos los tipos de desastres disponibles en el plugin Disasters. Cada desastre está diseñado para crear desafíos únicos y mantener el juego emocionante y desafiante.
