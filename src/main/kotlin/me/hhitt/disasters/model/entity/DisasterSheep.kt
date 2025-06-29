package me.hhitt.disasters.model.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.PanicGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
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

class DisasterSheep(entityType: EntityType<out Sheep>, level: Level, location: Location): Sheep(entityType, level) {

    private var tick: Int = 3

    init {
        this.isInvulnerable = true
        this.color = DyeColor.GREEN
        this.setPos(location.x, location.y, location.z)
        this.isNoAi = false
    }

    fun call() {

        when(tick) {
            3 -> {
                this.color = DyeColor.GREEN
                tick--
            }
            2 -> {
                this.color = DyeColor.ORANGE
                tick--
            }
            1 -> {
                this.color = DyeColor.RED
                tick--
            }
            0 -> {
                val location = Location(this.level().world , this.x, this.y, this.z)
                spawnTNTExplosion(location)
                this.remove(RemovalReason.KILLED)
            }
        }
    }


    private fun spawnTNTExplosion(location: Location) {
        val world: World = location.world ?: return

        val tnt = world.spawnEntity(location, org.bukkit.entity.EntityType.TNT) as TNTPrimed
        tnt.fuseTicks = 0
        tnt.isSilent = false
        tnt.yield = 4.0f
    }

    fun enableFollowNearestPlayer() {
        this.goalSelector.availableGoals.clear()

        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, PanicGoal(this, 1.25))
        this.goalSelector.addGoal(2, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        this.goalSelector.addGoal(3, RandomLookAroundGoal(this))
        this.goalSelector.addGoal(4, RandomStrollGoal(this, 1.0))

        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(
            this,
            Player::class.java,
            true
        )
        )
    }


}