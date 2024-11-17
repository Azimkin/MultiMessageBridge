package top.azimkin.multiMessageBridge.metadata

import org.bukkit.entity.Player

interface PlayerMetadataProvider {
    fun getPrefix(player: Player): String
}