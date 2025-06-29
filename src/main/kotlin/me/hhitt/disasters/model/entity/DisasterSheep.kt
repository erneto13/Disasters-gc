package me.hhitt.disasters.model.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal
import net.minecraft.world.entity.ai.goal.PanicGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.animal.sheep.Sheep
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Level
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.TNTPrimed

/**
 * DisasterSheep is a class that represents a sheep entity that changes color and explodes
 * in a sequence of colors: green, orange, and red. After the red color, it spawns a TNT explosion.
 *
 * @param entityType The type of the entity (Sheep).
 * @param level The level where the entity is spawned.
 * @param location The location where the entity is spawned.
 */

class DisasterSheep(
    entityType: EntityType<out Sheep>,
    level: Level,
    location: Location
) : Sheep(entityType, level) {

    private var tick = 3

    init {
        isInvulnerable = true
        color = DyeColor.GREEN
        setPos(location.x, location.y, location.z)
        isNoAi = false
        setupGoals()
    }

    fun call() {
        when (tick) {
            3 -> color = DyeColor.GREEN
            2 -> color = DyeColor.ORANGE
            1 -> color = DyeColor.RED
            0 -> {
                val loc = Location(level().world, x, y, z)
                spawnTNTExplosion(loc)
                remove(RemovalReason.KILLED)
                return
            }
        }
        tick--
    }

    private fun setupGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, PanicGoal(this, 1.25))
        goalSelector.addGoal(2, RandomStrollGoal(this, 1.0))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 32.0f))
        goalSelector.addGoal(4, MoveTowardsRestrictionGoal(this, 1.2))

        targetSelector.addGoal(5, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    private fun spawnTNTExplosion(location: Location) {
        val world: World = location.world ?: return
        val tnt = world.spawn(location, TNTPrimed::class.java)
        tnt.fuseTicks = 0
        tnt.yield = 4.0f
    }
}