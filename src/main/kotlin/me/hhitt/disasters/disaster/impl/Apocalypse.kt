package me.hhitt.disasters.disaster.impl

import kotlin.random.Random
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.DisasterFileManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Apocalypse : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val zombiesData = mutableMapOf<Zombie, ZombieData>()

    private var spawnInterval = 40
    private var zombiesPerPlayer = 2
    private var spawnRadius = 10
    private var preventSunBurn = true
    private var movementSpeed = 1.2
    private var zombieHealth = 30.0
    private var canBreakDoors = true
    private var canBreakBlocks = true
    private var blockBreakInterval = 30
    private var blockBreakRadius = 2
    private var helmetChance = 0.3
    private var enchantedHelmetChance = 0.3
    private var maxProtectionLevel = 2
    private var weaponChance = 0.4
    private var enchantedWeaponChance = 0.25
    private var maxSharpnessLevel = 2
    private var fullArmorChance = 0.15
    private var fullArmorType = "IRON"
    private var potionEffectChance = 0.2
    private var babyZombieChance = 0.1
    private var babySpeedMultiplier = 1.5

    private val possibleHelmets = mutableListOf<ItemConfig>()
    private val possibleWeapons = mutableListOf<ItemConfig>()
    private val possibleEffects = mutableListOf<EffectConfig>()
    private val breakableBlocks = mutableSetOf<Material>()

    override fun start(arena: Arena) {
        loadConfig()
        arenas.add(arena)
    }

    override fun pulse(time: Int) {
        if (time % spawnInterval != 0) {
            processZombieBlockBreaking()
            return
        }

        arenas.toList().forEach { arena ->
            arena.alive.toList().forEach { player ->
                spawnZombiesNearPlayer(player, spawnRadius, zombiesPerPlayer)
            }
        }

        processZombieBlockBreaking()
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        zombiesData.keys.toList().forEach { zombie ->
            if (zombie.isValid) {
                zombie.remove()
            }
        }
        zombiesData.clear()
    }

    private fun loadConfig() {
        val config = DisasterFileManager.getDisasterConfig("apocalypse")

        if (config == null) {
            Disasters.getInstance()
                    .logger
                    .warning("Apocalypse config not found! Using default values.")
            return
        }

        // cargar configuracion basica
        spawnInterval = config.getInt("spawn-interval", 40)
        zombiesPerPlayer = config.getInt("zombies-per-player", 2)
        spawnRadius = config.getInt("spawn-radius", 10)
        preventSunBurn = config.getBoolean("prevent-sun-burn", true)
        movementSpeed = config.getDouble("movement-speed", 1.2)
        zombieHealth = config.getDouble("zombie-health", 30.0)
        canBreakDoors = config.getBoolean("can-break-doors", true)
        canBreakBlocks = config.getBoolean("can-break-blocks", true)
        blockBreakInterval = config.getInt("block-break-interval", 30)
        blockBreakRadius = config.getInt("block-break-radius", 2)
        helmetChance = config.getDouble("helmet-chance", 0.3)
        enchantedHelmetChance = config.getDouble("enchanted-helmet-chance", 0.3)
        maxProtectionLevel = config.getInt("max-protection-level", 2)
        weaponChance = config.getDouble("weapon-chance", 0.4)
        enchantedWeaponChance = config.getDouble("enchanted-weapon-chance", 0.25)
        maxSharpnessLevel = config.getInt("max-sharpness-level", 2)
        fullArmorChance = config.getDouble("full-armor-chance", 0.15)
        fullArmorType = config.getString("full-armor-type", "IRON")!!
        potionEffectChance = config.getDouble("potion-effect-chance", 0.2)
        babyZombieChance = config.getDouble("baby-zombie-chance", 0.1)
        babySpeedMultiplier = config.getDouble("baby-speed-multiplier", 1.5)

        // cargar bloques rompibles
        breakableBlocks.clear()
        config.getStringList("breakable-blocks").forEach { materialName ->
            try {
                breakableBlocks.add(Material.valueOf(materialName.uppercase()))
            } catch (e: IllegalArgumentException) {
                Disasters.getInstance()
                        .logger
                        .warning("Invalid material in breakable-blocks: $materialName")
            }
        }

        // cargar cascos posibles
        possibleHelmets.clear()
        config.getMapList("possible-helmets").forEach { map ->
            val material = map["material"] as? String ?: return@forEach
            val chance = (map["chance"] as? Number)?.toDouble() ?: 1.0
            possibleHelmets.add(ItemConfig(material, chance))
        }

        // cargar armas posibles
        possibleWeapons.clear()
        config.getMapList("possible-weapons").forEach { map ->
            val material = map["material"] as? String ?: return@forEach
            val chance = (map["chance"] as? Number)?.toDouble() ?: 1.0
            possibleWeapons.add(ItemConfig(material, chance))
        }

        // cargar efectos posibles
        possibleEffects.clear()
        config.getMapList("possible-effects").forEach { map ->
            val effect = map["effect"] as? String ?: return@forEach
            val amplifier = (map["amplifier"] as? Number)?.toInt() ?: 0
            val duration = (map["duration"] as? Number)?.toInt() ?: 999999
            val chance = (map["chance"] as? Number)?.toDouble() ?: 1.0
            possibleEffects.add(EffectConfig(effect, amplifier, duration, chance))
        }
    }

    private fun spawnZombiesNearPlayer(player: Player, radius: Int, amount: Int) {
        val world: World = player.world
        repeat(amount) {
            val spawnLocation = findSafeSpawnLocation(player.location, radius)
            spawnLocation?.let { loc ->
                val zombie = world.spawnEntity(loc, EntityType.ZOMBIE) as Zombie
                customizeZombie(zombie)
                zombiesData[zombie] = ZombieData(0)
            }
        }
    }

    private fun processZombieBlockBreaking() {
        if (!canBreakBlocks) return

        val iterator = zombiesData.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val zombie = entry.key
            val data = entry.value

            // remover zombies muertos o invalidos
            if (!zombie.isValid || zombie.isDead) {
                iterator.remove()
                continue
            }

            data.ticksSinceLastBreak++

            if (data.ticksSinceLastBreak >= blockBreakInterval) {
                tryBreakNearbyBlock(zombie)
                data.ticksSinceLastBreak = 0
            }
        }
    }

    private fun tryBreakNearbyBlock(zombie: Zombie) {
        val location = zombie.location
        val nearbyBlocks = mutableListOf<Block>()

        // buscar bloques cercanos
        for (x in -blockBreakRadius..blockBreakRadius) {
            for (y in -1..2) {
                for (z in -blockBreakRadius..blockBreakRadius) {
                    val block =
                            location.world.getBlockAt(
                                    location.blockX + x,
                                    location.blockY + y,
                                    location.blockZ + z
                            )

                    if (breakableBlocks.contains(block.type)) {
                        nearbyBlocks.add(block)
                    }
                }
            }
        }

        if (nearbyBlocks.isEmpty()) return

        val blockToBreak = nearbyBlocks.random()

        // reproducir sonido de romper puerta
        location.world.playSound(
                blockToBreak.location,
                Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                1.0f,
                0.8f
        )

        blockToBreak.type = Material.AIR
    }

    private fun customizeZombie(zombie: Zombie) {

        // configurar vida
        val healthAttr = zombie.getAttribute(Attribute.MAX_HEALTH)
        healthAttr?.baseValue = zombieHealth
        zombie.health = zombieHealth

        // configurar velocidad
        val speedAttr = zombie.getAttribute(Attribute.MOVEMENT_SPEED)
        speedAttr?.let { attr -> attr.baseValue = attr.baseValue * movementSpeed }

        zombie.setCanBreakDoors(canBreakDoors)

        // zombie bebe
        if (Random.nextDouble() < babyZombieChance) {
            zombie.setBaby()
            val babySpeedAttr = zombie.getAttribute(Attribute.MOVEMENT_SPEED)
            if (babySpeedAttr != null) {
                val speed = babySpeedAttr.baseValue
                babySpeedAttr.baseValue = speed * babySpeedMultiplier
            }
        }

        // equipar armadura
        if (Random.nextDouble() < fullArmorChance) {
            equipFullArmor(zombie)
        } else {
            if (Random.nextDouble() < helmetChance) {
                equipHelmet(zombie)
            }
        }

        // equipar arma
        if (Random.nextDouble() < weaponChance) {
            equipWeapon(zombie)
        }

        // aplicar efectos
        if (Random.nextDouble() < potionEffectChance) {
            applyPotionEffect(zombie)
        }
    }

    private fun equipHelmet(zombie: Zombie) {
        if (possibleHelmets.isEmpty()) return

        val helmet = selectRandomItem(possibleHelmets) ?: return
        val material = Material.getMaterial(helmet.material) ?: return
        val item = ItemStack(material)

        if (Random.nextDouble() < enchantedHelmetChance) {
            val level = Random.nextInt(1, maxProtectionLevel + 1)
            item.addEnchantment(Enchantment.PROTECTION, level)
        }

        zombie.equipment?.helmet = item
        zombie.equipment?.helmetDropChance = 0.0f
    }

    private fun equipWeapon(zombie: Zombie) {
        if (possibleWeapons.isEmpty()) return

        val weapon = selectRandomItem(possibleWeapons) ?: return
        val material = Material.getMaterial(weapon.material) ?: return
        val item = ItemStack(material)

        if (Random.nextDouble() < enchantedWeaponChance) {
            val level = Random.nextInt(1, maxSharpnessLevel + 1)
            item.addEnchantment(Enchantment.SHARPNESS, level)
        }

        zombie.equipment?.setItemInMainHand(item)
        zombie.equipment?.itemInMainHandDropChance = 0.0f
    }

    private fun equipFullArmor(zombie: Zombie) {
        val prefix = fullArmorType.uppercase()

        val helmet = Material.getMaterial("${prefix}_HELMET")
        val chestplate = Material.getMaterial("${prefix}_CHESTPLATE")
        val leggings = Material.getMaterial("${prefix}_LEGGINGS")
        val boots = Material.getMaterial("${prefix}_BOOTS")

        helmet?.let {
            zombie.equipment?.helmet = ItemStack(it)
            zombie.equipment?.helmetDropChance = 0.0f
        }
        chestplate?.let {
            zombie.equipment?.chestplate = ItemStack(it)
            zombie.equipment?.chestplateDropChance = 0.0f
        }
        leggings?.let {
            zombie.equipment?.leggings = ItemStack(it)
            zombie.equipment?.leggingsDropChance = 0.0f
        }
        boots?.let {
            zombie.equipment?.boots = ItemStack(it)
            zombie.equipment?.bootsDropChance = 0.0f
        }
    }

    private fun applyPotionEffect(zombie: Zombie) {
        if (possibleEffects.isEmpty()) return

        val effectConfig = selectRandomEffect() ?: return
        val effectType = PotionEffectType.getByName(effectConfig.effect) ?: return
        val effect =
                PotionEffect(
                        effectType,
                        effectConfig.duration,
                        effectConfig.amplifier,
                        false,
                        false
                )
        zombie.addPotionEffect(effect)
    }

    private fun selectRandomItem(items: List<ItemConfig>): ItemConfig? {
        val totalWeight = items.sumOf { it.chance }
        var random = Random.nextDouble() * totalWeight
        for (item in items) {
            random -= item.chance
            if (random <= 0) return item
        }
        return items.firstOrNull()
    }

    private fun selectRandomEffect(): EffectConfig? {
        val totalWeight = possibleEffects.sumOf { it.chance }
        var random = Random.nextDouble() * totalWeight
        for (effect in possibleEffects) {
            random -= effect.chance
            if (random <= 0) return effect
        }
        return possibleEffects.firstOrNull()
    }

    private fun findSafeSpawnLocation(location: Location, radius: Int): Location? {
        repeat(10) {
            val randomX = location.x + Random.nextDouble(-radius.toDouble(), radius.toDouble())
            val randomZ = location.z + Random.nextDouble(-radius.toDouble(), radius.toDouble())
            val highestY =
                    location.world.getHighestBlockYAt(randomX.toInt(), randomZ.toInt()).toDouble()
            val potentialLocation = Location(location.world, randomX, highestY + 1, randomZ)

            if (isSafeLocation(potentialLocation)) {
                return potentialLocation
            }
        }
        return null
    }

    private fun isSafeLocation(location: Location): Boolean {
        val world = location.world
        val block = world.getBlockAt(location)
        val blockAbove = world.getBlockAt(location.add(0.0, 1.0, 0.0))

        return block.type != Material.LAVA &&
                block.type != Material.CACTUS &&
                blockAbove.type == Material.AIR
    }

    private data class ItemConfig(val material: String, val chance: Double)

    private data class EffectConfig(
            val effect: String,
            val amplifier: Int,
            val duration: Int,
            val chance: Double
    )

    private data class ZombieData(var ticksSinceLastBreak: Int)
}
