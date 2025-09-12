package top.azimkin.multiMessageBridge.handlers.chat

import net.essentialsx.api.v2.events.chat.GlobalChatEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver

class EssentialsChatHandler(val minecraftReceiver: MinecraftReceiver) : ChatHandler(), Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, MultiMessageBridge.inst)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onMessage(event: GlobalChatEvent) {
        if (!event.isCancelled) {
            onReceive(
                MessageContext(
                    senderName = event.player.name,
                    message = event.message,
                    platform = minecraftReceiver.name,
                    role = MultiMessageBridge.inst.metadataProvider.getPrefix(event.player)
                )
            )
        }
    }
}