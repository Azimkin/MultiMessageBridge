package top.azimkin.multiMessageBridge.metadata

import net.milkbowl.vault.chat.Chat
import org.bukkit.entity.Player
import top.azimkin.multiMessageBridge.MultiMessageBridge

class VaultMetadataProvider : PlayerMetadataProvider {
    val service: Chat = MultiMessageBridge.inst.server.servicesManager.getRegistration(Chat::class.java)!!.provider
    override fun getPrefix(player: Player): String = service.getPlayerPrefix(player)
}