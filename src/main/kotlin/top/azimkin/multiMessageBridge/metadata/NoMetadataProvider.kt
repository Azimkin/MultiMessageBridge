package top.azimkin.multiMessageBridge.metadata

import org.bukkit.entity.Player

class NoMetadataProvider : PlayerMetadataProvider {
    override fun getPrefix(player: Player): String = ""
}