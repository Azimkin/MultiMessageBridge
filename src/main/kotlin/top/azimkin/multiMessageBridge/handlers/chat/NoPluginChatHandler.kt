package top.azimkin.multiMessageBridge.handlers.chat

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver
import top.azimkin.multiMessageBridge.utilities.toPlainText

class NoPluginChatHandler(val minecraftReceiver: MinecraftReceiver) : ChatHandler(), Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, MultiMessageBridge.inst)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onMinecraftMessage(event: AsyncChatEvent) {
        if (event.isCancelled) return
        onReceive(MessageContext(
            event.player.name,
            event.message().toPlainText(),
            false,
            minecraftReceiver.name,
            null,
            null,
            MultiMessageBridge.inst.metadataProvider.getPrefix(event.player)
        ))
    }
}