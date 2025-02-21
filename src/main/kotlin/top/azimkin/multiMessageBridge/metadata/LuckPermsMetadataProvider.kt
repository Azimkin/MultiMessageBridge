package top.azimkin.multiMessageBridge.metadata

import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player

class LuckPermsMetadataProvider : PlayerMetadataProvider {
    val provider = LuckPermsProvider.get()
    override fun getPrefix(player: Player): String {
        val metadata = provider.getPlayerAdapter(Player::class.java).getMetaData(player)
        return metadata.prefix ?: ""
    }
}