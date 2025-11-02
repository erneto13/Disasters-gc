package me.hhitt.disasters.util

import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

object EntityUtils {

    fun setScaledHealth(entity: LivingEntity, multiplier: Double) {
        val attr = entity.getAttribute(Attribute.MAX_HEALTH) ?: return
        attr.baseValue = attr.baseValue * multiplier
        entity.health = attr.baseValue
    }
}
