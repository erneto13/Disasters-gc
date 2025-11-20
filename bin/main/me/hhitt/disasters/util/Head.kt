package me.hhitt.disasters.util

import com.destroystokyo.paper.profile.ProfileProperty
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object Head {

    fun fromBase64(base64: String): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD)
        val meta = skull.itemMeta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID(), null)
        profile.properties.add(ProfileProperty("textures", base64))
        meta.playerProfile = profile
        skull.itemMeta = meta
        return skull
    }
}
