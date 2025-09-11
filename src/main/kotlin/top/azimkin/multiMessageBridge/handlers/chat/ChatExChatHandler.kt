package top.azimkin.multiMessageBridge.handlers.chat

import de.jeter.chatex.api.events.PlayerUsesGlobalChatEvent
import de.jeter.chatex.api.events.PlayerUsesRangeModeEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver
import java.lang.Boolean.parseBoolean

class ChatExChatHandler(val minecraftReceiver: MinecraftReceiver, config: Map<String, String>) : ChatHandler(),
    Listener {

    private val parsedConfig = Config(config)

    init {
        Bukkit.getPluginManager().registerEvents(this, MultiMessageBridge.inst)
    }

    @EventHandler
    fun onRangedChat(event: PlayerUsesRangeModeEvent) {
        if (parsedConfig.useRangedAsGlobal) dispatchMessage(event.player, event.message)
    }

    @EventHandler
    fun onGlobalChat(event: PlayerUsesGlobalChatEvent) {
        dispatchMessage(event.player, event.message)
    }

    private fun dispatchMessage(player: Player, message: String) {
        onReceive(
            MessageContext(
                senderName = player.name,
                message = message,
                platform = minecraftReceiver.name,
                role = MultiMessageBridge.inst.metadataProvider.getPrefix(player)
            )
        )
    }

    private class Config(map: Map<String, String>) {
        val useRangedAsGlobal: Boolean = parseBoolean(map["useRangedAsGlobal"])
    }
}